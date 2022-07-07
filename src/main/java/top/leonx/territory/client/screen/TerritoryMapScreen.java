package top.leonx.territory.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import top.leonx.territory.common.container.TerritoryTableContainer;

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
    private              EditBox territoryNameTextField;
    private ExtendedButton doneButton;
    private int xpRequired;
    public TerritoryMapScreen(TerritoryTableContainer container, AbstractContainerScreen<TerritoryTableContainer> parent, Consumer<Integer> changePage) {
        super(container, parent, changePage);
    }

    public void init() {

        final int halfW = width / 2;
        territoryNameTextField = new EditBox(font, parent.getGuiLeft() + 160, parent.getGuiTop() + 24, 80, 16, new TextComponent("Name"));
        doneButton = new ExtendedButton(halfW + 40, parent.getGuiTop() + parent.getYSize() - 30, 70, 20,
                                        new TranslatableComponent("gui.territory.done_btn"), $ -> container.Done());
        this.addButton(doneButton);
        this.addButton(new ExtendedButton(halfW + 40, parent.getGuiTop()+parent.getYSize()-52, 70, 20,  new TranslatableComponent("gui.territory.permission_btn"),
                                          $ -> NavigateTo(1)));

        territoryNameTextField.setValue(container.territoryInfo.territoryName);
        this.children.add(territoryNameTextField);
    }


    @Override
    public void renderInternal(PoseStack matrix,final int mouseX, final int mouseY, final float partialTicks) {
        territoryNameTextField.render(matrix,mouseX, mouseY, partialTicks);
        territoryNameTextField.renderButton(matrix,mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawGuiContainerForegroundLayer(PoseStack stack, int mouseX, int mouseY) {
        drawMap(stack);
        drawChunkOverlay(stack,mouseX, mouseY);
        font.draw(stack, I18n.get("gui.territory.territory_name"),  160, 8, foregroundColor);

        String protectPowerTranslation=I18n.get("gui.territory.protect_power");
        font.draw(stack,protectPowerTranslation,  160,  50, foregroundColor);

        font.draw(stack,Integer.toString(container.getUsedProtectPower()),  160,  64,
                        container.getUsedProtectPower() < container.getTotalProtectPower() ? 0x00A838 : foregroundColor);
        font.draw(stack,"/" + container.getTotalProtectPower(),
                        160 + this.font.width(Integer.toString(container.getUsedProtectPower())), 64,
                        foregroundColor);

        if(xpRequired>0)
        {
            String xpRequiredTranslation=I18n.get("gui.territory.xp_required");
            font.draw(stack,xpRequiredTranslation,160,79, foregroundColor);
            if(xpRequired>container.getPlayerLevel())
            {
                Minecraft.getInstance().textureManager.bindForSetup(xpIconLocation);
                blit(stack,157,89,(Math.min(xpRequired,3)-1)*16,16,16,16,48,32);
                font.draw(stack,Integer.toString(xpRequired),  173,  93,0x8c605d);
            }else {
                Minecraft.getInstance().textureManager.bindForSetup(xpIconLocation);
                blit(stack,157,89,(Math.min(xpRequired,3)-1)*16,0,16,16,48,32);
                font.draw(stack,Integer.toString(xpRequired),  173,  93,0xc8ff8f);
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

    private void drawMap(PoseStack matrix) {
        getMinecraft().getTextureManager().bindForSetup(container.mapLocation);
        blit(matrix,mapPosLeft, mapPosTop, 0, 0, mapSizeX, mapSizeY,mapSizeX,mapSizeY);
    }

    private void drawChunkOverlay(PoseStack matrix, int mouseX, int mouseY) {

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value);

        getMinecraft().getTextureManager().bindForSetup(territorySquareLocation);

        container.territories.forEach(pos -> {
            int chunkX = pos.x;
            int chunkZ = pos.z;
            blit(matrix,mapPosLeft + ((chunkX - container.mapLeftTopChunkPos.x) << 4), mapPosTop + ((chunkZ - container.mapLeftTopChunkPos.z) << 4), 0, 0, 16
                    , 16, 16,
                 16);
        });


        if (container.getUsedProtectPower() < container.getTotalProtectPower()) {
            getMinecraft().getTextureManager().bindForSetup(expandSquareLocation);
            // 绘制可以选择的新区块
            drawOverlayByCollection(matrix,container.selectableChunkPos);
        }

        getMinecraft().getTextureManager().bindForSetup(edgeSquareLocation);
        drawOverlayByCollection(matrix,container.removableChunkPos);

        // 绘制鼠标悬浮的区
        int mouseOnChunkX = (mouseX - this.parent.getGuiLeft() - mapPosLeft) / 16;
        int mouseOnChunkY = (mouseY - this.parent.getGuiTop() - mapPosTop) / 16;

        getMinecraft().getTextureManager().bindForSetup(mouseOnSquareLocation);
        ChunkPos mouseOverPos = new ChunkPos(container.mapLeftTopChunkPos.x + mouseOnChunkX, container.mapLeftTopChunkPos.z + mouseOnChunkY);
        if (getContainer().removableChunkPos.contains(
                mouseOverPos) || (container.getUsedProtectPower() < getContainer().getTotalProtectPower() && getContainer().selectableChunkPos.contains(
                mouseOverPos)) && mouseOnChunkX >= 0 && mouseOnChunkX < chunkNumX && mouseOnChunkY >= 0 && mouseOnChunkY < chunkNumY) {

            blit(matrix,mapPosLeft + (mouseOnChunkX << 4), mapPosTop + (mouseOnChunkY << 4), 0, 0, 16, 16, 16, 16);
        }
        getMinecraft().getTextureManager().bindForSetup(forbiddenSquareLocation);
        drawOverlayByCollection(matrix,container.forbiddenChunkPos.stream().filter(
                t -> t.x >= container.mapLeftTopChunkPos.x && t.x <= container.mapLeftTopChunkPos.x + mapSizeX / 16 && t.z >= container.mapLeftTopChunkPos.z && t.z <= container.mapLeftTopChunkPos.z + mapSizeY / 16).collect(
                Collectors.toList()));

    }

    private void drawOverlayByCollection(PoseStack matrix,Collection<ChunkPos> collection) {
        for (ChunkPos pos : collection) {
            int posX = pos.x - container.mapLeftTopChunkPos.x;
            int posZ = pos.z - container.mapLeftTopChunkPos.z;
            if (posX < 0 || posZ < 0 || posX >= chunkNumX || posZ >= chunkNumY) continue;

            blit(matrix,mapPosLeft + (posX << 4), mapPosTop + (posZ << 4), 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public void tick() {
        super.tick();
        container.territoryInfo.territoryName = territoryNameTextField.getValue();
        xpRequired = container.getXpRequired();
        doneButton.active= xpRequired<=container.getPlayerLevel();
    }
}
