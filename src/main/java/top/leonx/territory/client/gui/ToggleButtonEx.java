package top.leonx.territory.client.gui;

import net.minecraft.client.gui.widget.ToggleWidget;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ToggleButtonEx extends ToggleWidget {
    @Nullable
    public Consumer<ToggleButtonEx> onTriggered;
    public ToggleButtonEx(int xIn, int yIn, int widthIn, int heightIn, boolean triggered) {
        super(xIn, yIn, widthIn, heightIn, triggered);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setStateTriggered(!this.isStateTriggered());
        if(onTriggered!=null)onTriggered.accept(this);
    }
}
