package top.leonx.territory.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractScreenPage<T extends AbstractContainerMenu> extends Screen {
    protected final List<Widget> buttons = Lists.newArrayList();
    protected T container;
    AbstractContainerScreen<T> parent;
    Consumer<Integer> changePageNum;
    public AbstractScreenPage(T container, AbstractContainerScreen<T> parent, Consumer<Integer> changePage)
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
    public List<? extends GuiEventListener> children() {
        return this.children;
    }
    @SuppressWarnings("deprecation")
    public void render(PoseStack matrix, final int mouseX, final int mouseY, final float partialTicks){


        renderInternal(matrix,mouseX, mouseY, partialTicks);
        for (Widget button : this.buttons) {
            button.render(matrix,mouseX, mouseY, partialTicks);
        }

    }
    public abstract void renderInternal(PoseStack matrix,final int mouseX, final int mouseY, final float partialTicks);
    public abstract void drawGuiContainerForegroundLayer(PoseStack matrix,int mouseX, int mouseY);
    public abstract void init();
    @Nonnull
    protected <B extends AbstractWidget> B addButton(@Nonnull B p_addButton_1_) {
        this.buttons.add(p_addButton_1_);
        this.children.add(p_addButton_1_);
        return p_addButton_1_;
    }
    protected void NavigateTo(int page)
    {
        changePageNum.accept(page);
    }
}
