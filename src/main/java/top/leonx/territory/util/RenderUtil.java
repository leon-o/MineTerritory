package top.leonx.territory.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtil {
    public static  void drawWall(Vec3d player_pos, Vec3d posA, Vec3d posB)
    {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(-player_pos.x, -player_pos.y, -player_pos.z);
        Color c = new Color(255, 215, 0, 155);
        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        bufferBuilder.pos(posA.x,posA.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posA.x,posB.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posA.x,posB.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posA.x,posA.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();

        bufferBuilder.pos(posA.x,posA.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posA.x,posB.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posB.x,posB.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posB.x,posA.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();

        bufferBuilder.pos(posB.x,posA.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posB.x,posB.y,posA.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posB.x,posB.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posB.x,posA.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();

        bufferBuilder.pos(posA.x,posA.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posA.x,posB.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posB.x,posB.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();
        bufferBuilder.pos(posB.x,posA.y,posB.z).color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()).endVertex();

        tessellator.draw();

        GL11.glTranslated(player_pos.x, player_pos.y, player_pos.z);
        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }
    public static void drawBoundingBox(Vec3d player_pos, Vec3d posA, Vec3d posB, boolean smooth, float width) {

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(-player_pos.x, -player_pos.y, -player_pos.z);
        //posA=new Vec3d(posA.x-player_pos.x,posA.y-player_pos.y,posA.z-player_pos.z);
        //posB=new Vec3d(posB.x-player_pos.x,posB.y-player_pos.y,posB.z-player_pos.z);
        Color c = new Color(255, 215, 0, 150);
        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        GL11.glLineWidth(width);
        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_LINE, DefaultVertexFormats.POSITION_COLOR);

        double dx = Math.abs(posA.x - posB.x);
        double dy = Math.abs(posA.y - posB.y);
        double dz = Math.abs(posA.z - posB.z);

        //AB
        bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();          //A
        bufferBuilder.pos(posA.x, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //B
        //BC
        bufferBuilder.pos(posA.x, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //B
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //C
        //CD
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //C
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //D
        //DA
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //D
        bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();          //A
        //EF
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //E
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //F
        //FG
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //F
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex(); //G
        //GH
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex(); //G
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //H
        //HE
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //H
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //E
        //AE
        bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();          //A
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //E
        //BF
        bufferBuilder.pos(posA.x, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //B
        bufferBuilder.pos(posA.x, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //F
        //CG
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //C
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z+dz).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex(); //G
        //DH
        bufferBuilder.pos(posA.x+dx, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();       //D
        bufferBuilder.pos(posA.x+dx, posA.y+dy, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();    //H

        tessellator.draw();

        GL11.glTranslated(player_pos.x, player_pos.y, player_pos.z);
        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }
}
