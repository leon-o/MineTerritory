package top.leonx.territory.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
//    private static final ResourceLocation edgeSquareLocation = new ResourceLocation("territory", "textures/gui" +
//            "/slash_overlay.png");
//    public static  void drawWall(Vec3d player_pos, Vec3d posA, Vec3d posB)
//    {
//        //GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
//        GlStateManager.disableCull();
//        //GlStateManager.disableLighting();
//        //GL11.glDisable(GL11.GL_TEXTURE_2D);
//        GlStateManager.depthMask(false);
////        GL11.glEnable(GL11.GL_BLEND);
////        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.pushMatrix();
//        GlStateManager.translated(-player_pos.x, -player_pos.y, -player_pos.z);
//        Color c = new Color(255, 255, 255, 155);
//        //GlStateManager.color4f(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
//        Minecraft.getInstance().textureManager.bindTexture(edgeSquareLocation);
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferBuilder = tessellator.getBuffer();
//        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
//
//        int skyLight = 0xFFFFF0;
//        int blockLight =  0xFFFFF0;
//
//        int r=c.getRed();
//        int g=c.getGreen();
//        int b=c.getBlue();
//        int a=c.getAlpha();
//        bufferBuilder.pos(posA.x,posA.y,posA.z).tex(0,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posA.x,posB.y,posA.z).tex(0,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posA.x,posB.y,posB.z).tex(1,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posA.x,posA.y,posB.z).tex(1,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//
//        bufferBuilder.pos(posA.x,posA.y,posA.z).tex(0,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posA.x,posB.y,posA.z).tex(0,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posB.x,posB.y,posA.z).tex(1,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posB.x,posA.y,posA.z).tex(1,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//
//        bufferBuilder.pos(posB.x,posA.y,posA.z).tex(0,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posB.x,posB.y,posA.z).tex(0,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posB.x,posB.y,posB.z).tex(1,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posB.x,posA.y,posB.z).tex(1,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//
//        bufferBuilder.pos(posA.x,posA.y,posB.z).tex(0,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posA.x,posB.y,posB.z).tex(0,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posB.x,posB.y,posB.z).tex(1,1).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//        bufferBuilder.pos(posB.x,posA.y,posB.z).tex(1,0).color(r,g,b,a).lightmap(skyLight, blockLight).endVertex();
//
//        tessellator.draw();
//
//        GlStateManager.popMatrix();
//        //GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GlStateManager.depthMask(true);
//        GlStateManager.enableCull();
//        //GL11.glPopAttrib();
//    }

    public static void drawWall(Vec3d from,Vec3d to,double height,Vec3d rgb,float alpha,int skyL, int blockL, BufferBuilder buffer)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vec3d(0,height,0),rgb,alpha,skyL,blockL,buffer);
    }
    public static void drawWall(Vec3d from,Vec3d to,double height,int minU,int minV,int maxU,int maxV,Vec3d rgb,float alpha,int skyL, int blockL,
                                BufferBuilder buffer)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vec3d(0,height,0),minU,minV,maxU,maxV,rgb,alpha,skyL,blockL,buffer);
    }
    public static void drawDoubleSidePlane(Vec3d o,Vec3d l1,Vec3d l2,Vec3d rgb,float alpha,int skyL, int blockL, BufferBuilder buffer)
    {
        drawDoubleSidePlane(o,l1,l2,0,0,1,1,rgb,alpha,skyL,blockL,buffer);
    }

    public static void drawDoubleSidePlane(Vec3d o,Vec3d l1,Vec3d l2,int minU,int minV,int maxU,int maxV,Vec3d rgb,float alpha,int skyL, int blockL,
                                           BufferBuilder buffer)
    {
        Vec3d n = l1.crossProduct(l2).normalize();
        drawPlane(o.add(n.scale(0.01)),l1,l2,minU,minV,maxU,maxV,rgb,alpha,skyL,blockL, buffer);
        drawPlane(o.subtract(n.scale(0.01)),l2,l1,minV,minU,maxV,maxU,rgb,alpha,skyL,blockL, buffer);
    }

    public static void drawPlane(Vec3d o,Vec3d l1,Vec3d l2,Vec3d rgb,float alpha,int skyL, int blockL, BufferBuilder buffer)
    {
        drawPlane(o,l1,l2,0,0,1,1,rgb,alpha,skyL,blockL,buffer);
    }

    /**
     * Draw plane by a origin point and two edges on the screen.
     * @param o origin point
     * @param l1 edge1
     * @param l2 edge2
     * @param col color RGB
     * @param skyL skylight color RGB
     * @param blockL block light color RGB
     * @param buffer Buffer Builder
     */
    // Normal direction = l1 x l2
    public static void drawPlane(Vec3d o,Vec3d l1,Vec3d l2,int minU,int minV,int maxU,int maxV,Vec3d col,float alpha,int skyL, int blockL, BufferBuilder buffer)
    {
        float r=(float) col.x;//(col>>>16)& 0x000000FF;
        float g=(float)col.y;//(col>>>8)& 0x000000FF;
        float b=(float)col.z;//col & 0x000000FF;
        buffer.pos(o.x,o.y,o.z).tex(minU,minV).color(r,g,b,alpha).lightmap(skyL, blockL).endVertex();
        buffer.pos(o.x+l1.x,o.y+l1.y,o.z+l1.z).tex(minU,maxV).color(r,g,b,alpha).lightmap(skyL, blockL).endVertex();
        buffer.pos(o.x+l1.x+l2.x,o.y+l1.y+l2.y,o.z+l1.z+l2.z).tex(maxU,maxV).color(r,g,b,alpha).lightmap(skyL, blockL).endVertex();
        buffer.pos(o.x+l2.x,o.y+l2.y,o.z+l2.z).tex(maxU,minV).color(r,g,b,alpha).lightmap(skyL, blockL).endVertex();
    }

    public static void enableTextureRepeat() {
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    }

    public static void disableTextureRepeat() {
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
    }
}
