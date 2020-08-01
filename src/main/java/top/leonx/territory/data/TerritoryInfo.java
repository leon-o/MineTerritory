package top.leonx.territory.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nullable;
import java.util.*;

public class TerritoryInfo {
    //public final static TerritoryInfo defaultTerritoryInfo=new TerritoryInfo(null,new HashSet<>());

    @Nullable
    public Set<ChunkPos> territories;
    @Nullable
    public BlockPos centerPos;
    @Nullable
    public String territoryName;
    @Nullable
    public Map<UUID, PermissionFlag> permissions;
    @Nullable
    public PermissionFlag defaultPermission;
    @Nullable
    private UUID ownerId;
    @Nullable
    private String ownerName;
    private boolean isProtected=false;
    public void assignedTo(UUID ownerId)
    {
        isProtected=true;
        setOwnerId(ownerId);
    }
    public void assignedTo(UUID ownerId,BlockPos tablePos,String name,PermissionFlag defaultPer,Set<ChunkPos> associatedChunks,
                           Map<UUID, PermissionFlag> specificPer)
    {
        isProtected=true;
        setOwnerId(ownerId);
        centerPos=tablePos;
        territoryName=name;
        defaultPermission=defaultPer;
        territories=associatedChunks;
        permissions=specificPer;
    }
    public void deassign()
    {
        isProtected=false;
        ownerId=null;
        ownerName=null;
        territories=null;
        territoryName=null;
        permissions=null;
        defaultPermission=null;
    }

    public boolean IsProtected(){return isProtected;}
    @Nullable
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID id) {
        this.ownerId = id;
        ownerName = UserUtil.getNameByUUID(id);
    }

    @Nullable
    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof TerritoryInfo) {
            TerritoryInfo data = (TerritoryInfo) obj;
            boolean a=Objects.equals(ownerId,data.ownerId);
            boolean b=Objects.equals(permissions,data.permissions);
            boolean c=Objects.equals(defaultPermission, data.defaultPermission);
            boolean d=Objects.equals(centerPos,data.centerPos);
            boolean e=Objects.equals(territories,data.territories);
            return a&&b&&c&&d&&e;
        }
        return false;
    }

    public TerritoryInfo copy() {
        Map<UUID, PermissionFlag> flags = new HashMap<>();
        if(permissions!=null)
            permissions.forEach(flags::put);
        TerritoryInfo info=new TerritoryInfo();
        info.isProtected=isProtected;
        info.ownerId=ownerId;
        info.ownerName= ownerName;
        info.defaultPermission=new PermissionFlag(defaultPermission.getCode());
        info.territoryName=territoryName;
        info.territories=new HashSet<>(territories);
        info.permissions=flags;
        info.centerPos=centerPos;
        return info;
    }
    @Override
    public String toString() {
        return String.format("{owner:%s,name:%s,center:%s,area:%s,defP:%d}", ownerName == null ? "NULL" : ownerName, territoryName == null ? "NULL" : territoryName, centerPos == null ?
                        "NULL" :centerPos.toString(), territories == null ? "NULL" : territories.iterator().next().toString(), defaultPermission == null ? 0 : defaultPermission.getCode());
    }
}
