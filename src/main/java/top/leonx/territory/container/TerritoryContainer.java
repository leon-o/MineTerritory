package top.leonx.territory.container;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryOperationMsg;
import top.leonx.territory.tileentities.TerritoryTileEntity;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class TerritoryContainer extends Container {

    //The pos relative to mapLeftTopChunkPos

    private final PlayerEntity player;
    public final BlockPos tileEntityPos;
    public final ChunkPos tileEntityChunkPos;
    public final TerritoryInfo territoryInfo;
    public final Set<ChunkPos> territories = new HashSet<>();
    private final Set<ChunkPos> originalTerritories=new HashSet<>();
    public final Set<ChunkPos> selectableChunkPos = new HashSet<>();
    public final Set<ChunkPos> removableChunkPos = new HashSet<>();
    public TerritoryContainer(int id, PlayerInventory inventory, PacketBuffer buffer) {
        this(id, inventory, getTileEntity(inventory, buffer));
    }

    public TerritoryContainer(int id, PlayerInventory inventory, TerritoryTileEntity tileEntity) {
        super(ModContainerTypes.TERRITORY_CONTAINER, id);
        this.player=inventory.player;
        this.territoryInfo= tileEntity.getTerritoryInfo().copy();

        tileEntityPos = tileEntity.getPos();
        tileEntityChunkPos=new ChunkPos(tileEntityPos.getX()>>4,tileEntityPos.getZ()>>4);

        territories.addAll(tileEntity.getTerritoryInfo().territories);
        originalTerritories.addAll(tileEntity.getTerritoryInfo().territories);

        if (Objects.requireNonNull(tileEntity.getWorld()).isRemote) {
            initSelectableChunkPos();
            initRemovableChunkPos();
        }
        protectPower = computeProtectPower();
    }

    public int protectPower;

    private static TerritoryTileEntity getTileEntity(PlayerInventory inventory, PacketBuffer buffer) {
        final TileEntity tileAtPos = inventory.player.world.getTileEntity(buffer.readBlockPos());
        return (TerritoryTileEntity) tileAtPos;
    }

    static {
        TerritoryPacketHandler.registerMessage(TerritoryOperationMsg.class, TerritoryOperationMsg::encode,
                TerritoryOperationMsg::decode,
                TerritoryContainer::handler);
    }

    private static void handler(TerritoryOperationMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayerEntity sender = contextSupplier.get().getSender();
            if (sender == null)//client side ,when success
            {
                Minecraft.getInstance().player.closeScreen();
            } else {
                TerritoryContainer container = (TerritoryContainer) sender.openContainer;
                if (!container.updateTileEntityServerSide(sender, msg))return;

                World world=container.player.world;
                BlockState state=world.getBlockState(container.tileEntityPos);
                world.notifyBlockUpdate(container.tileEntityPos, state,state,2); //notify all clients to update.

                TerritoryPacketHandler.CHANNEL.sendTo(msg, sender.connection.netManager,
                        NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }


    //private final TerritoryTileEntity tileEntity; Should avoid directly operating the Tile Entity directly on the client side


    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        return true;
    }

    public boolean updateTileEntityServerSide(ServerPlayerEntity player, TerritoryOperationMsg msg) {
        if (originalTerritories.size() + msg.readyAdd.length - msg.readyRemove.length > protectPower)
            return false;

        TerritoryTileEntity tileEntity= (TerritoryTileEntity) player.world.getTileEntity(tileEntityPos);

        for (ChunkPos pos : msg.readyRemove) {
            tileEntity.removeJurisdiction(pos);
        }

        for (ChunkPos pos : msg.readyAdd) {

            if (!player.isCreative()) {
                if (player.experienceLevel >= 30) {
                    player.addExperienceLevel(-30);
                } else {
                    return false;
                }
            }
            player.world.playSound(player, player.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.BLOCKS, 1F, 1F);

            tileEntity.addTerritory(pos);
        }

        tileEntity.setPermissionAll(msg.permissions);
        tileEntity.getTerritoryInfo().defaultPermission=msg.defaultPermission;
        tileEntity.markDirty();
        return true;
    }

    @Override
    public void onContainerClosed(@Nonnull PlayerEntity playerIn) {

        super.onContainerClosed(playerIn);
    }

    public void Done() {

        ChunkPos[] readyToRemove =
                originalTerritories.stream().filter(t -> !territories.contains(t)).toArray(ChunkPos[]::new);
        ChunkPos[] readyToAdd =
                territories.stream().filter(t -> !originalTerritories.contains(t)).toArray(ChunkPos[]::new);

        TerritoryOperationMsg msg = new TerritoryOperationMsg(readyToAdd,readyToRemove, territoryInfo.permissions,territoryInfo.defaultPermission);

        TerritoryPacketHandler.CHANNEL.sendToServer(msg);
    }


    public void initSelectableChunkPos() {
        selectableChunkPos.clear();
        HashSet<ChunkPos> tmp = new HashSet<>(territories);


        for (ChunkPos pos : tmp) {
            int chunkX = pos.x;
            int chunkZ = pos.z;

            selectableChunkPos.add(new ChunkPos(chunkX + 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ + 1));
            selectableChunkPos.add(new ChunkPos(chunkX - 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ - 1));
        }
        selectableChunkPos.removeIf(territories::contains);
    }

    public void initRemovableChunkPos() {
        removableChunkPos.clear();
        List<ChunkPos> tmp = new ArrayList<>(territories);

        for (ChunkPos pos : tmp) {
            int chunkX = pos.x;
            int chunkZ = pos.z;
            ChunkPos right = new ChunkPos(chunkX + 1, chunkZ);
            ChunkPos up = new ChunkPos(chunkX, chunkZ + 1);
            ChunkPos left = new ChunkPos(chunkX - 1, chunkZ);
            ChunkPos down = new ChunkPos(chunkX, chunkZ - 1);

            List<Boolean> touched = Arrays.asList(territories.contains(left), territories.contains(up), territories.contains(right),
                    territories.contains(down));
            int touchedCount= (int) touched.stream().filter(t->t).count();
            if (touchedCount==4
                    ||touchedCount==2 && (touched.get(0)&&touched.get(2) ||touched.get(1)&&touched.get(3)))
                continue;

            removableChunkPos.add(pos);
        }
        removableChunkPos.remove(tileEntityChunkPos); // Player cant remove the chunkPos where the tileEntity is located.
    }

    @OnlyIn(Dist.CLIENT)
    public int getBlockPower(IWorld world, BlockPos pos) {
        String banner_name = Objects.requireNonNull(world.getBlockState(pos).getBlock().getRegistryName()).getPath();
        return banner_name.contains("banner") ? 1 : 0;
    }

    private int computeProtectPower() {
        int power = 0;
        BlockPos pos = tileEntityPos;
        IWorld world = player.world;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && world.isAirBlock(pos.add(l, 0, k)) && world.isAirBlock(pos.add(l, 1, k))) {
                    power += getBlockPower(world, pos.add(l * 2, 0, k * 2));
                    power += getBlockPower(world, pos.add(l * 2, 1, k * 2));

                    if (l != 0 && k != 0) {
                        power += getBlockPower(world, pos.add(l * 2, 0, k));
                        power += getBlockPower(world, pos.add(l * 2, 1, k));
                        power += getBlockPower(world, pos.add(l, 0, k * 2));
                        power += getBlockPower(world, pos.add(l, 1, k * 2));
                    }
                }
            }
        }

        return power+1;
    }

    public int getTotalProtectPower() {
        return protectPower;
    }

    public int getUsedProtectPower() {
        return territories.size();
    }
}
