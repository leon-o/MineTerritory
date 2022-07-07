package top.leonx.territory.client.gui;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class CheckBoxButtonEx extends Checkbox {
    public CheckBoxButtonEx(int left, int top, int width, int height, Component text, boolean checked) {
        super(left, top, width, height, text, checked);
    }
    public Consumer<CheckBoxButtonEx> onCheckedChange;
    @Override
    public void onPress() {
        super.onPress();
        if(onCheckedChange!=null)
            onCheckedChange.accept(this);
    }
    public void setIsChecked(boolean checked)
    {
        if(checked^selected())
            super.onPress();
    }
}
