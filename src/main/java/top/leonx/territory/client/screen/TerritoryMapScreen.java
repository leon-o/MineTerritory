package top.leonx.territory.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import top.leonx.territory.container.TerritoryTableContainer;

import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TerritoryMapScreen extends AbstractScreenPage<TerritoryTableContainer> {

    private final DynamicTexture mapTexture = new DynamicTexture(256, 256, true);
    private final ResourceLocation mapLocation = Minecraft.getInstance().getTextureManager().getDynamicTextureLocation(
            "map_dynamic" + MathHelper.nextInt(new Random(), 0, 128), mapTexture);

    private static final ResourceLocation territorySquareLocation = new ResourceLocation("minecraft", "textures/block" +
            "/blue_stained_glass.png");
    private static final ResourceLocation edgeSquareLocation = new ResourceLocation("territory", "textures/gui" +
            "/point_overlay.png");
    private static final ResourceLocation mouseOnSquareLocation = new ResourceLocation("minecraft", "textures/block" +
            "/light_blue_stained_glass.png");
    private static final ResourceLocation expandSquareLocation = new ResourceLocation("minecraft", "textures/block" +
            "/cyan_stained_glass.png");
    private static final ResourceLocation forbiddenSquareLocation = new ResourceLocation("minecraft", "textures/block" +
            "/red_stained_glass.png");

    private TextFieldWidget territoryNameTextField;
    public TerritoryMapScreen(TerritoryTableContainer container, ContainerScreen<TerritoryTableContainer> parent, Consumer<Integer> changePage) {
        super(container,parent,changePage);
    }


    public void init() {

        final int halfW = width / 2;
        final int halfH = height / 2;
        territoryNameTextField=new TextFieldWidget(font, parent.getGuiLeft()+160, parent.getGuiTop()+24, 80,16,"Name");
        this.addButton(new GuiButtonExt(halfW + 40, halfH + 50, 70, 20, I18n.format("gui.territory.done_btn"),
                $ -> container.Done()
        ));
        this.addButton(new GuiButtonExt(halfW+40, halfH + 28, 70, 20, I18n.format("gui.territory.permission_btn"),
                $ -> NavigateTo(1)
        ));
        mapLeftTopChunkPos = new ChunkPos((container.tileEntityPos.getX() >> 4) - 4, (container.tileEntityPos.getZ() >> 4) - 4);
        territoryNameTextField.setText(container.territoryInfo.territoryName);
        this.children.add(territoryNameTextField);
        drawMapTexture();

    }


    @Override
    public void renderInternal(final int mouseX, final int mouseY, final float partialTicks) {
        //playerList.render(mouseX,mouseY,partialTicks);
        territoryNameTextField.render(mouseX,mouseY,partialTicks);
        territoryNameTextField.renderButton(mouseX,mouseY,partialTicks);
        font.drawString(I18n.format("gui.territory.territory_name"),parent.getGuiLeft()+160,parent.getGuiTop()+8,0xFFFFFF);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
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
        int mouseOnChunkX = ((int) mouseX - this.parent.getGuiLeft() - mapPosLeft) / 16;
        int mouseOnChunkY = ((int) mouseY - this.parent.getGuiTop() - mapPosTop) / 16;
        ChunkPos pos = new ChunkPos(mapLeftTopChunkPos.x + mouseOnChunkX, mapLeftTopChunkPos.z + mouseOnChunkY);

        if(mouseOnChunkX < chunkNumX && mouseOnChunkY < chunkNumY)
        {
            // select new territory
            if (getContainer().selectableChunkPos.contains(pos) && container.getUsedProtectPower()<container.getTotalProtectPower()) {
                container.territories.add(pos);
            }else if(getContainer().removableChunkPos.contains(pos))
            {
                // select the territory to be removed
                container.territories.remove(pos);
            }
            getContainer().initChunkInfo();
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

        container.territories.forEach(pos->
        {
            int chunkX = pos.x;
            int chunkZ = pos.z;
            blit(mapPosLeft + ((chunkX - mapLeftTopChunkPos.x) << 4), mapPosTop + ((chunkZ - mapLeftTopChunkPos.z) << 4), 0, 0, 16,
                    16,16,16);
        });


        if(container.getUsedProtectPower()<container.getTotalProtectPower())
        {
            getMinecraft().getTextureManager().bindTexture(expandSquareLocation);
            // 绘制可以选择的新区块
            drawOverlayByCollection(container.selectableChunkPos);
        }

        getMinecraft().getTextureManager().bindTexture(edgeSquareLocation);
        drawOverlayByCollection(container.removableChunkPos);

        // 绘制鼠标悬浮的区
        int mouseOnChunkX = (mouseX - this.parent.getGuiLeft() - mapPosLeft) / 16;
        int mouseOnChunkY = (mouseY - this.parent.getGuiTop() - mapPosTop) / 16;

        getMinecraft().getTextureManager().bindTexture(mouseOnSquareLocation);
        ChunkPos mouseOverPos = new ChunkPos(mapLeftTopChunkPos.x + mouseOnChunkX, mapLeftTopChunkPos.z + mouseOnChunkY);
        if (getContainer().removableChunkPos.contains(mouseOverPos)||
                (container.getUsedProtectPower() < getContainer().getTotalProtectPower() && getContainer().selectableChunkPos.contains(mouseOverPos))
                && mouseOnChunkX >= 0 && mouseOnChunkX < chunkNumX
                && mouseOnChunkY >= 0 && mouseOnChunkY < chunkNumY) {

            blit(mapPosLeft + (mouseOnChunkX << 4), mapPosTop + (mouseOnChunkY << 4), 0, 0, 16,
                    16,16,16);
        }
        getMinecraft().getTextureManager().bindTexture(forbiddenSquareLocation);
        drawOverlayByCollection(container.forbiddenChunkPos.stream().filter(
                t->t.x>=mapLeftTopChunkPos.x&&t.x<=mapLeftTopChunkPos.x+mapSizeX/16&&t.z>=mapLeftTopChunkPos.z&&t.z<=mapLeftTopChunkPos.z+mapSizeY/16)
                .collect(Collectors.toList()));

    }

    private void drawOverlayByCollection(Collection<ChunkPos> collection) {
        for (ChunkPos pos : collection) {
            int posX = pos.x - mapLeftTopChunkPos.x;
            int posZ = pos.z - mapLeftTopChunkPos.z;
            if (posX < 0 || posZ < 0 || posX >= chunkNumX || posZ >= chunkNumY) continue;

            blit(mapPosLeft + (posX << 4), mapPosTop + (posZ << 4),
                    0, 0, 16, 16, 16, 16);
        }
    }


    @SuppressWarnings("deprecation")
    private void drawMapTexture() {

        BlockPos centerPos = container.tileEntityPos;
        //int[][] heightMap=new int[mapSizeX][mapSizeY];
        for (int i = 0; i < mapSizeX; ++i) {

            for (int j = 0; j < mapSizeY; ++j) {
                BlockPos pos = new BlockPos(centerPos.getX() + i-mapSizeX/2, 120, centerPos.getZ() + j-mapSizeX/2);
                BlockState state = null;
                for (; pos.getY() > 0; pos = pos.down()) {
                    state = Minecraft.getInstance().world.getBlockState(pos);
                    if (state.getBlock() != Blocks.AIR) {
                        break;
                    }
                }
                MaterialColor materialColor = state.getMaterialColor(Minecraft.getInstance().world, pos);
                int l = materialColor.getMapColor(materialColor.colorIndex);

                this.mapTexture.getTextureData().setPixelRGBA(i, j,
                        l | 0xFF000000);
            }
        }
        this.mapTexture.updateDynamicTexture();
    }

    @Override
    public void tick() {
        super.tick();
        container.territoryInfo.territoryName=territoryNameTextField.getText();
    }
}
