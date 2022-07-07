package top.leonx.territory.init.events;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.TickTask;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.common.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.core.PermissionFlag;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.core.TerritoryInfoHolder;
import top.leonx.territory.init.config.TerritoryConfig;
import top.leonx.territory.init.registry.ModCaps;
import top.leonx.territory.init.registry.ModItems;
import top.leonx.territory.util.BoundaryRender;
import top.leonx.territory.util.DataUtil;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = TerritoryMod.MODID)
public class GameEvent {

    private static final double   coolDownTime  = 100;
    private static final Random   random        = new Random();
    private static final int      timeDelay     = 0;
    private static       double   coolDown;
    private static       ChunkPos lastLookedPos = null;

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ChunkPos       chunkPos = new ChunkPos(event.getPos().getX() >> 4, event.getPos().getZ() >> 4);
        PermissionFlag doWhat   = PermissionFlag.BREAK_BLOCK;
        TerritoryInfo  info     = getTerritoryData(event.getPlayer().level, chunkPos);
        boolean        canDo    = hasPermission(info, event.getPlayer(), doWhat);
        if (!canDo) {
            event.setCanceled(true);
            notifyPlayer(info, event.getPlayer(), doWhat, event.getWorld().isClientSide);

        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ChunkPos chunkPos  = new ChunkPos(event.getPos().getX() >> 4, event.getPos().getZ() >> 4);
        PermissionFlag doWhat;
        BlockPos pos       = event.getPos();
        Block blockType = event.getWorld().getBlockState(pos).getBlock();

        if (blockType instanceof ChestBlock) {
            doWhat = PermissionFlag.USE_CHEST;
        } else if (blockType instanceof DoorBlock || blockType instanceof FenceGateBlock || blockType instanceof TrapDoorBlock) {
            doWhat = PermissionFlag.USE_DOOR;
        } else if (event.getItemStack().getItem() instanceof BlockItem) {
            doWhat = PermissionFlag.PLACE_BLOCK;
        } else if (event.getItemStack().getItem() != Items.AIR) {
            doWhat = PermissionFlag.USE_ITEM_ON_BLOCK;
        } else {
            return;
        }
        Player player = event.getPlayer();
        TerritoryInfo info   = getTerritoryData(player.level, chunkPos);
        boolean       canDo  = hasPermission(info, event.getPlayer(), doWhat);
        if (!canDo) {
            event.setCanceled(true);
            notifyPlayer(info, event.getPlayer(), doWhat, event.getWorld().isClientSide);
        }
    }

    @SubscribeEvent
    public static void onEntityDamageEntity(LivingDamageEvent event) {
        var entity = event.getSource().getDirectEntity();
        if (entity instanceof Player) {

            Player   player   = (Player) entity;
            BlockPos       pos      = event.getEntityLiving().getOnPos();
            ChunkPos       chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            PermissionFlag doWhat   = PermissionFlag.ATTACK_ENTITY;
            TerritoryInfo  info     = getTerritoryData(player.level, chunkPos);
            boolean        canDo    = hasPermission(info, player, doWhat);
            if (!canDo) {
                event.setCanceled(true);
                notifyPlayer(info, player, doWhat, player.level.isClientSide);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        var world = event.getWorld();
        if(world.isClientSide()) return;
        BlockState state = event.getState();
        if(!TerritoryConfig.powerProvider.containsKey(state.getBlock().asItem())) return;

        if (DataUtil.getBlockStateProtectPower(state,world,event.getPos())>0) {
            Set<BlockEntity> tileEntityNear = getBlockEntityNear(world, event.getPos());
            tileEntityNear.forEach(t -> {
                if (t instanceof TerritoryTableTileEntity tileEntity) {
                    tileEntity.getLevel().getServer().doRunTask(new TickTask(1, tileEntity::notifyPowerProviderDestroy));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(event.getWorld().isClientSide()) return;

        BlockState state = event.getPlacedBlock();
        if(!TerritoryConfig.powerProvider.containsKey(state.getBlock().asItem())) return;

        if (DataUtil.getBlockStateProtectPower(state,event.getWorld(),event.getPos())>0) {
            Set<BlockEntity> tileEntityNear = getBlockEntityNear(event.getWorld(), event.getPos());
            tileEntityNear.forEach(t -> {
                if (t instanceof TerritoryTableTileEntity tileEntity) {
                    tileEntity.getLevel().getServer().doRunTask(new TickTask(1, tileEntity::notifyPowerProviderPlace));
                }
            });
        }
    }

    private static Set<BlockEntity> getBlockEntityNear(LevelAccessor world, BlockPos pos) {
            Set<BlockEntity> tileEntities = new HashSet<>();

            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
                    if ((k != 0 || l != 0) && world.isEmptyBlock(pos.offset(l, 0, k)) && world.isEmptyBlock(pos.offset(l, 1, k))) {
                        tileEntities.add(world.getBlockEntity(pos.offset(l * 2, 0, k * 2)));
                        tileEntities.add(world.getBlockEntity(pos.offset(l * 2, 1, k * 2)));

                        if (l != 0 && k != 0) {
                            tileEntities.add(world.getBlockEntity(pos.offset(l * 2, 0, k)));
                            tileEntities.add(world.getBlockEntity(pos.offset(l * 2, 1, k)));
                            tileEntities.add(world.getBlockEntity(pos.offset(l, 0, k * 2)));
                            tileEntities.add(world.getBlockEntity(pos.offset(l, 1, k * 2)));
                        }
                    }
                }
            }

            return tileEntities;
    }

    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player   player   = event.getPlayer();
        var         target   = event.getTarget();
        BlockPos       pos      = target.getOnPos();
        ChunkPos       chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        PermissionFlag doWhat   = PermissionFlag.INTERACT_ENTITY;
        TerritoryInfo  info     = getTerritoryData(player.level, chunkPos);
        boolean        canDo    = hasPermission(info, player, doWhat);
        if (!canDo) {
            event.setCanceled(true);
            notifyPlayer(info, player, doWhat, player.level.isClientSide);
        }
    }

    private static void notifyPlayer(TerritoryInfo info, Player player, PermissionFlag flag, boolean clientSide) {
        if (!clientSide) {
            SendMessage(player, new TranslatableComponent ("message.territory.no_permission", new TranslatableComponent (flag.getTranslationKey())));
        } else if(TerritoryConfig.displayBoundary){
            BoundaryRender.StartRender(TerritoryInfoHolder.get(player.level).getAssociatedTerritory(info), 100);
        }
    }

    private static boolean hasPermission(TerritoryInfo info, Player player, PermissionFlag flag) {
        if (info.IsProtected()) {
            return player.hasPermissions(4) || info.ownerId != null && (info.ownerId.equals(
                    player.getUUID()) || (info.permissions != null && info.permissions.containsKey(player.getUUID()) && info.permissions.get(
                    player.getUUID()).contain(flag)) || info.defaultPermission.contain(flag));
        }
        return true;
    }

    @Nullable
    private static TerritoryInfo getTerritoryData(Level world, ChunkPos pos) {
        var chunk = world.getChunk(pos.x, pos.z);
        return chunk.getCapability(ModCaps.TERRITORY_INFO_CAPABILITY).orElse(new TerritoryInfo());
    }

    private static void SendMessage(Player player, Component component) {
        if (coolDown > 0) return;
        player.sendMessage(component, Util.NIL_UUID);
        coolDown = coolDownTime;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        coolDown = Math.max(0, coolDown - Minecraft.getInstance().getFrameTime());
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;

        Level    world       = clientPlayer.getCommandSenderWorld();
        ChunkPos lastTickPos = new ChunkPos((int) (clientPlayer.xOld - 0.5) >> 4, (int) (clientPlayer.zOld - 0.5) >> 4);
        ChunkPos thisTickPos = new ChunkPos((int) (clientPlayer.getX() - 0.5) >> 4, (int) (clientPlayer.getZ() - 0.5) >> 4);
        if (lastTickPos == null) lastTickPos = thisTickPos;

        TerritoryInfo lastTerritoryInfo = TerritoryInfoHolder.get(world).getChunkTerritoryInfo(lastTickPos);
        TerritoryInfo thisTerritoryInfo = TerritoryInfoHolder.get(world).getChunkTerritoryInfo(thisTickPos);

        if (thisTerritoryInfo.IsProtected() && (!lastTerritoryInfo.IsProtected() || !lastTerritoryInfo.equals(thisTerritoryInfo))) {

            String ownerName = UserUtil.getNameByUUID(thisTerritoryInfo.ownerId);

            if (hasPermission(thisTerritoryInfo, clientPlayer, PermissionFlag.ENTER)) {
                if (UserUtil.DEFAULT_UUID.equals(thisTerritoryInfo.ownerId))
                    SendMessage(clientPlayer, new TranslatableComponent("message.territory.enter_public_territory", thisTerritoryInfo.territoryName));
                else SendMessage(clientPlayer, new TranslatableComponent ("message.territory.enter_territory", ownerName, thisTerritoryInfo.territoryName));

            } else {

                SendMessage(clientPlayer, new TranslatableComponent ("message.territory.no_permission",
                                                                       new TranslatableComponent (PermissionFlag.ENTER.getTranslationKey())));

                Vec3 vec               = clientPlayer.getDeltaMovement();
                Vec3 vecAfterCollision = lastTickPos.x != thisTickPos.x ? new Vec3(-vec.x, vec.y, vec.z) : new Vec3(vec.x, vec.y, -vec.z);
                vecAfterCollision = vecAfterCollision.normalize().scale(Mth.clamp(vec.length(), 1, 10));

                clientPlayer.setDeltaMovement(vecAfterCollision);
                clientPlayer.setPos(clientPlayer.xOld, clientPlayer.yOld, clientPlayer.zOld);
            }
            if(TerritoryConfig.displayBoundary)
                BoundaryRender.StartRender(TerritoryInfoHolder.get(clientPlayer.level).getAssociatedTerritory(thisTerritoryInfo), 100);
        } else if (!thisTerritoryInfo.IsProtected() && lastTerritoryInfo.IsProtected()) {

            if (UserUtil.DEFAULT_UUID.equals(lastTerritoryInfo.ownerId))
                SendMessage(clientPlayer, new TranslatableComponent ("message.territory.exit_public_territory", lastTerritoryInfo.territoryName));
            else {
                String ownerName = UserUtil.getNameByUUID(lastTerritoryInfo.ownerId);
                SendMessage(clientPlayer, new TranslatableComponent ("message.territory.exit_territory", ownerName, lastTerritoryInfo.territoryName));
            }
        }

        ItemStack heldItem = clientPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() == ModItems.TerritoryBlockItem.get()) {
            LocalPlayer player = Minecraft.getInstance().player;
            var blockRayTraceResult = Minecraft.getInstance().level.clip(
                    new ClipContext(player.getEyePosition(0), player.getEyePosition(0).add(player.getLookAngle().scale(4)),
                                        ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
            BlockPos pos          = blockRayTraceResult.getBlockPos();
            ChunkPos thisChunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);

            if (!thisChunkPos.equals(lastLookedPos)) {
                HashSet<ChunkPos> set = new HashSet<>();
                set.add(thisChunkPos);

                lastLookedPos = thisChunkPos;

                if(TerritoryConfig.displayBoundary)
                    BoundaryRender.StartRender(set, 100);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelLastEvent event) {
        if(!TerritoryConfig.displayBoundary)return;
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float pitch         = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
        float yaw           = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
        BoundaryRender.Render(event.getPoseStack(),projectedView, pitch, yaw, event.getPartialTick());
    }

}
