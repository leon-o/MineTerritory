package top.leonx.territory.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
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
    public void setSelected(@Nullable PlayerEntry selected) {
        super.setSelected(selected);
        selected.onSelected.accept(selected);
    }
    private int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        int i = this.getScrollbarPosition();
        int           j             = i + 6;
        Tessellator   tessellator   = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.x0, this.y1, 0.0D).tex((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(this.x1, this.y1, 0.0D).tex((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(this.x1, this.y0, 0.0D).tex((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(this.x0, this.y0, 0.0D).tex((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        tessellator.draw();
        int k = this.getRowLeft();
        int l = this.y0 + 4 - (int)this.getScrollAmount();


        this.renderList(matrixStack, k, l, mouseX, mouseY, partialTicks);

        int k1 = getMaxScroll();
        if (k1 > 0) {
            int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            l1 = MathHelper.clamp(l1, 32, this.y1 - this.y0 - 8);
            int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
            if (i2 < this.y0) {
                i2 = this.y0;
            }

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(i, this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(j, this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(j, this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(i, this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(i, i2 + l1, 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(j, i2 + l1, 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(j, i2, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(i, i2, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(i, i2 + l1 - 1, 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(j - 1, i2 + l1 - 1, 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(j - 1, i2, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(i, i2, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        this.renderDecorations(matrixStack, mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
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
