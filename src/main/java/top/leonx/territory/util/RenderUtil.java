package top.leonx.territory.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class RenderUtil {
    private static       BufferBuilder buffer;
    private static final Tessellator   tessellator = Tessellator.getInstance();
    public static void startDraw()
    {
        buffer=tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
    }
    public static void endDraw()
    {
        tessellator.draw();
    }
    public static void drawWall(Vector3d from, Vector3d to, double height, Vector3d rgb, float alpha, int skyL, int blockL)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vector3d(0,height,0),rgb,alpha,skyL,blockL);
    }
    public static void drawWall(Vector3d from,Vector3d to,double height,int minU,int minV,int maxU,int maxV,Vector3d rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vector3d(0,height,0),minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
    }
    public static void drawDoubleSidePlane(Vector3d o,Vector3d l1,Vector3d l2,Vector3d rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(o,l1,l2,0,0,1,1,rgb,alpha,skyL,blockL);
    }

    public static void drawDoubleSidePlane(Vector3d o,Vector3d l1,Vector3d l2,int minU,int minV,int maxU,int maxV,Vector3d rgb,float alpha,int skyL, int blockL)
    {
        Vector3d n = l1.crossProduct(l2).normalize();
        drawPlane(o.add(n.scale(0.02)),l1,l2,minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
        drawPlane(o.subtract(n.scale(0.02)),l2,l1,minV,minU,maxV,maxU,rgb,alpha,skyL,blockL);
    }

    public static void drawPlane(Vector3d o,Vector3d l1,Vector3d l2,Vector3d rgb,float alpha,int skyL, int blockL)
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
    public static void drawPlane(Vector3d o,Vector3d l1,Vector3d l2,int minU,int minV,int maxU,int maxV,Vector3d col,float alpha,int skyL, int blockL)
    {
        float r=(float)col.x;//(col>>>16)& 0x000000FF;
        float g=(float)col.y;//(col>>>8)& 0x000000FF;
        float b=(float)col.z;//col & 0x000000FF;
        Vector3d normal = l1.crossProduct(l2).normalize();
        Vector3d nf=new Vector3d(normal.x,normal.y,normal.z);
        buffer.pos(o.x,o.y,o.z).color(r,g,b,alpha).tex(minU,minV).lightmap(skyL, blockL).endVertex();
        buffer.pos(o.x+l1.x,o.y+l1.y,o.z+l1.z).color(r,g,b,alpha).tex(minU,maxV).lightmap(skyL, blockL).endVertex();
        buffer.pos(o.x+l1.x+l2.x,o.y+l1.y+l2.y,o.z+l1.z+l2.z).color(r,g,b,alpha).tex(maxU,maxV).lightmap(skyL, blockL).endVertex();
        buffer.pos(o.x+l2.x,o.y+l2.y,o.z+l2.z).color(r,g,b,alpha).tex(maxU,minV).lightmap(skyL, blockL).endVertex();
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
