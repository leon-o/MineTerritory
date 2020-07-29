package top.leonx.territory.data;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerritoryOperationMsg {
    @Nonnull
    public ChunkPos[] readyAdd;
    @Nonnull
    public ChunkPos[] readyRemove;
    @Nonnull
    public Map<UUID,PermissionFlag> permissions;
    public PermissionFlag defaultPermission;
    public String territoryName;
    public TerritoryOperationMsg(String territoryName,@Nonnull ChunkPos[] readyAdd, @Nonnull ChunkPos[] readyRemove,
                                 @Nonnull Map<UUID,PermissionFlag> permissionFlagMap,PermissionFlag defaultPermission) {
        this.readyAdd=readyAdd;
        this.readyRemove=readyRemove;
        permissions = permissionFlagMap;
        this.defaultPermission=defaultPermission;
        this.territoryName=territoryName;
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
        msg.permissions.forEach((k, v)->{
            buffer.writeUniqueId(k);
            buffer.writeInt(v.getCode());
        });
        buffer.writeInt(msg.defaultPermission.getCode());
        buffer.writeString(msg.territoryName);
    }

    public static TerritoryOperationMsg decode(PacketBuffer buffer) {
        int addLength=buffer.readInt();
        int removeLength=buffer.readInt();
        int permissionLength=buffer.readInt();

        HashMap<UUID,PermissionFlag> permissions = new HashMap<>();
        ChunkPos[] readyAdd = new ChunkPos[addLength];
        ChunkPos[] readyRemove = new ChunkPos[removeLength];

        for (int i=0;i<readyAdd.length;i++)
        {
            readyAdd[i] = new ChunkPos(buffer.readInt(),buffer.readInt());
        }
        for (int i=0;i<readyRemove.length;i++)
        {
            readyRemove[i] = new ChunkPos(buffer.readInt(),buffer.readInt());
        }
        for (int i=0;i<permissionLength;i++)
        {
            UUID uuid=buffer.readUniqueId();
            int code = buffer.readInt();
            PermissionFlag flag=new PermissionFlag(code);
            permissions.put(uuid,flag);
        }
        int defaultPermissionCode=buffer.readInt();
        String territoryName=buffer.readString(16);
        return new TerritoryOperationMsg(territoryName,readyAdd,readyRemove,permissions,new PermissionFlag(defaultPermissionCode));
    }
}
