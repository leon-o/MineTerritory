package top.leonx.territory.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.client.gui.GuiModList;
import net.minecraftforge.fml.client.gui.GuiSlotModList;

import java.util.UUID;

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
    protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
    }

    FontRenderer getFontRenderer()
    {
        return font;
    }
    public class PlayerEntry extends ExtendedList.AbstractListEntry<PlayerEntry> {

        private final UUID uuid;
        private final String name;
        private final PlayerList parent;
        public String getName()
        {return name;}
        public UUID getUUID()
        {
            return uuid;
        }
        public PlayerEntry(UUID uuid, PlayerList parent)
        {
            this(uuid,UsernameCache.getLastKnownUsername(uuid),parent);
        }
        public PlayerEntry(UUID uuid,String name,PlayerList parent)
        {
            this.uuid=uuid;
            this.name=name;
            this.parent=parent;
        }

        @Override
        public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
            FontRenderer font = this.parent.getFontRenderer();
            font.drawString(font.trimStringToWidth(name, entryWidth),left + 3, top + 2, 0xFFFFFF);
//            font.drawString(font.trimStringToWidth("giao", entryWidth), left + 3 , top + 2 + font.FONT_HEIGHT,
//                    0xCCCCCC);
        }
        @Override
        public boolean mouseClicked(double x, double y, int btn)
        {
            parent.setSelected(this);
            PlayerList.this.setSelected(this);
            return false;
        }
    }
}
