package top.leonx.territory.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.opengl.GL11;
import top.leonx.territory.container.TerritoryContainer;

import java.awt.*;
import java.util.*;

public class TerritoryScreen extends ContainerScreen<TerritoryContainer> {

    private final DynamicTexture mapTexture = new DynamicTexture(256, 256, true);
    private final ResourceLocation mapLocation = Minecraft.getInstance().getTextureManager().getDynamicTextureLocation(
            "map_dynamic" + MathHelper.nextInt(new Random(), 0, 128), mapTexture);

    ResourceLocation backgroundLocation = new ResourceLocation("minecraft", "textures/gui/demo_background.png");

    ResourceLocation territorySquareLocation = new ResourceLocation("minecraft", "textures/block/blue_stained_glass.png");
    ResourceLocation edgeSquareLocation = new ResourceLocation("territory", "textures/gui/slash_overlay.png");
    ResourceLocation mouseOnSquareLocation = new ResourceLocation("minecraft", "textures/block" +
            "/light_blue_stained_glass.png");
    ResourceLocation expandSquareLocation = new ResourceLocation("minecraft", "textures/block" +
            "/cyan_stained_glass.png");
    ResourceLocation selectedSquareLocation = new ResourceLocation("minecraft", "textures/block" +
            "/white_stained_glass.png");

    public TerritoryScreen(TerritoryContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = 250;
    }


    @Override
    protected void init() {
        final int halfW = this.width / 2;
        final int halfH = this.height / 2;

        this.addButton(new GuiButtonExt(halfW + 40, halfH + 50, 70, 20, "DONE",
                $ -> container.Done()
        ));

        mapLeftTopChunkPos = new ChunkPos((container.centerPos.getX() >> 4) - 4, (container.centerPos.getZ() >> 4) - 4);
        drawMapTexture();
        super.init();

    }


    private void drawSquare(int x, int y, int width, int height, Color color) {
        GlStateManager.bindTexture(0);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.putColorRGBA(0, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        bufferbuilder.pos(x, y + height, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawMap();
        drawChunkOverlay(mouseX, mouseY);
        this.font.drawString(Integer.toString(container.getUsedProtectPower()),
                (this.width >> 1) -10,
                (this.height >> 1) - 20,
                container.getUsedProtectPower()<container.getTotalProtectPower()?0xFF00BF4D : 0xFFFFFFFF);

        this.font.drawString("/"+ container.getTotalProtectPower(), this.width >> 1,
                (this.height >> 1) - 20, 0xFFFFFFFF);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_) {
        int mouseOnChunkX = ((int) mouseX - this.guiLeft - mapPosLeft) / 16;
        int mouseOnChunkY = ((int) mouseY - this.guiTop - mapPosTop) / 16;
        ChunkPos pos = new ChunkPos(mapLeftTopChunkPos.x + mouseOnChunkX, mapLeftTopChunkPos.z + mouseOnChunkY);

        if(mouseOnChunkX < chunkNumX && mouseOnChunkY < chunkNumY)
        {
            // select new territory
            if (getContainer().selectableChunkPos.contains(pos)) {
                if (container.chunkToBeAdded.contains(pos))
                    container.chunkToBeAdded.remove(pos);
                else {
                    // must not exceed protection capacity
                    if (getContainer().jurisdictions.size() + container.chunkToBeAdded.size() < getContainer().getTotalProtectPower())
                        container.chunkToBeAdded.add(pos);
                }
            }else if(getContainer().removableChunkPos.contains(pos) || container.chunkToBeRemoved.contains(pos))
            {
                // select the territory to be removed
                if (container.chunkToBeRemoved.contains(pos))
                    container.chunkToBeRemoved.remove(pos);
                else
                    container.chunkToBeRemoved.add(pos);
            }
            getContainer().initRemovableChunkPos();
            getContainer().initSelectableChunkPos();
        }

        return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
    }
    ChunkPos mapLeftTopChunkPos;


    final static int mapPosLeft = 10;
    final static int mapPosTop = 10;
    final static int mapSizeX = 144;
    final static int mapSizeY = 144;
    final static int chunkNumX = mapSizeX / 16;
    final static int chunkNumY = mapSizeY / 16;

    private void drawMap() {
        getMinecraft().getTextureManager().bindTexture(mapLocation);
        this.blit(mapPosLeft, mapPosTop, 0, 0, mapSizeX, mapSizeY);
    }

    private void drawChunkOverlay(int mouseX, int mouseY) {

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        getMinecraft().getTextureManager().bindTexture(territorySquareLocation);
        //绘制领地区块
        for (int i = 0; i < container.jurisdictions.size(); i++) {
            ChunkPos pos = container.jurisdictions.get(i);
            int chunkX = pos.x;
            int chunkZ = pos.z;
            if(getContainer().chunkToBeRemoved.contains(pos)) continue;
            this.blit(mapPosLeft + ((chunkX - mapLeftTopChunkPos.x) << 4), mapPosTop + ((chunkZ - mapLeftTopChunkPos.z) << 4), 0, 0, 16,
                    16,16,16);
        }


        getMinecraft().getTextureManager().bindTexture(expandSquareLocation);
        // 绘制可以选择的新区块
        drawOverlayByCollection(container.selectableChunkPos);

//        // 绘制要删除的区块
//        drawOverlayByCollection(container.chunkToBeRemoved);

        getMinecraft().getTextureManager().bindTexture(edgeSquareLocation);
        drawOverlayByCollection(container.removableChunkPos);

        // 绘制鼠标悬浮的区域
        int mouseOnChunkX = (mouseX - this.guiLeft - mapPosLeft) / 16;
        int mouseOnChunkY = (mouseY - this.guiTop - mapPosTop) / 16;

        getMinecraft().getTextureManager().bindTexture(mouseOnSquareLocation);
        ChunkPos mouseOverPos = new ChunkPos(mapLeftTopChunkPos.x + mouseOnChunkX, mapLeftTopChunkPos.z + mouseOnChunkY);
        if (getContainer().removableChunkPos.contains(mouseOverPos)||
                (getContainer().jurisdictions.size() + container.chunkToBeAdded.size() < getContainer().getTotalProtectPower() && getContainer().selectableChunkPos.contains(mouseOverPos))
                && mouseOnChunkX >= 0 && mouseOnChunkX < chunkNumX
                && mouseOnChunkY >= 0 && mouseOnChunkY < chunkNumY) {

            this.blit(mapPosLeft + (mouseOnChunkX << 4), mapPosTop + (mouseOnChunkY << 4), 0, 0, 16,
                    16,16,16);
        }


        // 绘制选中的区域
        getMinecraft().getTextureManager().bindTexture(selectedSquareLocation);
        drawOverlayByCollection(container.chunkToBeAdded);
    }

    private void drawOverlayByCollection(Collection<ChunkPos> collection) {
        Iterator<ChunkPos> iterator=collection.iterator();
        while (iterator.hasNext()) {
            ChunkPos pos = iterator.next();
            int posX = pos.x - mapLeftTopChunkPos.x;
            int posZ = pos.z - mapLeftTopChunkPos.z;
            if (posX < 0 || posZ < 0 || posX >= chunkNumX || posZ >= chunkNumY) continue;

            this.blit(mapPosLeft + (posX << 4), mapPosTop + (posZ << 4),
                    0, 0, 16, 16,16,16);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().textureManager.bindTexture(backgroundLocation);
        int startX = this.guiLeft;
        int startY = this.guiTop;
        this.blit(startX, startY, 0, 0, this.xSize, this.ySize);
    }


    private void drawMapTexture() {

        BlockPos centerPos = container.centerPos;
        //int[][] heightMap=new int[mapSizeX][mapSizeY];
        for (int i = 0; i < mapSizeX; ++i) {

            for (int j = 0; j < mapSizeY; ++j) {
                BlockPos pos = new BlockPos(centerPos.getX() + i, 120, centerPos.getZ() + j);
                BlockState state = null;
                for (; pos.getY() > 0; pos = pos.down()) {
                    state = Minecraft.getInstance().world.getBlockState(pos);
                    if (state.getBlock() != Blocks.AIR) {
                        break;
                    }
                }
                MaterialColor materialColor = state.getMaterialColor(Minecraft.getInstance().world, pos);
                int l = materialColor.getMapColor(materialColor.colorIndex);

                this.mapTexture.getTextureData().setPixelRGBA(j, i,
                        l | 0xFF000000);
            }
        }
        this.mapTexture.updateDynamicTexture();
    }

}
