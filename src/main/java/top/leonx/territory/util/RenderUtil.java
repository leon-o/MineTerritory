package top.leonx.territory.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class RenderUtil {
    private static BufferBuilder buffer;
    private static final Tesselator tessellator = Tesselator.getInstance();
    public static void startDraw()
    {
        buffer=tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
    }
    public static void endDraw()
    {
        tessellator.end();
    }
    public static void drawWall(Vec3 from, Vec3 to, double height, Vec3 rgb, float alpha, int skyL, int blockL)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vec3(0,height,0),rgb,alpha,skyL,blockL);
    }
    public static void drawWall(Vec3 from,Vec3 to,double height,int minU,int minV,int maxU,int maxV,Vec3 rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(from,to.subtract(from),new Vec3(0,height,0),minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
    }
    public static void drawDoubleSidePlane(Vec3 o,Vec3 l1,Vec3 l2,Vec3 rgb,float alpha,int skyL, int blockL)
    {
        drawDoubleSidePlane(o,l1,l2,0,0,1,1,rgb,alpha,skyL,blockL);
    }

    public static void drawDoubleSidePlane(Vec3 o,Vec3 l1,Vec3 l2,int minU,int minV,int maxU,int maxV,Vec3 rgb,float alpha,int skyL, int blockL)
    {
        Vec3 n = l1.cross(l2);
        drawPlane(o.add(n.scale(0.02)),l1,l2,minU,minV,maxU,maxV,rgb,alpha,skyL,blockL);
        drawPlane(o.subtract(n.scale(0.02)),l2,l1,minV,minU,maxV,maxU,rgb,alpha,skyL,blockL);
    }

    public static void drawPlane(Vec3 o,Vec3 l1,Vec3 l2,Vec3 rgb,float alpha,int skyL, int blockL)
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
    public static void drawPlane(Vec3 o,Vec3 l1,Vec3 l2,int minU,int minV,int maxU,int maxV,Vec3 col,float alpha,int skyL, int blockL)
    {
        float r=(float)col.x;//(col>>>16)& 0x000000FF;
        float g=(float)col.y;//(col>>>8)& 0x000000FF;
        float b=(float)col.z;//col & 0x000000FF;
        Vec3 normal = l1.cross(l2).normalize();
        Vec3 nf=new Vec3(normal.x,normal.y,normal.z);
        buffer.vertex(o.x,o.y,o.z).color(r,g,b,alpha).uv(minU,minV).uv2(skyL, blockL).endVertex();
        buffer.vertex(o.x+l1.x,o.y+l1.y,o.z+l1.z).color(r,g,b,alpha).overlayCoords(minU,maxV).uv2(skyL, blockL).endVertex();
        buffer.vertex(o.x+l1.x+l2.x,o.y+l1.y+l2.y,o.z+l1.z+l2.z).color(r,g,b,alpha).uv(maxU,maxV).uv2(skyL, blockL).endVertex();
        buffer.vertex(o.x+l2.x,o.y+l2.y,o.z+l2.z).color(r,g,b,alpha).uv(maxU,minV).uv2(skyL, blockL).endVertex();
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
