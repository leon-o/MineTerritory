package top.leonx.territory.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
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

        Vec3[] relativeEdged={new Vec3(0,0,0),new Vec3(0,0,16),new Vec3(16,0,16),new Vec3(16,0,0),new Vec3(0,0,0)};

        territories.forEach(t->{
            Vec3 originPos=new Vec3(t.x<<4,0,t.z<<4);

            for(int i=0;i<4;i++)
            {
                EdgeEntry entry=new EdgeEntry(originPos.add(relativeEdged[i]),originPos.add(relativeEdged[i+1]));
                if(edges.remove(entry)==null)
                    edges.put(entry,0);
            }
        });
    }

    public static void Render(PoseStack stack, Vec3 viewPos, float pitch, float yaw , double partialTick)
    {
        float alpha = (float) Mth.clampedLerp(0, 1, 3 - 2*(usedTime / duration));
        if(alpha<=0) return;
        usedTime+=partialTick;

        Minecraft.getInstance().textureManager.bindForSetup(checkerboardOverlayLocation); //must load once before


        stack.pushPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


        stack.mulPose(Vector3f.XN.rotation(pitch));
        stack.mulPose(Vector3f.YP.rotationDegrees(yaw-180));
        stack.translate(-viewPos.x, -viewPos.y, -viewPos.z);

        RenderUtil.enableTextureRepeat();
        Minecraft.getInstance().textureManager.bindForSetup(checkerboardOverlayLocation);

        RenderUtil.startDraw();
        edges.forEach((t,v)-> RenderUtil.drawWall(t.from,t.to,255,0,0,255,16,new Vec3(1,1,1),alpha,0xF0,0xF0));

        RenderUtil.endDraw();

        RenderUtil.disableTextureRepeat();
        //GlStateManager.enableCull();
        RenderSystem.depthMask(true);
        stack.popPose();
        RenderSystem.disableBlend();
    }

    private static class EdgeEntry
    {
        Vec3 from;
        Vec3 to;
        public EdgeEntry(Vec3 from,Vec3 to)
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
