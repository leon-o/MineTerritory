package top.leonx.territory.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nullable;
import java.util.*;

import static top.leonx.territory.util.DataUtil.ConvertNbtToUUIDPermission;
import static top.leonx.territory.util.DataUtil.ConvertUUIDPermissionToNbt;

public class TerritoryInfo implements INBTSerializable<Tag> {
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

    // Version identification
    public int version=1;
    private boolean isProtected=false;
    @SuppressWarnings("unused")
    public void assignedTo(UUID ownerId)
    {
        isProtected=true;
        this.ownerId=ownerId;
    }
    public void assignedTo(UUID ownerId, UUID territoryId, BlockPos tablePos, String name, PermissionFlag defaultPer, Map<UUID, PermissionFlag> specificPer)
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
        this.version=info.version;
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

    private static final String VERSION_KEY = "version";
    private static final String OWNER_ID_KEY="owner_id";
    private static final String TERRITORY_ID_KEY="te_id";
    //private static final String TERRITORY_POS_KEY ="territories";
    private static final String PERMISSION_KEY="permission";
    private static final String DEFAULT_PERMISSION_KEY="def_permission";
    private static final String TERRITORY_NAME_KEY="name";
    private static final String CENTER_POS="center_pos";
    private static final String IS_PROTECTED_KEY="protect";

    @Override
    public Tag serializeNBT() {
        CompoundTag compound=new CompoundTag();
        compound.putInt(VERSION_KEY, version);
        if(permissions!=null)
        {
            ListTag permissionListNBT=new ListTag();
            permissions.forEach((k, v)-> permissionListNBT.add(ConvertUUIDPermissionToNbt(k,v)));
            compound.put(PERMISSION_KEY,permissionListNBT);
        }
        if(ownerId!=null)
            compound.putUUID(OWNER_ID_KEY, ownerId);
        if(territoryId!=null)
            compound.putUUID(TERRITORY_ID_KEY,territoryId);
        if(defaultPermission!=null)
            compound.putInt(DEFAULT_PERMISSION_KEY,defaultPermission.getCode());
        if(territoryName!=null)
            compound.putString(TERRITORY_NAME_KEY,territoryName);
        if(centerPos!=null)
            compound.putLong(CENTER_POS,centerPos.asLong());
        compound.putBoolean(IS_PROTECTED_KEY,IsProtected());
        return compound;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if(nbt.getId()!=10) return;
        CompoundTag compound=(CompoundTag) nbt;
        boolean isProtected = compound.getBoolean(IS_PROTECTED_KEY);
        version= compound.getInt(VERSION_KEY);
        if(isProtected)
        {
            UUID ownerId=compound.getUUID(OWNER_ID_KEY);
            UUID territoryId=compound.getUUID(TERRITORY_ID_KEY);
            HashMap<UUID, PermissionFlag> permissionFlagHashMap;
            ListTag permissionList = compound.getList(PERMISSION_KEY, 10);
            permissionFlagHashMap=new HashMap<>();
            for (Tag t : permissionList) {
                Map.Entry<UUID, PermissionFlag> entry = ConvertNbtToUUIDPermission((CompoundTag) t);
                permissionFlagHashMap.put(entry.getKey(), entry.getValue());
            }

            String territoryName=compound.getString(TERRITORY_NAME_KEY);
            PermissionFlag defPermissionFlag=new PermissionFlag(compound.getInt(DEFAULT_PERMISSION_KEY));
            BlockPos centerPos= BlockPos.of(compound.getLong(CENTER_POS));

            assignedTo(ownerId,territoryId,centerPos,territoryName,defPermissionFlag,permissionFlagHashMap);
        }
    }
}
