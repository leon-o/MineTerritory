package top.leonx.territory.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PermissionToggleButton extends Widget {
    @Nullable
    public Consumer<PermissionToggleButton> onTriggered;
    public PermissionToggleButton(int xIn, int yIn, int widthIn, int heightIn, ITextComponent title, boolean triggered) {
        super(xIn, yIn, widthIn, heightIn, title);
        this.stateTriggered = triggered;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setStateTriggered(!this.isStateTriggered());
        if(onTriggered!=null)onTriggered.accept(this);
    }
    protected boolean stateTriggered;


    public void setStateTriggered(boolean triggered) {
        this.stateTriggered = triggered;
    }

    public boolean isStateTriggered() {
        return this.stateTriggered;
    }

    @Override
    protected int getYImage(boolean hover) {
        if(hover)
            return 2;
        else if(this.stateTriggered)
            return 1;
        else
            return 0;
    }

    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTick) {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = this.getYImage(this.isHovered);
            GuiUtils.drawContinuousTexturedBox(WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
            this.renderBg(matrix,mc, mouseX, mouseY);
            int color = 0xa0a0a0;

            if (packedFGColor != 0)
            {
                color = packedFGColor;
            }
            else if (this.stateTriggered)
            {
                color = 0xe0e0e0;
            }
            else if (this.isHovered)
            {
                color = 0xffffa0;
            }

            ITextComponent buttonText = this.getMessage();
            int strWidth = mc.fontRenderer.getStringWidth(buttonText.getString());
            int ellipsisWidth = mc.fontRenderer.getStringWidth("...");

            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                buttonText = new StringTextComponent(mc.fontRenderer.func_238417_a_(buttonText, width - 6 - ellipsisWidth).getString() + "...");

            this.drawCenteredString(matrix,mc.fontRenderer, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }
}
