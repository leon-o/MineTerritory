package top.leonx.territory.events;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.capability.ModCapabilities;
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
        PermissionFlag doWhat=PermissionFlag.BREAK_BLOCK;
        TerritoryInfo info = getTerritoryData(event.getPlayer().world, chunkPos);
        boolean canDo=hasPermission(info,event.getPlayer(),doWhat);
        if (!canDo) {
            event.setCanceled(true);
            notifyPlayer(info, event.getPlayer(), doWhat,event.getWorld().isRemote);

        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        ChunkPos chunkPos=new ChunkPos(event.getPos().getX()>>4,event.getPos().getZ()>>4);
        PermissionFlag doWhat;
        BlockPos pos = event.getPos();
        Block blockType=event.getWorld().getBlockState(pos).getBlock();

        if(blockType instanceof ContainerBlock)
        {
            doWhat=PermissionFlag.USE_CHEST;
        }else if(blockType instanceof DoorBlock || blockType instanceof FenceGateBlock || blockType instanceof TrapDoorBlock){
            doWhat=PermissionFlag.USE_DOOR;
        }else if(event.getItemStack().getItem() instanceof BlockItem) {
            doWhat=PermissionFlag.PLACE_BLOCK;
        }else if(event.getItemStack().getItem()!= Items.AIR){
            doWhat=PermissionFlag.USE_ITEM_ON_BLOCK;
        }else {
            return;
        }
        PlayerEntity player=event.getPlayer();
        TerritoryInfo info = getTerritoryData(player.world, chunkPos);
        boolean canDo=hasPermission(info,event.getPlayer(),doWhat);
        if (!canDo) {
            event.setCanceled(true);
            notifyPlayer(info, event.getPlayer(), doWhat,event.getWorld().isRemote);
        }
    }
    @SubscribeEvent
    public static void onEntityDamageEntity(LivingDamageEvent event)
    {
        Entity entity = event.getSource().getTrueSource();
        if(entity instanceof PlayerEntity)
        {
            PlayerEntity player=(PlayerEntity)entity;
            BlockPos pos=event.getEntityLiving().getPosition();
            ChunkPos chunkPos=new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
            PermissionFlag doWhat = PermissionFlag.ATTACK_ENTITY;
            TerritoryInfo info = getTerritoryData(player.world, chunkPos);
            boolean canDo=hasPermission(info,player,doWhat);
            if (!canDo) {
                event.setCanceled(true);
                notifyPlayer(info, player, doWhat,player.world.isRemote);
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event)
    {
        PlayerEntity player=event.getPlayer();
        Entity target = event.getTarget();
        BlockPos pos=target.getPosition();
        ChunkPos chunkPos=new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
        PermissionFlag doWhat = PermissionFlag.INTERACT_ENTITY;
        TerritoryInfo info = getTerritoryData(player.world, chunkPos);
        boolean canDo=hasPermission(info,player,doWhat);
        if (!canDo) {
            event.setCanceled(true);
            notifyPlayer(info, player, doWhat,player.world.isRemote);
        }
    }
    private static void notifyPlayer(TerritoryInfo info, PlayerEntity player, PermissionFlag flag, boolean clientSide)
    {
        if(!clientSide) {
            SendMessage(player,new TranslationTextComponent("message.territory.no_permission",
                    new TranslationTextComponent(flag.getTranslationKey())));
        }else{
            OutlineRender.StartRender(info.territories,100);
        }
    }
    private static boolean hasPermission(TerritoryInfo info, PlayerEntity player, PermissionFlag flag)
    {
        if(info.IsProtected())
        {
            return  player.hasPermissionLevel(4)||
                    info.getOwnerId()!=null && (info.getOwnerId().equals(player.getUniqueID())||
                            info.permissions.containsKey(player.getUniqueID()) && info.permissions.get(player.getUniqueID()).contain(flag)||
                            !info.permissions.containsKey(player.getUniqueID()) && info.defaultPermission.contain(flag));
        }
        return true;
    }

    @Nullable
    private static TerritoryInfo getTerritoryData(World world, ChunkPos pos)
    {
        Chunk chunk = world.getChunk(pos.x, pos.z);
        return chunk.getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY).orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());
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

        TerritoryInfo lastTerritoryInfo =
                clientPlayer.getEntityWorld().getChunk(lastTickPos.x,lastTickPos.z).getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY).orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());
        TerritoryInfo thisTerritoryInfo =
                clientPlayer.getEntityWorld().getChunk(thisTickPos.x,thisTickPos.z).getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY).orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());

        if(thisTerritoryInfo.IsProtected() && (!lastTerritoryInfo.IsProtected() || !lastTerritoryInfo.equals(thisTerritoryInfo)))
        {
            String ownerName = UserUtil.getNameByUUID(thisTerritoryInfo.getOwnerId());
            if(hasPermission(thisTerritoryInfo,clientPlayer,PermissionFlag.ENTER))
            {
                SendMessage(clientPlayer,new TranslationTextComponent("message.territory.enter_territory",ownerName,thisTerritoryInfo.territoryName));
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
        }else if(!thisTerritoryInfo.IsProtected() && lastTerritoryInfo.IsProtected())
        {
            String ownerName = UserUtil.getNameByUUID(lastTerritoryInfo.getOwnerId());
            SendMessage(clientPlayer,new TranslationTextComponent("message.territory.exit_territory",ownerName,lastTerritoryInfo.territoryName));
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
        OutlineRender.Render(projectedView,event.getPartialTicks());
    }

}
