package top.leonx.territory.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RenderUtil {
    private static BufferBuilder buffer;
    private static final Tessellator tessellator = Tessellator.getInstance();
    public static void startDraw()
    {
        buffer=tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_LIGHT);
    }
    public static void endDraw()
    {
        tessellator.draw();
    }
    public static Vec3d sub(Vec3d a, Vec3d b){
        return new Vec3d(a.x-b.x,a.y-b.y,a.z-b.z);
    }
    public static void drawWall(Vec3d from, Vec3d to, double height, Vec3d rgb, float alpha, int skyL, int blockL)
    {
        drawDoubleSidePlane(from,sub(to,from),new Vec3d(0,height,0),rgb,alpha,skyL,blockL);
    }
    public static void drawWall(Vec3d from, Vec3d to, double height, int minU, int minV, int maxU, int maxV, Vec3d rgb, float alpha, int skyL, int blockL)
    {
        drawDoubleSidePlane(from,sub(to,from),new Vec3d(0,height,0),minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
    }
    public static void drawDoubleSidePlane(Vec3d o,Vec3d l1,Vec3d l2,Vec3d rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(o,l1,l2,0,0,1,1,rgb,alpha,skyL,blockL);
    }

    public static Vec3d crossProduct(Vec3d a,Vec3d b){
        return new Vec3d(a.y*b.z-b.y*a.z,a.z*b.x-b.z*a.x,a.x*b.y-b.x*a.y);
    }

    /*public static Vec3d normalize(Vec3d v){
       double l= Math.sqrt(v.x*v.x+v.y*v.y+v.z*v.z);
       return new Vec3d(v.x/l,v.y/l,v.z/l);
    }

    public static Vec3d scale(Vec3d v,double f){
        return new Vec3d(v.x*f,v.y*f,v.z*f);
    }*/

    public static void drawDoubleSidePlane(Vec3d o,Vec3d l1,Vec3d l2,int minU,int minV,int maxU,int maxV,Vec3d rgb,float alpha,int skyL, int blockL)
    {
        Vec3d n = l1.crossProduct(l2).normalize().multiply(0.2);

        drawPlane(o.add(n),l1,l2,minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
        drawPlane(o.subtract(n),l2,l1,minV,minU,maxV,maxU,rgb,alpha,skyL,blockL);
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
        Vec3d nf=new Vec3d(normal.x,normal.y,normal.z);
        buffer.vertex(o.x,o.y,o.z).color(r,g,b,alpha).texture(minU,minV).light(skyL, blockL).next();
        buffer.vertex(o.x+l1.x,o.y+l1.y,o.z+l1.z).color(r,g,b,alpha).texture(minU,maxV).light(skyL, blockL).next();
        buffer.vertex(o.x+l1.x+l2.x,o.y+l1.y+l2.y,o.z+l1.z+l2.z).color(r,g,b,alpha).light(maxU,maxV).light(skyL, blockL).next();
        buffer.vertex(o.x+l2.x,o.y+l2.y,o.z+l2.z).color(r,g,b,alpha).texture(maxU,minV).light(skyL, blockL).next();
    }

    public static void enableTextureRepeat() {
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    }

    public static void disableTextureRepeat() {
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
    }
}
