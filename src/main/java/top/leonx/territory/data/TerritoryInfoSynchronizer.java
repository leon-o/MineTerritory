package top.leonx.territory.data;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.util.UserUtil;

import java.util.*;
import java.util.function.Supplier;

import static top.leonx.territory.capability.ModCapabilities.TERRITORY_INFO_CAPABILITY;

public class TerritoryInfoSynchronizer {
    static {
        TerritoryPacketHandler.registerMessage(UpdateSingleChunkMsg.class, UpdateSingleChunkMsg::encode,
                UpdateSingleChunkMsg::decode,
                TerritoryInfoSynchronizer::handler);
    }

    public static void UpdateInfoToClientTracked(Chunk chunk) {
        TerritoryInfo info = chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
        UpdateInfoToClientTracked(chunk, info);
    }

    public static void UpdateInfoToClientTracked(Chunk chunk, TerritoryInfo info) {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new UpdateSingleChunkMsg(chunk.getPos(), info));
    }

    public static void UpdateDeassignationToClientTracked(Chunk chunk) {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new UpdateSingleChunkMsg(chunk.getPos()));
    }

    public static void UpdateInfoToClientPlayer(Chunk chunk, ServerPlayerEntity player) {
        TerritoryInfo info = chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateSingleChunkMsg(chunk.getPos(), info));
    }
    public static void UpdateInfoToClientPlayer(ChunkPos pos, TerritoryInfo info, ServerPlayerEntity player)
    {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateSingleChunkMsg(pos, info));
    }

    // client handler
    private static void handler(UpdateSingleChunkMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayerEntity sender = contextSupplier.get().getSender();
            if (sender == null)//client side
            {
                PlayerEntity player   = Minecraft.getInstance().player;
                World        world    = player.world;
                ChunkPos     chunkPos = msg.target;

                if (msg.isProtected) {
                    TerritoryInfoHolder.get(world).assignToChunk(chunkPos, msg.ownerId, msg.territoryId, msg.center, msg.territoryName,
                            msg.defaultPermission, msg.permissions);
                } else {
                    TerritoryInfoHolder.get(world).deassignToChunk(chunkPos);
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public static class UpdateSingleChunkMsg {
        public Map<UUID, PermissionFlag> permissions;
        public PermissionFlag            defaultPermission;
        public String                    territoryName;
        public BlockPos                  center;
        public ChunkPos                  target;
        public UUID                      ownerId;
        public UUID                      territoryId;
        public boolean                   isProtected = true;

        public UpdateSingleChunkMsg(ChunkPos pos) {
            this.target = pos;
            this.isProtected = false;
        }

        public UpdateSingleChunkMsg(ChunkPos target, TerritoryInfo info) {
            this.target = target;
            this.isProtected = info.IsProtected();
            this.ownerId = info.ownerId;
            this.center = info.centerPos;
            this.defaultPermission = info.defaultPermission;
            this.permissions = info.permissions;
            this.territoryName = info.territoryName;
            this.territoryId = info.territoryId;
        }

        public UpdateSingleChunkMsg(ChunkPos target, UUID ownerId, UUID territoryId, String territoryName, BlockPos center,
                                    Map<UUID, PermissionFlag> permissionFlagMap, PermissionFlag defaultPermission) {
            permissions = permissionFlagMap;
            this.defaultPermission = defaultPermission;
            this.territoryName = territoryName;
            this.center = center;
            this.target = target;
            this.ownerId = ownerId;
            this.territoryId = territoryId;
        }

        public static void encode(UpdateSingleChunkMsg msg, PacketBuffer buffer) {
            buffer.writeBoolean(msg.isProtected);
            buffer.writeLong(msg.target.asLong());
            if (msg.isProtected) {

                buffer.writeUniqueId(msg.ownerId);
                buffer.writeUniqueId(msg.territoryId);
                buffer.writeString(msg.territoryName);
                buffer.writeInt(msg.defaultPermission.getCode());

                if (!UserUtil.isDefaultUser(msg.ownerId)) //DefaultUser means public territory.There isn't center and permissions.
                {
                    buffer.writeBlockPos(msg.center);

                    buffer.writeInt(msg.permissions.size());
                    msg.permissions.forEach((k, v) -> {
                        buffer.writeUniqueId(k);
                        buffer.writeInt(v.getCode());
                    });
                }
            }

        }

        public static UpdateSingleChunkMsg decode(PacketBuffer buffer) {
            boolean  isProtected = buffer.readBoolean();
            ChunkPos target      = new ChunkPos(buffer.readLong());
            if (isProtected) {

                UUID                          owner                 = buffer.readUniqueId();
                UUID                          territoryId           = buffer.readUniqueId();
                String                        territoryName         = buffer.readString(16);
                BlockPos                      center                = null;
                HashMap<UUID, PermissionFlag> permissions           = null;
                int                           defaultPermissionCode = buffer.readInt();

                if (!UserUtil.isDefaultUser(owner)) {
                    center = buffer.readBlockPos();
                    permissions = new HashMap<>();

                    int permissionLength = buffer.readInt();

                    for (int i = 0; i < permissionLength; i++) {

                        UUID           uuid = buffer.readUniqueId();
                        int            code = buffer.readInt();
                        PermissionFlag flag = new PermissionFlag(code);

                        permissions.put(uuid, flag);
                    }
                }

                return new UpdateSingleChunkMsg(target, owner, territoryId, territoryName, center, permissions, new PermissionFlag(defaultPermissionCode));
            }
            return new UpdateSingleChunkMsg(target);
        }
    }
}
