package top.leonx.territory.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import top.leonx.territory.util.UserUtil;

import java.util.*;

public class TerritoryInfo {
    public BlockPos centerPos = BlockPos.ORIGIN;
    public String territoryName = "";
    public Map<Integer,TerritoryArea> areas = Collections.emptyMap();
    public Map<Integer,TerritoryGroup> groups = Collections.emptyMap();
    public PermissionFlag defaultPermission = PermissionFlag.NONE;
    public UUID ownerId = Util.NIL_UUID;
    public UUID territoryId = Util.NIL_UUID;

    // Version identification
    public int version=1;
    private boolean isProtected=false;

    public boolean IsProtected(){return isProtected;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerritoryInfo that = (TerritoryInfo) o;
        return version == that.version && territoryId.equals(that.territoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(territoryId, version);
    }

    public TerritoryInfo copy() {
        TerritoryInfo info=new TerritoryInfo();
        info.isProtected=isProtected;
        info.ownerId=ownerId;
        info.defaultPermission=new PermissionFlag(defaultPermission.getCode());
        info.territoryName=territoryName;
        info.centerPos=centerPos;
        info.territoryId=territoryId;
        return info;
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


    public static final String VERSION_KEY = "version";
    public static final String OWNER_ID_KEY="owner_id";
    public static final String TERRITORY_ID_KEY="te_id";
    //private static final String TERRITORY_POS_KEY ="territories";
    public static final String GROUP_ID_KEY="permission";
    public static final String AREA_KEY="areas";
    public static final String DEFAULT_PERMISSION_KEY="def_permission";
    public static final String TERRITORY_NAME_KEY="name";
    public static final String CENTER_POS="center_pos";
    public static final String IS_PROTECTED_KEY="protect";


    public void readFromNbt(NbtCompound compound) {
        TerritoryInfo instance = this;
        instance.version= compound.getInt(VERSION_KEY);

        this.ownerId=compound.getUuid(OWNER_ID_KEY);
        this.territoryId=compound.getUuid(TERRITORY_ID_KEY);
        this.territoryName=compound.getString(TERRITORY_NAME_KEY);
        this.defaultPermission=new PermissionFlag(compound.getInt(DEFAULT_PERMISSION_KEY));
        this.centerPos= BlockPos.fromLong(compound.getLong(CENTER_POS));
        NbtList groupNBT = compound.getList(GROUP_ID_KEY,NbtElement.COMPOUND_TYPE);
        NbtList areaNBT = compound.getList(AREA_KEY,NbtElement.COMPOUND_TYPE);

        if(groupNBT!=null && groupNBT.size()>0){
            this.groups= new HashMap<>();
            for (NbtElement element : groupNBT) {
                if(element instanceof NbtCompound groupTag){
                    var group = new TerritoryGroup();
                    group.readFromNbt(groupTag);
                    this.groups.put(group.groupId,group);
                }
            }
        }else{
            this.groups = Collections.emptyMap();
        }
        if(areaNBT!=null && areaNBT.size()>0){
            this.areas = new HashMap<>();

            for (NbtElement element : areaNBT) {
                if(element instanceof NbtCompound areaTag){
                    var area = new TerritoryArea();
                    area.readFromNbt(areaTag);
                    this.areas.put(area.areaId,area);
                }
            }
        }else{
            this.areas = Collections.emptyMap();
        }
            /*NbtList permissionList = compound.getList(PERMISSION_KEY, 10);
            permissionFlagHashMap=new HashMap<>();
            for (NbtElement t : permissionList) {
                Map.Entry<UUID, PermissionFlag> entry = ConvertNbtToUUIDPermission((NbtCompound) t);
                permissionFlagHashMap.put(entry.getKey(), entry.getValue());
            }*/


    }


    public void writeToNbt(NbtCompound compound) {
        TerritoryInfo instance = this;
        compound.putInt(VERSION_KEY,instance.version);
        compound.putUuid(OWNER_ID_KEY, instance.ownerId);
        compound.putUuid(TERRITORY_ID_KEY,instance.territoryId);
        compound.putInt(DEFAULT_PERMISSION_KEY,instance.defaultPermission.getCode());
        compound.putString(TERRITORY_NAME_KEY,instance.territoryName);
        compound.putLong(CENTER_POS,instance.centerPos.asLong());
        NbtList groupList = new NbtList();
        NbtList areaList = new NbtList();

        for (Integer id : groups.keySet()) {
            var group = groups.get(id);
            NbtCompound groupTag = new NbtCompound();
            group.writeToNbt(groupTag);
            groupList.add(groupTag);
        }

        for (Integer id : areas.keySet()) {
            var area = areas.get(id);
            NbtCompound areaTag = new NbtCompound();
            area.writeToNbt(areaTag);
            areaList.add(areaTag);
        }

        compound.put(GROUP_ID_KEY,groupList);
        compound.put(AREA_KEY,areaList);
    }

    public Map<Integer, TerritoryArea> getAreas() {
        return areas;
    }
}
