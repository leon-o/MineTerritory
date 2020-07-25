package top.leonx.territory.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.util.OutlineRender;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Random;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = TerritoryMod.MODID)
public class GameEvent {
    static Random random=new Random();

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        ChunkPos chunkPos=new ChunkPos(event.getPos().getX()>>4,event.getPos().getZ()>>4);
        if (!hasPermission(chunkPos, event.getPlayer(),PermissionFlag.LEFT_CLICK)) {
            event.setCanceled(true);

            if(!event.getWorld().isRemote) {
                SendMessage(event.getPlayer(),new TranslationTextComponent("message.territory.no_permission",
                        new TranslationTextComponent(PermissionFlag.LEFT_CLICK.getTranslationKey())));
            }else{
                TerritoryInfo data=TerritoryMod.TERRITORY_INFO_HASH_MAP.get(chunkPos);
                OutlineRender.StartRender(data.territories,100);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        ChunkPos chunkPos=new ChunkPos(event.getPos().getX()>>4,event.getPos().getZ()>>4);
        if (!hasPermission(chunkPos, event.getPlayer(),PermissionFlag.RIGHT_CLICK)) {
            event.setCanceled(true);

            if(!event.getWorld().isRemote)
            {
                SendMessage(event.getPlayer(),new TranslationTextComponent("message.territory.no_permission",
                        new TranslationTextComponent(PermissionFlag.RIGHT_CLICK.getTranslationKey())));
            }else{
                TerritoryInfo data=TerritoryMod.TERRITORY_INFO_HASH_MAP.get(chunkPos);
                OutlineRender.StartRender(data.territories,100);
            }
        }
    }

    private static boolean hasPermission(ChunkPos pos, PlayerEntity player, PermissionFlag flag)
    {
        if(TerritoryMod.TERRITORY_INFO_HASH_MAP.containsKey(pos))
        {
            TerritoryInfo data=TerritoryMod.TERRITORY_INFO_HASH_MAP.get(pos);
            return data.getOwnerId()==null&&!player.hasPermissionLevel(4) ||
                    data.getOwnerId()!=null &&
                            (data.getOwnerId().equals(player.getUniqueID())||
                            data.permissions.containsKey(player.getUniqueID()) && data.permissions.get(player.getUniqueID()).contain(flag)||
                            !data.permissions.containsKey(player.getUniqueID()) && data.defaultPermission.contain(flag));
        }
        return true;
    }

    @Nullable
    private static TerritoryInfo getTerritoryData(ChunkPos pos)
    {
        if(TerritoryMod.TERRITORY_INFO_HASH_MAP.containsKey(pos))
        {
            return TerritoryMod.TERRITORY_INFO_HASH_MAP.get(pos);
        }
        return null;
    }
    static int timeDelay=0;


    static final double coolDownTime=100;
    static double coolDown;
    private static void SendMessage(PlayerEntity player, ITextComponent component)
    {
        if(coolDown>0) return;
        player.sendMessage(component);
        coolDown=coolDownTime;
    }
    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event)
    {
        coolDown-=1;
    }

    private static ChunkPos lastLookedPos=null;

    @OnlyIn(Dist.CLIENT)
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
            String ownerName = UserUtil.getNameByUUID(thisTerritoryInfo.getOwnerId());
            if(hasPermission(thisTickPos,clientPlayer,PermissionFlag.ENTER))
            {
                SendMessage(clientPlayer,new TranslationTextComponent("message.territory.enter_territory",ownerName));
            }else{
                SendMessage(clientPlayer,new TranslationTextComponent("message.territory.no_permission",
                        new TranslationTextComponent(PermissionFlag.ENTER.getTranslationKey())));
                Vec3d vec=clientPlayer.getMotion();
                Vec3d vecAfterCollision=lastTickPos.x!=thisTickPos.x?new Vec3d(-vec.x,vec.y,vec.z):new Vec3d(vec.x,vec.y,-vec.z);
                vecAfterCollision=vecAfterCollision.normalize().scale(MathHelper.clamp(vec.length(),1,10));
                clientPlayer.setMotion(vecAfterCollision);
                clientPlayer.setPosition(clientPlayer.lastTickPosX,clientPlayer.lastTickPosY,clientPlayer.lastTickPosZ);
            }
            OutlineRender.StartRender(thisTerritoryInfo.territories,100);
        }else if(thisTerritoryInfo ==null && lastTerritoryInfo !=null)
        {
            String ownerName = UserUtil.getNameByUUID(lastTerritoryInfo.getOwnerId());

            SendMessage(clientPlayer,new TranslationTextComponent("message.territory.exit_territory",ownerName));
        }

        ItemStack heldItem = clientPlayer.getHeldItem(Hand.MAIN_HAND);
        if(heldItem.getItem()== ModItems.TerritoryBlockItem)
        {
            ClientPlayerEntity player=Minecraft.getInstance().player;
            BlockRayTraceResult blockRayTraceResult = Minecraft.getInstance().world.rayTraceBlocks(new RayTraceContext(player.getEyePosition(0),
                    player.getEyePosition(0).add(player.getLookVec().scale(4)),
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, player));
            BlockPos pos = blockRayTraceResult.getPos();
            ChunkPos thisChunkPos=new ChunkPos(pos.getX()>>4,pos.getZ()>>4);

            if(!thisChunkPos.equals(lastLookedPos))
            {
                HashSet<ChunkPos> set = new HashSet<>();
                set.add(thisChunkPos);
                OutlineRender.StartRender(set,100);
                lastLookedPos=thisChunkPos;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent event)
    {
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        //RenderUtil.drawWall(projectedView,new Vec3d(-10.0001,0,-10.0001),new Vec3d(10.0001,200,10.0001));
        OutlineRender.Render(projectedView,event.getPartialTicks());
        //utlineRender.Render(projectedView.add(new Vec3d(0,2,0)),event.getPartialTicks());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onPlayerLogOut(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        TerritoryMod.TERRITORY_INFO_HASH_MAP.clear();
    }
}
