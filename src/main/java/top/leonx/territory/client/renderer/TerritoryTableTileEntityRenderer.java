package top.leonx.territory.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;


public class TerritoryTableTileEntityRenderer extends TileEntityRenderer<TerritoryTableTileEntity> {

    private static final ResourceLocation MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation COMPASS_ADDITION = new ResourceLocation("territory","textures/block/compass.png");



    private float EasingLerp(float min, float max, float x) {
        return (float) ((-Math.pow(x - 1, 4) + 1) * (max - min) + min);//POWER 3
    }

    @Override
    public void render(TerritoryTableTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {

        if (tileEntityIn.rise) {
            tileEntityIn.height += 0.06 * partialTicks;
            tileEntityIn.scale += 0.04f * partialTicks;
        } else {
            tileEntityIn.height -= 0.06 * partialTicks;
            tileEntityIn.scale -= 0.04f * partialTicks;
        }
        tileEntityIn.height = MathHelper.clamp(tileEntityIn.height, 0, 1f);
        tileEntityIn.scale = MathHelper.clamp(tileEntityIn.scale, 0, 1f);
        @SuppressWarnings("SuspiciousNameCombination")
        float height = EasingLerp(0.8f, 1.1f, tileEntityIn.height);
        float scale = EasingLerp(1 / 4f, 1 / 2f, tileEntityIn.scale);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x+0.5, y, z+0.5);
        Minecraft.getInstance().textureManager.bindTexture(COMPASS_ADDITION);
        Tessellator       tessellator = Tessellator.getInstance();
        BufferBuilder compassBuffer=tessellator.getBuffer();
        compassBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        compassBuffer.pos(-0.5F, 0.752F,0.5F).tex(0.0F, 1.0F).normal(0,1,0).endVertex();
        compassBuffer.pos(0.5F,0.752F, 0.5F).tex(1.0F, 1.0F).normal(0,1,0).endVertex();
        compassBuffer.pos(0.5F, 0.752F,-0.5F).tex(1.0F, 0.0F).normal(0,1,0).endVertex();
        compassBuffer.pos(-0.5F, 0.752F,-0.5F).tex(0.0F, 0.0F).normal(0,1,0).endVertex();
        tessellator.draw();

        GlStateManager.translatef(0, height, 0);
        float angleLerp=MathHelper.func_219805_h(partialTicks,tileEntityIn.angleLastTick,tileEntityIn.angle);
        GlStateManager.rotatef(90 - angleLerp,0, 1, 0 );

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(scale,scale,scale);
        GlStateManager.rotatef(30*(3*scale-0.5f),1, 0, 0);

        Minecraft.getInstance().textureManager.bindTexture(MAP_BACKGROUND);
        BufferBuilder mapBackBuffer=tessellator.getBuffer();
        mapBackBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        mapBackBuffer.pos(-0.6F, 0.0F,0.6F).tex(0.0F, 1.0F).normal(0,1,0).endVertex();
        mapBackBuffer.pos(0.6F,0.0F, 0.6F).tex(1.0F, 1.0F).normal(0,1,0).endVertex();
        mapBackBuffer.pos(0.6F, 0.0F,-0.6F).tex(1.0F, 0.0F).normal(0,1,0).endVertex();
        mapBackBuffer.pos(-0.6F, 0.0F,-0.6F).tex(0.0F, 0.0F).normal(0,1,0).endVertex();
        tessellator.draw();


        Minecraft.getInstance().textureManager.bindTexture(tileEntityIn.mapLocation);
        BufferBuilder mapBuffer=tessellator.getBuffer();
        mapBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        mapBuffer.pos(-0.55F, 0.002F,0.55F).tex(0.0F, 1.0F).normal(0,1,0).endVertex();
        mapBuffer.pos(0.55F,0.002F, 0.55F).tex(1.0F, 1.0F).normal(0,1,0).endVertex();
        mapBuffer.pos(0.55F, 0.002F,-0.55F).tex(1.0F, 0.0F).normal(0,1,0).endVertex();
        mapBuffer.pos(-0.55F, 0.002F,-0.55F).tex(0.0F, 0.0F).normal(0,1,0).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();

        if(TerritoryConfig.displayOwnerName)
        {
            float fontSize = 1 / 96f;
            GlStateManager.translatef(0, height-0.5f, 1 / 12f);
            GlStateManager.scalef(fontSize, -fontSize, fontSize);

            String owner_string = I18n.format("gui.territory.owner");

            Minecraft.getInstance().fontRenderer.drawString(owner_string, -Minecraft.getInstance().fontRenderer.getStringWidth(owner_string) / 2f, 0,
                                                                   0XFFFFFFFF);

            Minecraft.getInstance().fontRenderer.drawString(tileEntityIn.getOwnerName(), -Minecraft.getInstance().fontRenderer.getStringWidth(tileEntityIn.getOwnerName()) / 2f,
                                                       15f, 0XFFFFFFFF);
        }

        GlStateManager.popMatrix();
    }
}
