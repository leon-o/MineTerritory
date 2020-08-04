package top.leonx.territory.container;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.capability.ModCapabilities;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class TerritoryTableContainer extends Container {

    static {
        TerritoryPacketHandler.registerMessage(1,TerritoryOperationMsg.class, TerritoryOperationMsg::encode,
                TerritoryOperationMsg::decode,
                TerritoryTableContainer::handler);
    }

    public final BlockPos tileEntityPos;
    public final ChunkPos tileEntityChunkPos;
    public final TerritoryInfo territoryInfo;
    public final Set<ChunkPos> territories = new HashSet<>();
    public final Set<ChunkPos> selectableChunkPos = new HashSet<>();
    public final Set<ChunkPos> removableChunkPos = new HashSet<>();
    public final Set<ChunkPos> forbiddenChunkPos = new HashSet<>();
    private final PlayerEntity player;
    private final Set<ChunkPos> originalTerritories = new HashSet<>();
    public ChunkPos mapLeftTopChunkPos;
    public int protectPower;

    public TerritoryTableContainer(int id, PlayerInventory inventory, PacketBuffer buffer) {
        this(id, inventory, getTileEntity(inventory, buffer));
    }

    public TerritoryTableContainer(int id, PlayerInventory inventory, TerritoryTableTileEntity tileEntity) {
        super(ModContainerTypes.TERRITORY_CONTAINER, id);
        this.player = inventory.player;
        this.territoryInfo = tileEntity.getTerritoryInfo().copy();

        tileEntityPos = tileEntity.getPos();
        tileEntityChunkPos = new ChunkPos(tileEntityPos.getX() >> 4, tileEntityPos.getZ() >> 4);
        mapLeftTopChunkPos = new ChunkPos((tileEntityPos.getX() >> 4) - 4, (tileEntityPos.getZ() >> 4) - 4);
        territories.addAll(tileEntity.territories);
        originalTerritories.addAll(tileEntity.territories);

        if (Objects.requireNonNull(tileEntity.getWorld()).isRemote) {

            for (int x = mapLeftTopChunkPos.x; x < mapLeftTopChunkPos.x + 9; x++) {
                for (int z = mapLeftTopChunkPos.z; z < mapLeftTopChunkPos.z + 9; z++) {
                    if(territories.contains(new ChunkPos(x,z))) continue;
                    Chunk chunk = tileEntity.getWorld().getChunk(x, z);

                    TerritoryInfo info = chunk.getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY).orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());
                    if (info.IsProtected() && !info.equals(territoryInfo))
                        forbiddenChunkPos.add(new ChunkPos(x, z));
                }
            }

            initChunkInfo();
        }
        protectPower = computeProtectPower();
    }

    private static TerritoryTableTileEntity getTileEntity(PlayerInventory inventory, PacketBuffer buffer) {
        final TileEntity tileAtPos = inventory.player.world.getTileEntity(buffer.readBlockPos());
        return (TerritoryTableTileEntity) tileAtPos;
    }

    private static void handler(TerritoryOperationMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayerEntity sender = contextSupplier.get().getSender();
            if (sender == null)//client side ,when success
            {
                Minecraft.getInstance().player.closeScreen();
            } else {
                TerritoryTableContainer container = (TerritoryTableContainer) sender.openContainer;
                if (!container.updateTileEntityServerSide(sender, msg)) return;

                World world = container.player.world;
                BlockState state = world.getBlockState(container.tileEntityPos);
                world.notifyBlockUpdate(container.tileEntityPos, state, state, 2); //notify all clients to update.

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

        TerritoryTableTileEntity tileEntity = (TerritoryTableTileEntity) player.world.getTileEntity(tileEntityPos);
        if (!player.isCreative()) {
            int experienceNeed = msg.readyAdd.length + msg.readyRemove.length;
            if (player.experienceLevel >= experienceNeed) {
                player.addExperienceLevel(-(msg.readyAdd.length + msg.readyRemove.length));
            } else {
                player.sendMessage(new TranslationTextComponent("message.territory.need_experience", Integer.toString(experienceNeed)));
                return false;
            }
        }
        player.world.playSound(player, player.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS, 1F, 1F);
        for (ChunkPos pos : msg.readyRemove) {
            tileEntity.territories.remove(pos);
        }

        Collections.addAll(tileEntity.territories, msg.readyAdd);

        tileEntity.getTerritoryInfo().permissions = msg.permissions;
        tileEntity.getTerritoryInfo().defaultPermission = msg.defaultPermission;
        tileEntity.getTerritoryInfo().territoryName = msg.territoryName;
        tileEntity.markDirty();
        tileEntity.updateTerritoryToWorld();
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

        TerritoryOperationMsg msg = new TerritoryOperationMsg(territoryInfo.territoryName, readyToAdd, readyToRemove,
                territoryInfo.permissions, territoryInfo.defaultPermission);

        TerritoryPacketHandler.CHANNEL.sendToServer(msg);
    }

    public void initChunkInfo() {
        selectableChunkPos.clear();


        for (ChunkPos pos : territories) {
            int chunkX = pos.x;
            int chunkZ = pos.z;

            selectableChunkPos.add(new ChunkPos(chunkX + 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ + 1));
            selectableChunkPos.add(new ChunkPos(chunkX - 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ - 1));
        }
        selectableChunkPos.removeIf(territories::contains);
        selectableChunkPos.removeIf(forbiddenChunkPos::contains);

        removableChunkPos.clear();

        removableChunkPos.addAll(territories);
        removableChunkPos.removeAll(computeCutChunk(tileEntityChunkPos, territories));
        removableChunkPos.remove(tileEntityChunkPos); // Player cant remove the chunkPos where the tileEntity is located.
    }

    // Calculate cut vertex https://www.cnblogs.com/en-heng/p/4002658.html
    private HashSet<ChunkPos> computeCutChunk(ChunkPos center, Collection<ChunkPos> chunks) {
        HashMap<ChunkPos, Boolean> visit = new HashMap<>();
        HashMap<ChunkPos, ChunkPos> parent = new HashMap<>();
        HashMap<ChunkPos, Integer> dfn = new HashMap<>();
        HashMap<ChunkPos, Integer> low = new HashMap<>();
        chunks.forEach(t -> {
            visit.put(t, false);
            parent.put(t, null);
            dfn.put(t, 0);
            low.put(t, 0);
        });
        HashSet<ChunkPos> result = new HashSet<>();
        computeCutChunk(center, chunks, visit, parent, dfn, low, 0, result);
        return result;
    }

    private void computeCutChunk(ChunkPos current, Collection<ChunkPos> chunks, Map<ChunkPos, Boolean> visit, Map<ChunkPos, ChunkPos> parent, Map<ChunkPos,
            Integer> dfn, Map<ChunkPos, Integer> low, int dfsCount, HashSet<ChunkPos> result) {
        dfsCount++;
        int children = 0;
        int chunkX = current.x;
        int chunkZ = current.z;
        ChunkPos right = new ChunkPos(chunkX + 1, chunkZ);
        ChunkPos up = new ChunkPos(chunkX, chunkZ + 1);
        ChunkPos left = new ChunkPos(chunkX - 1, chunkZ);
        ChunkPos down = new ChunkPos(chunkX, chunkZ - 1);
        List<ChunkPos> linked = Arrays.asList(left, up, right, down);
        visit.replace(current, true);
        dfn.replace(current, dfsCount);
        low.replace(current, dfsCount);
        for (ChunkPos pos : linked) {
            if (!chunks.contains(pos)) continue;
            if (!visit.get(pos)) {
                children++;
                parent.replace(pos, current);
                computeCutChunk(pos, chunks, visit, parent, dfn, low, dfsCount, result);
                low.replace(current, Math.min(low.get(pos), low.get(current)));
                if (parent.get(current) == null && children > 1 || parent.get(current) != null && low.get(pos) >= dfn.get(current)) {
                    result.add(current);
                }
            } else if (pos != parent.get(current)) {
                low.replace(current, Math.min(low.get(current), dfn.get(pos)));
            }
        }
    }

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

        return power + 1;
    }

    public int getTotalProtectPower() {
        return protectPower;
    }

    public int getUsedProtectPower() {
        return territories.size();
    }

    public static class TerritoryOperationMsg {
        @Nonnull
        public ChunkPos[] readyAdd;
        @Nonnull
        public ChunkPos[] readyRemove;
        @Nonnull
        public Map<UUID, PermissionFlag> permissions;
        public PermissionFlag defaultPermission;
        public String territoryName;

        public TerritoryOperationMsg(String territoryName, @Nonnull ChunkPos[] readyAdd, @Nonnull ChunkPos[] readyRemove,
                                     @Nonnull Map<UUID, PermissionFlag> permissionFlagMap, PermissionFlag defaultPermission) {
            this.readyAdd = readyAdd;
            this.readyRemove = readyRemove;
            permissions = permissionFlagMap;
            this.defaultPermission = defaultPermission;
            this.territoryName = territoryName;
        }

        public static void encode(TerritoryOperationMsg msg, PacketBuffer buffer) {
            buffer.writeInt(msg.readyAdd.length);
            buffer.writeInt(msg.readyRemove.length);
            buffer.writeInt(msg.permissions.size());
            for (ChunkPos pos : msg.readyAdd) {
                buffer.writeInt(pos.x);
                buffer.writeInt(pos.z);
            }
            for (ChunkPos pos : msg.readyRemove) {
                buffer.writeInt(pos.x);
                buffer.writeInt(pos.z);
            }
            msg.permissions.forEach((k, v) -> {
                buffer.writeUniqueId(k);
                buffer.writeInt(v.getCode());
            });
            buffer.writeInt(msg.defaultPermission.getCode());
            buffer.writeString(msg.territoryName);
        }

        public static TerritoryOperationMsg decode(PacketBuffer buffer) {
            int addLength = buffer.readInt();
            int removeLength = buffer.readInt();
            int permissionLength = buffer.readInt();

            HashMap<UUID, PermissionFlag> permissions = new HashMap<>();
            ChunkPos[] readyAdd = new ChunkPos[addLength];
            ChunkPos[] readyRemove = new ChunkPos[removeLength];

            for (int i = 0; i < readyAdd.length; i++) {
                readyAdd[i] = new ChunkPos(buffer.readInt(), buffer.readInt());
            }
            for (int i = 0; i < readyRemove.length; i++) {
                readyRemove[i] = new ChunkPos(buffer.readInt(), buffer.readInt());
            }
            for (int i = 0; i < permissionLength; i++) {
                UUID uuid = buffer.readUniqueId();
                int code = buffer.readInt();
                PermissionFlag flag = new PermissionFlag(code);
                permissions.put(uuid, flag);
            }
            int defaultPermissionCode = buffer.readInt();
            String territoryName = buffer.readString(16);
            return new TerritoryOperationMsg(territoryName, readyAdd, readyRemove, permissions, new PermissionFlag(defaultPermissionCode));
        }
    }
}
