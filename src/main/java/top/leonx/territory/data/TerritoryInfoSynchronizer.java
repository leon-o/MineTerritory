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
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.TerritoryPacketHandler;

import java.util.*;
import java.util.function.Supplier;

import static top.leonx.territory.capability.ModCapabilities.TERRITORY_INFO_CAPABILITY;

public class TerritoryInfoSynchronizer {
    static {
        TerritoryPacketHandler.registerMessage(UpdateSingleChunkMsg.class, UpdateSingleChunkMsg::encode,
                UpdateSingleChunkMsg::decode,
                TerritoryInfoSynchronizer::handler);
    }
    public static void UpdateTerritoryInfoToTracked(Chunk chunk)
    {
        TerritoryInfo info=chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
        UpdateTerritoryInfoToTracked(chunk,info);
    }
    public static void UpdateTerritoryInfoToTracked(Chunk chunk,TerritoryInfo info)
    {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(()->chunk),new UpdateSingleChunkMsg(chunk.getPos(),info));
    }
    public static void UpdateDeassignationToTracked(Chunk chunk)
    {
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(()->chunk),new UpdateSingleChunkMsg(chunk.getPos()));
    }
    public static void UpdateTerritoryInfoToPlayer(Chunk chunk,ServerPlayerEntity player)
    {
        TerritoryInfo info=chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
        TerritoryPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new UpdateSingleChunkMsg(chunk.getPos(),info));
    }
    private static void handler(UpdateSingleChunkMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayerEntity sender = contextSupplier.get().getSender();
            if (sender == null)//client side
            {
                PlayerEntity player = Minecraft.getInstance().player;
                World world = player.world;
                ChunkPos chunkPos=msg.target;
                Chunk targetChunk=world.getChunk(chunkPos.x,chunkPos.z);
                TerritoryInfo info=targetChunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
                if(msg.isProtected)
                {
                    info.assignedTo(msg.ownerId,msg.center,msg.territoryName,msg.defaultPermission,msg.permissions);
                    if(!TerritoryMod.TERRITORY_INFO_HASH_MAP.containsKey(chunkPos))
                        TerritoryMod.TERRITORY_INFO_HASH_MAP.replace(chunkPos,info);
                    TerritoryMod.TERRITORY_INFO_HASH_MAP.put(chunkPos,info);
                }else{
                    info.deassign();
                    TerritoryMod.TERRITORY_INFO_HASH_MAP.remove(chunkPos);
                }
                targetChunk.markDirty();
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public static class UpdateSingleChunkMsg {
        public Map<UUID, PermissionFlag> permissions=null;
        public PermissionFlag defaultPermission=null;
        public String territoryName=null;
        public BlockPos center=null;
        public ChunkPos target;
        public UUID ownerId=null;
        public boolean isProtected=true;
        public UpdateSingleChunkMsg(ChunkPos pos)
        {
            this.target=pos;
            this.isProtected=false;
        }
        public UpdateSingleChunkMsg(ChunkPos target,TerritoryInfo info)
        {
            this.target=target;
            this.isProtected=info.IsProtected();
            this.ownerId=info.getOwnerId();
            this.center=info.centerPos;
            this.defaultPermission=info.defaultPermission;
            this.permissions=info.permissions;
            this.territoryName=info.territoryName;
        }
        public UpdateSingleChunkMsg(ChunkPos target,String territoryName, BlockPos center,UUID ownerId,
                                    Map<UUID, PermissionFlag> permissionFlagMap, PermissionFlag defaultPermission) {
            permissions = permissionFlagMap;
            this.defaultPermission = defaultPermission;
            this.territoryName = territoryName;
            this.center = center;
            this.target=target;
            this.ownerId=ownerId;
        }

        public static void encode(UpdateSingleChunkMsg msg, PacketBuffer buffer) {
            buffer.writeBoolean(msg.isProtected);
            buffer.writeLong(msg.target.asLong());
            if(msg.isProtected)
            {
                buffer.writeBlockPos(msg.center);
                buffer.writeUniqueId(msg.ownerId);
                buffer.writeInt(msg.permissions.size());
                msg.permissions.forEach((k, v) -> {
                    buffer.writeUniqueId(k);
                    buffer.writeInt(v.getCode());
                });
                buffer.writeInt(msg.defaultPermission.getCode());
                buffer.writeString(msg.territoryName);
            }

        }

        public static UpdateSingleChunkMsg decode(PacketBuffer buffer) {
            boolean isProtected = buffer.readBoolean();
            ChunkPos target = new ChunkPos(buffer.readLong());
            if (isProtected) {
                BlockPos center = buffer.readBlockPos();
                UUID owner = buffer.readUniqueId();
                int permissionLength = buffer.readInt();
                HashMap<UUID, PermissionFlag> permissions = new HashMap<>();
                for (int i = 0; i < permissionLength; i++) {
                    UUID uuid = buffer.readUniqueId();
                    int code = buffer.readInt();
                    PermissionFlag flag = new PermissionFlag(code);
                    permissions.put(uuid, flag);
                }
                int defaultPermissionCode = buffer.readInt();
                String territoryName = buffer.readString(16);
                return new UpdateSingleChunkMsg(target, territoryName, center, owner, permissions, new PermissionFlag(defaultPermissionCode));
            }
            return new UpdateSingleChunkMsg(target);
        }
    }
}
