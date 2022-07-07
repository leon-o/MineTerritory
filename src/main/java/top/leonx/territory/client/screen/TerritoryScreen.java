package top.leonx.territory.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import top.leonx.territory.common.container.TerritoryTableContainer;

import java.util.ArrayList;
import java.util.List;

public class TerritoryScreen extends AbstractContainerScreen<TerritoryTableContainer> {

    private final List<AbstractScreenPage<TerritoryTableContainer>> pages=new ArrayList<>();
    private int pageNumber=0;
    private static final ResourceLocation backgroundLocation = new ResourceLocation("minecraft", "textures/gui" +
            "/demo_background.png");

    public TerritoryScreen(TerritoryTableContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        pages.add(new TerritoryMapScreen(screenContainer,this,this::ChangePage));
        pages.add(new TerritoryPermissionScreen(screenContainer,this,this::ChangePage));
        this.imageWidth =250;
        //pages.add(new TerritoryPermissionScreen(screenContainer,inv,titleIn));
    }
    public void ChangePage(int num)
    {
        pageNumber=num;
    }

    @Override
    protected void init() {
        super.init();
        pages.forEach(t->t.init(minecraft,width,height));
    }


    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float ticks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, ticks);

        pages.get(pageNumber).render(stack,mouseX,mouseY,ticks);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        Minecraft.getInstance().textureManager.bindForSetup(backgroundLocation);
        int startX = this.getGuiLeft();
        int startY = this.getGuiTop();
        this.blit(pPoseStack,startX, startY, 0, 0, this.getXSize(), this.getYSize());
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
//        super.renderTooltip(pPoseStack, pX, pY);
        pages.get(pageNumber).drawGuiContainerForegroundLayer(pPoseStack,pX,pY);

    }


    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        pages.get(pageNumber).mouseClicked(p_mouseClicked_1_,p_mouseClicked_3_,p_mouseClicked_5_);
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }
    @Override
    public boolean mouseDragged(double d1, double d3, int d5, double d6, double d8) {
        return pages.get(pageNumber).mouseDragged(d1,d3,d5,d6,d8);
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        return pages.get(pageNumber).mouseReleased(x,y,btn);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return pages.get(pageNumber).mouseScrolled(x,y,amount);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return pages.get(pageNumber).keyReleased(keyCode,scanCode,modifiers);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        pages.get(pageNumber).keyPressed(p_keyPressed_1_,p_keyPressed_2_,p_keyPressed_3_);
        return true;//super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        return pages.get(pageNumber).charTyped(p_charTyped_1_,p_charTyped_2_);
    }

    @Override
    protected void containerTick() {
        pages.get(pageNumber).tick();
    }


}
