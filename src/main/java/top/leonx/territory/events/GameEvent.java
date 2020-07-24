package top.leonx.territory.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.util.OutlineRender;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = TerritoryMod.MODID)
public class GameEvent {
    static Random random=new Random();

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        ChunkPos chunkPos=new ChunkPos(event.getPos().getX()>>4,event.getPos().getZ()>>4);
        if (!hasPermission(chunkPos, event.getPlayer(),PermissionFlag.BREAK)) {
            event.setCanceled(true);

            if(!event.getWorld().isRemote) {
                TerritoryInfo data=TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.get(chunkPos);
                OutlineRender.StartRender(data.territories,100);
                event.getPlayer().sendMessage(new StringTextComponent("Not your territory"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        ChunkPos chunkPos=new ChunkPos(event.getPos().getX()>>4,event.getPos().getZ()>>4);
        if (!hasPermission(chunkPos, event.getPlayer(),PermissionFlag.PLACE)) {
            event.setCanceled(true);

            if(!event.getWorld().isRemote)
            {
                TerritoryInfo data=TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.get(chunkPos);
                OutlineRender.StartRender(data.territories,100);
                event.getPlayer().sendMessage(new StringTextComponent("Not your territory"));
            }
        }
    }

    private static boolean hasPermission(ChunkPos pos, PlayerEntity player, PermissionFlag flag)
    {
        if(TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
        {
            TerritoryInfo data=TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.get(pos);
            return data.getOwnerId()==null&&!player.hasPermissionLevel(4) ||
                    data.getOwnerId()!=null && (data.getOwnerId().equals(player.getUniqueID())|| data.permissions.containsKey(player.getUniqueID()) && data.permissions.get(player.getUniqueID()).contain(flag));
        }
        return true;
    }

    @Nullable
    private static TerritoryInfo getTerritoryData(ChunkPos pos)
    {
        if(TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
        {
            return TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.get(pos);
        }
        return null;
    }
    static int timeDelay=0;


//    static ChunkPos lastTickPos=null;//We cant get correct lastTickPos by LivingUpdateEvent
//    @SubscribeEvent
//    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event)
//    {
//
//        if(event.getEntityLiving() instanceof PlayerEntity)
//        {
//            PlayerEntity clientPlayer=(PlayerEntity)event.getEntityLiving();
//
//        }
//    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase!= TickEvent.Phase.END) return;

        PlayerEntity clientPlayer= Minecraft.getInstance().player;
        if(clientPlayer==null) return;

        ChunkPos lastTickPos=new ChunkPos((int) (clientPlayer.lastTickPosX-0.5)>>4,(int) (clientPlayer.lastTickPosZ-0.5)>>4);
        ChunkPos thisTickPos=new ChunkPos((int) (clientPlayer.posX-0.5)>>4,(int) (clientPlayer.posZ-0.5)>>4);
        if(lastTickPos==null)lastTickPos=thisTickPos;
        TerritoryInfo lastTerritoryInfo =getTerritoryData(lastTickPos);
        TerritoryInfo thisTerritoryInfo =getTerritoryData(thisTickPos);

        if((lastTerritoryInfo ==null && thisTerritoryInfo !=null) || (lastTerritoryInfo !=null && thisTerritoryInfo !=null && lastTerritoryInfo.getOwnerId()!= thisTerritoryInfo.getOwnerId()))
        {
            String ownerName;
            if(thisTerritoryInfo.getOwnerId()==null) // exit
            {
                ownerName= "'protected area'";
            }else{
                ownerName= UsernameCache.getLastKnownUsername(thisTerritoryInfo.getOwnerId());
            }
            if(hasPermission(thisTickPos,clientPlayer,PermissionFlag.ENTER))
            {
                clientPlayer.sendMessage(new StringTextComponent(String.format("You have entered %s's territory", ownerName)));
            }else{
                clientPlayer.sendMessage(new StringTextComponent("You are forbidden to enter"));
                Vec3d vec=clientPlayer.getMotion();
                Vec3d vecAfterCollision=lastTickPos.x!=thisTickPos.x?new Vec3d(-vec.x,vec.y,vec.z):new Vec3d(vec.x,vec.y,-vec.z);
                //if(vecAfterCollision.y<1E-2)vecAfterCollision=vecAfterCollision.add(0,0.3,0);
                vecAfterCollision=vecAfterCollision.normalize().scale(MathHelper.clamp(vec.length(),1,10));
                clientPlayer.setMotion(vecAfterCollision);
                clientPlayer.setPosition(clientPlayer.lastTickPosX,clientPlayer.lastTickPosY,clientPlayer.lastTickPosZ);
            }
            OutlineRender.StartRender(thisTerritoryInfo.territories,100);
        }else if(thisTerritoryInfo ==null && lastTerritoryInfo !=null)
        {
            String ownerName;
            if(lastTerritoryInfo.getOwnerId()==null) // exit
            {
                ownerName= "'protected area'";
            }else{
                ownerName= UsernameCache.getLastKnownUsername(lastTerritoryInfo.getOwnerId());
            }

            clientPlayer.sendMessage(new StringTextComponent(String.format("You have exited %s's territory", ownerName)));
        }

        ItemStack heldItem = clientPlayer.getHeldItem(Hand.MAIN_HAND);
        if(heldItem.getItem()== ModItems.TerritoryBlockItem)
        {
            // World world=Minecraft.getInstance().world;
            ChunkPos chunkPos=Minecraft.getInstance().world.getChunkAt(clientPlayer.getPosition()).getPos();
            timeDelay++;
            if(timeDelay==5)
            {
                timeDelay=0;
                for(float i=0;i<16;i+=0.2)
                {
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),
                            clientPlayer.posY,chunkPos.z<<4,0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),
                            clientPlayer.posY,(chunkPos.z<<4)+16,0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4),
                            clientPlayer.posY,(chunkPos.z<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+16,
                            clientPlayer.posY,(chunkPos.z<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),0,0.03,0);
                }
            }
        }else{
            timeDelay=0;
        }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent event)
    {
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        //RenderUtil.drawWall(projectedView,new Vec3d(-10.0001,0,-10.0001),new Vec3d(10.0001,200,10.0001));
        OutlineRender.Render(projectedView,event.getPartialTicks());
        //utlineRender.Render(projectedView.add(new Vec3d(0,2,0)),event.getPartialTicks());
    }
}
