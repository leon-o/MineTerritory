package top.leonx.territory.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class BoundaryRender {
    //private static final ResourceLocation testLocation = new ResourceLocation("minecraft", "textures/block" +
    //        "/birch_leaves.png");
    private static final ResourceLocation checkerboardOverlayLocation = new ResourceLocation("territory", "textures/gui" +
            "/checkerboard_overlay.png");
    //private static final ResourceLocation edgeSquareLocation = new ResourceLocation("territory", "textures/gui" +
    //        "/slash_overlay.png");
    static double duration=0;
    static double usedTime=0;
    static ConcurrentHashMap<EdgeEntry,Integer> edges=new ConcurrentHashMap<>();
    public static void StartRender(Set<ChunkPos> territories, double time)
    {
        if(territories==null) return;
        if(time==0)duration=Double.MAX_VALUE;
        else
        {duration=time;usedTime=0;}

        edges.clear();
        
        Vector3d[] relativeEdged={new Vector3d(0,0,0),new Vector3d(0,0,16),new Vector3d(16,0,16),new Vector3d(16,0,0),new Vector3d(0,0,0)};

        territories.forEach(t->{
            Vector3d originPos=new Vector3d(t.x<<4,0,t.z<<4);

            for(int i=0;i<4;i++)
            {
                EdgeEntry entry=new EdgeEntry(originPos.add(relativeEdged[i]),originPos.add(relativeEdged[i+1]));
                if(edges.remove(entry)==null)
                    edges.put(entry,0);
            }
        });
    }

    public static void Render(Vector3d viewPos,float pitch,float yaw ,double partialTick)
    {
        float alpha = (float) MathHelper.clampedLerp(0, 1, 3 - 2*(usedTime / duration));
        if(alpha<=0) return;
        usedTime+=partialTick;

        Minecraft.getInstance().textureManager.bindTexture(checkerboardOverlayLocation); //must load once before


        RenderSystem.pushMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


        RenderSystem.rotatef(pitch,1,0,0);
        RenderSystem.rotatef(yaw-180,0,1,0);
        RenderSystem.translated(-viewPos.x, -viewPos.y, -viewPos.z);

        RenderUtil.enableTextureRepeat();
        Minecraft.getInstance().textureManager.bindTexture(checkerboardOverlayLocation);

        RenderUtil.startDraw();
        edges.forEach((t,v)-> RenderUtil.drawWall(t.from,t.to,255,0,0,255,16,new Vector3d(1,1,1),alpha,0xF0,0xF0));

        RenderUtil.endDraw();

        RenderUtil.disableTextureRepeat();
        //GlStateManager.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();
        RenderSystem.disableBlend();
    }

    private static class EdgeEntry
    {
        Vector3d from;
        Vector3d to;
        public EdgeEntry(Vector3d from,Vector3d to)
        {
            if(from.x<to.x || from.x==to.x && from.z<to.z)
            {
                this.from=from;
                this.to=to;
            }else{
                this.from=to;
                this.to=from;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if(this==obj)
                return true;
            else if(obj instanceof  EdgeEntry)
            {
                EdgeEntry entry=(EdgeEntry)obj;
                return entry.from.equals(this.from) && entry.to.equals(this.to);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return from.hashCode()*31+to.hashCode();
        }
    }
}
