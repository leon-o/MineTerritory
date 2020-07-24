package top.leonx.territory.client.gui;

import net.minecraft.client.gui.widget.button.CheckboxButton;

import java.util.function.Consumer;

public class CheckBoxButtonEx extends CheckboxButton {
    public CheckBoxButtonEx(int left, int top, int width, int height, String text, boolean checked) {
        super(left, top, width, height, text, checked);
    }
    public Consumer<Boolean> onCheckedChange;
    @Override
    public void onPress() {
        super.onPress();
        if(onCheckedChange!=null)
            onCheckedChange.accept(isChecked());
    }
    public void setIsChecked(boolean checked)
    {
        if(checked^isChecked())
            super.onPress();
    }
}
