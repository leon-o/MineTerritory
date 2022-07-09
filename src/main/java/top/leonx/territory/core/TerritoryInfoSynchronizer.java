package top.leonx.territory.core;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import top.leonx.territory.init.handler.TerritoryPacketHandler;
import top.leonx.territory.util.UserUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static top.leonx.territory.init.registry.ModCaps.TERRITORY_INFO_CAPABILITY;

public class TerritoryInfoSynchronizer {
    public static void register()
    {
        if(FMLEnvironment.dist==Dist.CLIENT)
            TerritoryPacketHandler.registerMessage(0, UpdateSingleLevelChunkMsg.class, UpdateSingleLevelChunkMsg::encode,
                    UpdateSingleLevelChunkMsg::decode,
                    TerritoryInfoSynchronizer::handlerClient);
        else
            TerritoryPacketHandler.registerMessage(0, UpdateSingleLevelChunkMsg.class, UpdateSingleLevelChunkMsg::encode,
                    UpdateSingleLevelChunkMsg::decode,
                    TerritoryInfoSynchronizer::handlerServer);
    }

    public static void UpdateInfoToClientTracked(LevelChunk chunk) {
        TerritoryInfo info = chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(new TerritoryInfo());
        UpdateInfoToClientTracked(chunk, info);
    }

    public static void UpdateInfoToClientTracked(LevelChunk chunk, TerritoryInfo info) {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new UpdateSingleLevelChunkMsg(chunk.getPos(), info));
    }

    public static void UpdateDeassignationToClientTracked(LevelChunk chunk) {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new UpdateSingleLevelChunkMsg(chunk.getPos()));
    }

    public static void UpdateInfoToClientPlayer(LevelChunk chunk, ServerPlayer player) {
        TerritoryInfo info = chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(new TerritoryInfo());
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateSingleLevelChunkMsg(chunk.getPos(), info));
    }

    public static void UpdateInfoToClientPlayer(ChunkPos pos, TerritoryInfo info, ServerPlayer player) {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateSingleLevelChunkMsg(pos, info));
    }

    @OnlyIn(Dist.CLIENT)
    // client handler
    private static void handlerClient(UpdateSingleLevelChunkMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() ->{
            var    world    = Minecraft.getInstance().level;
            ChunkPos chunkPos = msg.target;

            if (msg.isProtected) {
                TerritoryInfoHolder.get(world).assignToLevelChunk(chunkPos, msg.ownerId, msg.territoryId, msg.center, msg.territoryName,
                        msg.defaultPermission, msg.permissions);
            } else {
                TerritoryInfoHolder.get(world).deassignToLevelChunk(chunkPos);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private static <T> void handlerServer(T t, Supplier<NetworkEvent.Context> contextSupplier) {
        //DO NOTHING
    }

    public static class UpdateSingleLevelChunkMsg {
        public Map<UUID, PermissionFlag> permissions;
        public PermissionFlag            defaultPermission;
        public String                    territoryName;
        public BlockPos center;
        public ChunkPos                  target;
        public UUID                      ownerId;
        public UUID                      territoryId;
        public boolean                   isProtected = true;

        public UpdateSingleLevelChunkMsg(ChunkPos pos) {
            this.target = pos;
            this.isProtected = false;
        }

        public UpdateSingleLevelChunkMsg(ChunkPos target, TerritoryInfo info) {
            this.target = target;
            this.isProtected = info.IsProtected();
            this.ownerId = info.ownerId;
            this.center = info.centerPos;
            this.defaultPermission = info.defaultPermission;
            this.permissions = info.permissions;
            this.territoryName = info.territoryName;
            this.territoryId = info.territoryId;
        }

        public UpdateSingleLevelChunkMsg(ChunkPos target, UUID ownerId, UUID territoryId, String territoryName, BlockPos center,
                                    Map<UUID, PermissionFlag> permissionFlagMap, PermissionFlag defaultPermission) {
            permissions = permissionFlagMap;
            this.defaultPermission = defaultPermission;
            this.territoryName = territoryName;
            this.center = center;
            this.target = target;
            this.ownerId = ownerId;
            this.territoryId = territoryId;
        }

        public static void encode(UpdateSingleLevelChunkMsg msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.isProtected);
            buffer.writeLong(msg.target.toLong());
            if (msg.isProtected && msg.ownerId != null && msg.territoryId != null && msg.territoryName != null && msg.defaultPermission != null) {

                buffer.writeUUID(msg.ownerId);
                buffer.writeUUID(msg.territoryId);
                buffer.writeUtf(msg.territoryName);
                buffer.writeInt(msg.defaultPermission.getCode());

                if (!UserUtil.isDefaultUser(msg.ownerId)) //DefaultUser means public territory.There isn't center and permissions.
                {
                    buffer.writeBlockPos(msg.center);

                    buffer.writeInt(msg.permissions.size());
                    msg.permissions.forEach((k, v) -> {
                        buffer.writeUUID(k);
                        buffer.writeInt(v.getCode());
                    });
                }
            }

        }

        public static UpdateSingleLevelChunkMsg decode(FriendlyByteBuf buffer) {
            boolean  isProtected = buffer.readBoolean();
            ChunkPos target      = new ChunkPos(buffer.readLong());
            if (isProtected) {

                UUID                          owner                 = buffer.readUUID();
                UUID                          territoryId           = buffer.readUUID();
                String                        territoryName         = buffer.readUtf(16);
                BlockPos                      center                = null;
                HashMap<UUID, PermissionFlag> permissions           = null;
                int                           defaultPermissionCode = buffer.readInt();

                if (!UserUtil.isDefaultUser(owner)) {
                    center = buffer.readBlockPos();
                    permissions = new HashMap<>();

                    int permissionLength = buffer.readInt();

                    for (int i = 0; i < permissionLength; i++) {

                        UUID           uuid = buffer.readUUID();
                        int            code = buffer.readInt();
                        PermissionFlag flag = new PermissionFlag(code);

                        permissions.put(uuid, flag);
                    }
                }

                return new UpdateSingleLevelChunkMsg(target, owner, territoryId, territoryName, center, permissions, new PermissionFlag(defaultPermissionCode));
            }
            return new UpdateSingleLevelChunkMsg(target);
        }
    }
}
