package top.leonx.territory.util;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
    private static BufferBuilder buffer;
    private static Tessellator           tessellator   = Tessellator.getInstance();
    public static void startDraw()
    {

        buffer=tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
    }
    public static void endDraw()
    {
        tessellator.draw();
    }
    public static void drawWall(Vec3d from,Vec3d to,double height,Vec3d rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vec3d(0,height,0),rgb,alpha,skyL,blockL);
    }
    public static void drawWall(Vec3d from,Vec3d to,double height,int minU,int minV,int maxU,int maxV,Vec3d rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vec3d(0,height,0),minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
    }
    public static void drawDoubleSidePlane(Vec3d o,Vec3d l1,Vec3d l2,Vec3d rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(o,l1,l2,0,0,1,1,rgb,alpha,skyL,blockL);
    }

    public static void drawDoubleSidePlane(Vec3d o,Vec3d l1,Vec3d l2,int minU,int minV,int maxU,int maxV,Vec3d rgb,float alpha,int skyL, int blockL)
    {
        Vec3d n = l1.crossProduct(l2).normalize();
        drawPlane(o.add(n.scale(0.02)),l1,l2,minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
        drawPlane(o.subtract(n.scale(0.02)),l2,l1,minV,minU,maxV,maxU,rgb,alpha,skyL,blockL);
    }

    public static void drawPlane(Vec3d o,Vec3d l1,Vec3d l2,Vec3d rgb,float alpha,int skyL, int blockL)
    {
        drawPlane(o,l1,l2,0,0,1,1,rgb,alpha,skyL,blockL);
    }

    /**
     * Draw plane by a origin point and two edges on the screen.
     * @param o origin point
     * @param l1 edge1
     * @param l2 edge2
     * @param col color RGB
     * @param skyL skylight color RGB
     * @param blockL block light color RGB
     */
    // Normal direction = l1 x l2
    public static void drawPlane(Vec3d o,Vec3d l1,Vec3d l2,int minU,int minV,int maxU,int maxV,Vec3d col,float alpha,int skyL, int blockL)
    {
        float r=(float)col.x;//(col>>>16)& 0x000000FF;
        float g=(float)col.y;//(col>>>8)& 0x000000FF;
        float b=(float)col.z;//col & 0x000000FF;
        Vec3d normal = l1.crossProduct(l2).normalize();
        Vector3f nf=new Vector3f((float) normal.x,(float)normal.y,(float)normal.z);
        buffer.pos(o.x,o.y,o.z).tex(minU,minV).lightmap(skyL, blockL).color(r,g,b,alpha).endVertex();
        buffer.pos(o.x+l1.x,o.y+l1.y,o.z+l1.z).tex(minU,maxV).lightmap(skyL, blockL).color(r,g,b,alpha).endVertex();
        buffer.pos(o.x+l1.x+l2.x,o.y+l1.y+l2.y,o.z+l1.z+l2.z).tex(maxU,maxV).lightmap(skyL, blockL).color(r,g,b,alpha).endVertex();
        buffer.pos(o.x+l2.x,o.y+l2.y,o.z+l2.z).tex(maxU,minV).lightmap(skyL, blockL).color(r,g,b,alpha).endVertex();
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
