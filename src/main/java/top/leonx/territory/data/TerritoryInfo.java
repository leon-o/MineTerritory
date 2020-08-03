package top.leonx.territory.data;

import net.minecraft.util.math.BlockPos;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nullable;
import java.util.*;

public class TerritoryInfo {
    //public final static TerritoryInfo defaultTerritoryInfo=new TerritoryInfo(null,new HashSet<>());

//    @Nullable
//    public Set<ChunkPos> territories;
    @Nullable
    public BlockPos centerPos;
    @Nullable
    public String territoryName;
    @Nullable
    public Map<UUID, PermissionFlag> permissions;
    @Nullable
    public PermissionFlag defaultPermission;
    @Nullable
    public UUID ownerId;
    @Nullable
    public UUID territoryId;

    private boolean isProtected=false;
    public void assignedTo(UUID ownerId)
    {
        isProtected=true;
        this.ownerId=ownerId;
    }
    public void assignedTo(UUID ownerId,UUID territoryId,BlockPos tablePos,String name,PermissionFlag defaultPer,Map<UUID, PermissionFlag> specificPer)
    {
        this.isProtected=true;
        this.ownerId=ownerId;
        this.centerPos=tablePos;
        this.territoryName=name;
        this.defaultPermission=defaultPer;
        this.permissions=specificPer;
        this.territoryId=territoryId;
    }
    public void getFrom(TerritoryInfo info)
    {
        this.isProtected=info.isProtected;
        this.ownerId=info.ownerId;
        this.centerPos=info.centerPos;
        this.territoryName=info.territoryName;
        this.defaultPermission=info.defaultPermission;
        this.permissions=info.permissions;
        this.territoryId=info.territoryId;
    }
    public void deassign()
    {
        isProtected=false;
        ownerId=null;
        territoryName=null;
        permissions=null;
        defaultPermission=null;
        territoryId=null;
    }

    public boolean IsProtected(){return isProtected;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof TerritoryInfo) {
            TerritoryInfo data = (TerritoryInfo) obj;
            return Objects.equals(ownerId,data.ownerId)
                    &&Objects.equals(permissions,data.permissions)
                    &&Objects.equals(defaultPermission, data.defaultPermission)
                    &&Objects.equals(centerPos,data.centerPos)
                    &&Objects.equals(territoryId,data.territoryId);
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
        info.defaultPermission=new PermissionFlag(defaultPermission.getCode());
        info.territoryName=territoryName;
        info.permissions=flags;
        info.centerPos=centerPos;
        info.territoryId=territoryId;
        return info;
    }

    @Override
    public int hashCode() {
        if(!isProtected)
            return 0;
        int hash=territoryId.hashCode();
        hash=31*hash+ownerId.hashCode();
        hash=31*hash+territoryName.hashCode();
        hash=31*hash+defaultPermission.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{id:%s,owner:%s,name:%s,center:%s,defP:%d}",
                territoryId==null?"NULL":territoryId.toString(),
                ownerId==null?"NULL":UserUtil.getNameByUUID(ownerId),
                Optional.ofNullable(territoryName).orElse("NULL"),
                centerPos == null ?"NULL" :centerPos.toString(),
                defaultPermission == null ? 0 : defaultPermission.getCode());
    }
}
