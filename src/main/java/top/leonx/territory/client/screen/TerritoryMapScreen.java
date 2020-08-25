package top.leonx.territory.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import top.leonx.territory.container.TerritoryTableContainer;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TerritoryMapScreen extends AbstractScreenPage<TerritoryTableContainer> {

    final static         int              mapPosLeft              = 10;
    final static         int              mapPosTop               = 10;
    final static         int              mapSizeX                = 144;
    final static         int              mapSizeY                = 144;
    final static         int              chunkNumX               = mapSizeX / 16;
    final static         int              chunkNumY               = mapSizeY / 16;
    private static final ResourceLocation territorySquareLocation = new ResourceLocation("minecraft", "textures/block/blue_stained_glass.png");
    private static final ResourceLocation edgeSquareLocation      = new ResourceLocation("territory", "textures/gui/point_overlay.png");
    private static final ResourceLocation mouseOnSquareLocation   = new ResourceLocation("minecraft", "textures/block/light_blue_stained_glass.png");
    private static final ResourceLocation expandSquareLocation    = new ResourceLocation("minecraft", "textures/block/cyan_stained_glass.png");
    private static final ResourceLocation forbiddenSquareLocation = new ResourceLocation("minecraft", "textures/block/red_stained_glass.png");
    private static final ResourceLocation xpIconLocation          = new ResourceLocation("territory", "textures/gui/xp_icon.png");
    private static final int              foregroundColor         = 0x293134;
    private              TextFieldWidget territoryNameTextField;
    private GuiButtonExt               doneButton;
    private int xpRequired;
    public TerritoryMapScreen(TerritoryTableContainer container, ContainerScreen<TerritoryTableContainer> parent, Consumer<Integer> changePage) {
        super(container, parent, changePage);
    }

    public void init() {

        final int halfW = width / 2;
        territoryNameTextField = new TextFieldWidget(font, parent.getGuiLeft() + 160, parent.getGuiTop() + 24, 80, 16, "Name");
        doneButton = new GuiButtonExt(halfW + 40, parent.getGuiTop() + parent.getYSize() - 30, 70, 20,
                                        I18n.format("gui.territory.done_btn"), $ -> container.Done());
        this.addButton(doneButton);
        this.addButton(new GuiButtonExt(halfW + 40, parent.getGuiTop()+parent.getYSize()-52, 70, 20, I18n.format("gui.territory.permission_btn"),
                                          $ -> NavigateTo(1)));

        territoryNameTextField.setText(container.territoryInfo.territoryName);
        this.children.add(territoryNameTextField);
    }

    @Override
    public void renderInternal(final int mouseX, final int mouseY, final float partialTicks) {
        territoryNameTextField.render(mouseX, mouseY, partialTicks);
        territoryNameTextField.renderButton(mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawMap();
        drawChunkOverlay(mouseX, mouseY);
        font.drawString(I18n.format("gui.territory.territory_name"),  160, 8, foregroundColor);

        String protectPowerTranslation=I18n.format("gui.territory.protect_power");
        font.drawString(protectPowerTranslation,  160,  50, foregroundColor);

        font.drawString(Integer.toString(container.getUsedProtectPower()),  160,  64,
                             container.getUsedProtectPower() < container.getTotalProtectPower() ? 0x00A838 : foregroundColor);
        font.drawString("/" + container.getTotalProtectPower(),
                        160 + this.font.getStringWidth(Integer.toString(container.getUsedProtectPower())), 64,
                        foregroundColor);

        if(xpRequired>0)
        {
            String xpRequiredTranslation=I18n.format("gui.territory.xp_required");
            font.drawString(xpRequiredTranslation,160,79, foregroundColor);
            if(xpRequired>container.getPlayerLevel())
            {
                Minecraft.getInstance().textureManager.bindTexture(xpIconLocation);
                blit(157,89,(Math.min(xpRequired,3)-1)*16,16,16,16,48,32);
                font.drawString(Integer.toString(xpRequired),  173,  93,0x8c605d);
            }else {
                Minecraft.getInstance().textureManager.bindTexture(xpIconLocation);
                blit(157,89,(Math.min(xpRequired,3)-1)*16,0,16,16,48,32);
                font.drawString(Integer.toString(xpRequired),  173,  93,0xc8ff8f);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_) {
        int      mouseOnChunkX = ((int) mouseX - this.parent.getGuiLeft() - mapPosLeft) / 16;
        int      mouseOnChunkY = ((int) mouseY - this.parent.getGuiTop() - mapPosTop) / 16;
        ChunkPos pos           = new ChunkPos(container.mapLeftTopChunkPos.x + mouseOnChunkX, container.mapLeftTopChunkPos.z + mouseOnChunkY);

        if (mouseOnChunkX < chunkNumX && mouseOnChunkY < chunkNumY) {
            // select new territory
            if (getContainer().selectableChunkPos.contains(pos) && container.getUsedProtectPower() < container.getTotalProtectPower()) {
                container.territories.add(pos);
            } else if (getContainer().removableChunkPos.contains(pos)) {
                // select the territory to be removed
                container.territories.remove(pos);
            }
            getContainer().initChunkInfo();
        }

        return super.mouseClicked(mouseX, mouseY, p_mouseClicked_5_);
    }

    private void drawMap() {
        getMinecraft().getTextureManager().bindTexture(container.mapLocation);
        blit(mapPosLeft, mapPosTop, 0, 0, mapSizeX, mapSizeY,mapSizeX,mapSizeY);
    }

    private void drawChunkOverlay(int mouseX, int mouseY) {

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        getMinecraft().getTextureManager().bindTexture(territorySquareLocation);

        container.territories.forEach(pos -> {
            int chunkX = pos.x;
            int chunkZ = pos.z;
            blit(mapPosLeft + ((chunkX - container.mapLeftTopChunkPos.x) << 4), mapPosTop + ((chunkZ - container.mapLeftTopChunkPos.z) << 4), 0, 0, 16, 16, 16,
                 16);
        });


        if (container.getUsedProtectPower() < container.getTotalProtectPower()) {
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
        ChunkPos mouseOverPos = new ChunkPos(container.mapLeftTopChunkPos.x + mouseOnChunkX, container.mapLeftTopChunkPos.z + mouseOnChunkY);
        if (getContainer().removableChunkPos.contains(
                mouseOverPos) || (container.getUsedProtectPower() < getContainer().getTotalProtectPower() && getContainer().selectableChunkPos.contains(
                mouseOverPos)) && mouseOnChunkX >= 0 && mouseOnChunkX < chunkNumX && mouseOnChunkY >= 0 && mouseOnChunkY < chunkNumY) {

            blit(mapPosLeft + (mouseOnChunkX << 4), mapPosTop + (mouseOnChunkY << 4), 0, 0, 16, 16, 16, 16);
        }
        getMinecraft().getTextureManager().bindTexture(forbiddenSquareLocation);
        drawOverlayByCollection(container.forbiddenChunkPos.stream().filter(
                t -> t.x >= container.mapLeftTopChunkPos.x && t.x <= container.mapLeftTopChunkPos.x + mapSizeX / 16 && t.z >= container.mapLeftTopChunkPos.z && t.z <= container.mapLeftTopChunkPos.z + mapSizeY / 16).collect(
                Collectors.toList()));

    }

    private void drawOverlayByCollection(Collection<ChunkPos> collection) {
        for (ChunkPos pos : collection) {
            int posX = pos.x - container.mapLeftTopChunkPos.x;
            int posZ = pos.z - container.mapLeftTopChunkPos.z;
            if (posX < 0 || posZ < 0 || posX >= chunkNumX || posZ >= chunkNumY) continue;

            blit(mapPosLeft + (posX << 4), mapPosTop + (posZ << 4), 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public void tick() {
        super.tick();
        container.territoryInfo.territoryName = territoryNameTextField.getText();
        xpRequired = container.getXpRequired();
        doneButton.active= xpRequired<=container.getPlayerLevel();
    }
}
