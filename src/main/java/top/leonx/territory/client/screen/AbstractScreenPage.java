package top.leonx.territory.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractScreenPage<T extends Container> extends Screen {
    protected final List<Widget> buttons = Lists.newArrayList();
    protected T container;
    ContainerScreen<T> parent;
    Consumer<Integer> changePageNum;
    public AbstractScreenPage(T container, ContainerScreen<T> parent, Consumer<Integer> changePage)
    {
        super(parent.getTitle());
        this.parent=parent;
        this.changePageNum =changePage;
        this.container=container;
    }

    public T getContainer()
    {
        return container;
    }
    @Nonnull
    public List<? extends IGuiEventListener> children() {
        return this.children;
    }
    public void render(final int mouseX, final int mouseY, final float partialTicks){
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();

        renderInternal(mouseX, mouseY, partialTicks);
        for (Widget button : this.buttons) {
            button.render(mouseX, mouseY, partialTicks);
        }

        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
        RenderHelper.enableStandardItemLighting();
    }
    public abstract void renderInternal(final int mouseX, final int mouseY, final float partialTicks);
    public abstract void drawGuiContainerForegroundLayer(int mouseX, int mouseY);
    public abstract void init();
    @Nonnull
    protected <B extends Widget> B addButton(@Nonnull B p_addButton_1_) {
        this.buttons.add(p_addButton_1_);
        this.children.add(p_addButton_1_);
        return p_addButton_1_;
    }
    protected void NavigateTo(int page)
    {
        changePageNum.accept(page);
    }
}
