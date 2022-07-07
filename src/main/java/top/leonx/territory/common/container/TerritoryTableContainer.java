package top.leonx.territory.common.container;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import top.leonx.territory.common.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.core.PermissionFlag;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.init.config.TerritoryConfig;
import top.leonx.territory.init.handler.TerritoryPacketHandler;
import top.leonx.territory.init.registry.ModCaps;
import top.leonx.territory.init.registry.ModMenuTypes;
import top.leonx.territory.util.MessageUtil;
import top.leonx.territory.util.TerritoryUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class TerritoryTableContainer extends AbstractContainerMenu {

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
    private final Player player;
    private final Set<ChunkPos> originalTerritories = new HashSet<>();
    public ResourceLocation mapLocation;
    public ChunkPos mapLeftTopChunkPos;
    public int protectPower;

    public TerritoryTableContainer(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getTileEntity(inventory, buffer));
    }

    public TerritoryTableContainer(int id, Inventory inventory, TerritoryTableTileEntity tileEntity) {
        super(ModMenuTypes.TERRITORY_CONTAINER.get(), id);
        this.player = inventory.player;
        this.territoryInfo = tileEntity.getTerritoryInfo().copy();

        tileEntityPos = tileEntity.getBlockPos();
        tileEntityChunkPos = new ChunkPos(tileEntityPos.getX() >> 4, tileEntityPos.getZ() >> 4);
        mapLeftTopChunkPos = new ChunkPos((tileEntityPos.getX() >> 4) - 4, (tileEntityPos.getZ() >> 4) - 4);
        territories.addAll(tileEntity.territories);
        originalTerritories.addAll(tileEntity.territories);

        if (Objects.requireNonNull(tileEntity.getLevel()).isClientSide) {

            for (int x = mapLeftTopChunkPos.x; x < mapLeftTopChunkPos.x + 9; x++) {
                for (int z = mapLeftTopChunkPos.z; z < mapLeftTopChunkPos.z + 9; z++) {
                    if(territories.contains(new ChunkPos(x,z))) continue;
                    LevelChunk chunk = tileEntity.getLevel().getChunk(x, z);

                    TerritoryInfo info = chunk.getCapability(ModCaps.TERRITORY_INFO_CAPABILITY).orElse(new TerritoryInfo());
                    if (info.IsProtected() && !info.equals(territoryInfo))
                        forbiddenChunkPos.add(new ChunkPos(x, z));
                }
            }

            initChunkInfo();
        }
        protectPower = tileEntity.computeProtectPower();
        if(FMLEnvironment.dist== Dist.CLIENT)
            mapLocation=tileEntity.mapLocation;
    }

    private static TerritoryTableTileEntity getTileEntity(Inventory inventory, FriendlyByteBuf buffer) {
        final var tileAtPos = inventory.player.level.getBlockEntity(buffer.readBlockPos());
        return (TerritoryTableTileEntity) tileAtPos;
    }

    private static void handler(TerritoryOperationMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer sender = contextSupplier.get().getSender();
            if (sender == null && Minecraft.getInstance().player != null)//client side ,when success
            {
                Minecraft.getInstance().player.closeContainer();
            } else {
                TerritoryTableContainer container = (TerritoryTableContainer) sender.containerMenu;
                if (!container.updateTileEntityServerSide(sender, msg)) return;

                var world = container.player.level;
                BlockState state = world.getBlockState(container.tileEntityPos);
                world.sendBlockUpdated(container.tileEntityPos, state, state, 2); //notify all clients to update.

                TerritoryPacketHandler.CHANNEL.sendTo(msg, sender.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }


    //private final TerritoryTileEntity tileEntity; Should avoid directly operating the Tile Entity directly on the client side


    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    public boolean updateTileEntityServerSide(ServerPlayer player, TerritoryOperationMsg msg) {
        if (originalTerritories.size() + msg.readyAdd.length - msg.readyRemove.length > protectPower)
            return false;

        TerritoryTableTileEntity tileEntity = (TerritoryTableTileEntity) player.level.getBlockEntity(tileEntityPos);
        if (!player.isCreative()) {
            int experienceNeed = (int) Math.round((msg.readyAdd.length-msg.readyRemove.length)* TerritoryConfig.expNeededPerChunk);
            if (player.experienceLevel >= experienceNeed) {
                player.giveExperienceLevels(-experienceNeed);
            } else {
                player.sendMessage(new TranslatableComponent("message.territory.need_experience", Integer.toString(experienceNeed)).setStyle(MessageUtil.YELLOW),
                                   Util.NIL_UUID);
                return false;
            }
        }
        player.level.playSound(null, player.getOnPos(), SoundEvents.ENCHANTMENT_TABLE_USE,
                               SoundSource.BLOCKS, 2F, 1F);
        for (ChunkPos pos : msg.readyRemove) {
            tileEntity.territories.remove(pos);
        }

        Collections.addAll(tileEntity.territories, msg.readyAdd);

        tileEntity.getTerritoryInfo().permissions = msg.permissions;
        tileEntity.getTerritoryInfo().defaultPermission = msg.defaultPermission;
        tileEntity.getTerritoryInfo().territoryName = msg.territoryName;
        tileEntity.setChanged();
        tileEntity.updateTerritoryToWorld();
        return true;
    }
    public int getXpRequired()
    {
        if (!player.isCreative()) {
            return  (int) Math.round((territories.size()- originalTerritories.size())* TerritoryConfig.expNeededPerChunk);
        }
        return 0;
    }
    public int getPlayerLevel(){return player.experienceLevel;}

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
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
        removableChunkPos.removeAll(TerritoryUtil.computeCutChunk(tileEntityChunkPos, territories));
        removableChunkPos.remove(tileEntityChunkPos); // Player cant remove the chunkPos where the tileEntity is located.
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

        public static void encode(TerritoryOperationMsg msg, FriendlyByteBuf buffer) {
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
                buffer.writeUUID(k);
                buffer.writeInt(v.getCode());
            });
            buffer.writeInt(msg.defaultPermission.getCode());
            buffer.writeUtf(msg.territoryName);
        }

        public static TerritoryOperationMsg decode(FriendlyByteBuf buffer) {
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
                UUID uuid = buffer.readUUID();
                int code = buffer.readInt();
                PermissionFlag flag = new PermissionFlag(code);
                permissions.put(uuid, flag);
            }
            int defaultPermissionCode = buffer.readInt();
            String territoryName = buffer.readUtf(16);
            return new TerritoryOperationMsg(territoryName, readyAdd, readyRemove, permissions, new PermissionFlag(defaultPermissionCode));
        }
    }
}
