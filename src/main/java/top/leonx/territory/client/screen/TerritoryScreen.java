package top.leonx.territory.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import top.leonx.territory.container.TerritoryTableContainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TerritoryScreen extends ContainerScreen<TerritoryTableContainer> {

    private final List<AbstractScreenPage<TerritoryTableContainer>> pages=new ArrayList<>();
    private int pageNumber=0;
    private static final ResourceLocation backgroundLocation = new ResourceLocation("minecraft", "textures/gui" +
            "/demo_background.png");

    public TerritoryScreen(TerritoryTableContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        pages.add(new TerritoryMapScreen(screenContainer,this,this::ChangePage));
        pages.add(new TerritoryPermissionScreen(screenContainer,this,this::ChangePage));
        this.xSize=250;
        //pages.add(new TerritoryPermissionScreen(screenContainer,inv,titleIn));
    }
    public void ChangePage(int num)
    {
        pageNumber=num;
    }
    @Override
    public void init(@Nonnull Minecraft mc, int width, int height) {
        super.init(mc, width, height);
        pages.forEach(t->t.init(mc,width,height));
    }

    @Override
    public void render(MatrixStack stack,int mouseX, int mouseY, float ticks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, ticks);

        pages.get(pageNumber).render(stack,mouseX,mouseY,ticks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack,float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().textureManager.bindTexture(backgroundLocation);
        int startX = this.guiLeft;
        int startY = this.guiTop;
        this.blit(stack,startX, startY, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack,int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(stack,mouseX, mouseY);
        pages.get(pageNumber).drawGuiContainerForegroundLayer(stack,mouseX,mouseY);
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
    public void tick() {
        pages.get(pageNumber).tick();
    }
}
