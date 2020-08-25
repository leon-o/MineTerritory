package top.leonx.territory.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.resources.I18n;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerList extends ExtendedList<PlayerList.PlayerEntry> {
    private final FontRenderer font;
    public PlayerList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        font=mcIn.fontRenderer;
    }

    @Override
    public int getRowWidth() {
        return width-10;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0+width-10;
    }

    @Override
    protected void renderBackground(MatrixStack matrix) {
        super.renderBackground(matrix);
    }

    @Override
    public void setSelected(@Nullable PlayerEntry selected) {
        super.setSelected(selected);
        selected.onSelected.accept(selected);
    }

    FontRenderer getFontRenderer()
    {
        return font;
    }
    public static class PlayerEntry extends ExtendedList.AbstractListEntry<PlayerEntry> {

        private final UUID uuid;
        private final String name;
        private final PlayerList parent;
        public boolean canDelete=true;
        public String getName()
        {return name;}
        public UUID getUUID()
        {
            return uuid;
        }
        private final Consumer<PlayerEntry> onSelected;
        public PlayerEntry(UUID uuid, PlayerList parent,@Nullable Consumer<PlayerEntry> onSelected)
        {
            this(uuid, UserUtil.getNameByUUID(uuid),parent,onSelected);
        }
        public PlayerEntry(UUID uuid,String name,PlayerList parent,@Nullable Consumer<PlayerEntry> onSelected)
        {
            this.uuid=uuid;
            this.name=name;
            this.parent=parent;
            this.onSelected=onSelected;
        }

        @Override
        public void render(MatrixStack matrix,int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_,
                           float partialTicks) {
            FontRenderer font = this.parent.getFontRenderer();
            if(UserUtil.isDefaultUser(uuid))
                font.drawString(matrix,I18n.format("gui.territory.all_player"),left + 3, top + 2, 0xFFF0F0);
            else
                font.drawString(matrix,name,left + 3, top + 2, 0xFFFFFF);
        }
        @Override
        public boolean mouseClicked(double x, double y, int btn)
        {
            parent.setSelected(this);
            if(onSelected!=null)
                onSelected.accept(this);
            //PlayerList.this.setSelected(this);
            return false;
        }
    }
}
