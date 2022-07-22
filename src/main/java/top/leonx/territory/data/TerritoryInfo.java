package top.leonx.territory.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import top.leonx.territory.util.UserUtil;

import java.util.*;

public class TerritoryInfo {
    public String territoryName = "";
    private TerritoryArea mainArea;
    //public Map<UUID,TerritoryArea> areas = Collections.emptyMap();
    public Map<UUID,TerritoryGroup> groups = Collections.emptyMap();
    public PermissionFlag defaultPermission = PermissionFlag.NONE;
    public UUID ownerId = Util.NIL_UUID;
    public UUID territoryId = Util.NIL_UUID;

    // Version identification
    public int version=1;

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


    @Override
    public String toString() {
        return String.format("{id:%s,owner:%s,name:%s,defP:%d}",
                territoryId==null?"NULL":territoryId.toString(),
                ownerId==null?"NULL":UserUtil.getNameByUUID(ownerId),
                Optional.ofNullable(territoryName).orElse("NULL"),
                defaultPermission == null ? 0 : defaultPermission.getCode());
    }


    public static final String VERSION_KEY = "version";
    public static final String OWNER_ID_KEY="owner_id";
    public static final String TERRITORY_ID_KEY="te_id";
    //private static final String TERRITORY_POS_KEY ="territories";
    public static final String GROUP_ID_KEY="permission";
    public static final String AREA_KEY="area";
    public static final String DEFAULT_PERMISSION_KEY="def_permission";
    public static final String TERRITORY_NAME_KEY="name";


    public void readFromNbt(NbtCompound compound) {
        TerritoryInfo instance = this;
        instance.version= compound.getInt(VERSION_KEY);

        this.ownerId=compound.getUuid(OWNER_ID_KEY);
        this.territoryId=compound.getUuid(TERRITORY_ID_KEY);
        this.territoryName=compound.getString(TERRITORY_NAME_KEY);
        this.defaultPermission=new PermissionFlag(compound.getInt(DEFAULT_PERMISSION_KEY));
        NbtList groupNBT = compound.getList(GROUP_ID_KEY,NbtElement.COMPOUND_TYPE);
        NbtCompound areaNBT = compound.getCompound(AREA_KEY);

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

        this.mainArea = new TerritoryArea();
        mainArea.readFromNbt(areaNBT);
    }


    public void writeToNbt(NbtCompound compound) {
        TerritoryInfo instance = this;
        compound.putInt(VERSION_KEY,instance.version);
        compound.putUuid(OWNER_ID_KEY, instance.ownerId);
        compound.putUuid(TERRITORY_ID_KEY,instance.territoryId);
        compound.putInt(DEFAULT_PERMISSION_KEY,instance.defaultPermission.getCode());
        compound.putString(TERRITORY_NAME_KEY,instance.territoryName);
        NbtList groupList = new NbtList();
        NbtCompound areaComponent = new NbtCompound();

        for (UUID id : groups.keySet()) {
            var group = groups.get(id);
            NbtCompound groupTag = new NbtCompound();
            group.writeToNbt(groupTag);
            groupList.add(groupTag);
        }

        mainArea.writeToNbt(areaComponent);

        compound.put(GROUP_ID_KEY,groupList);
        compound.put(AREA_KEY,areaComponent);
    }

    public TerritoryArea getMainArea() {
        return mainArea;
    }

    public void setMainArea(TerritoryArea mainArea) {
        this.mainArea = mainArea;
    }

    public BlockPos getCenterPos(){
        return mainArea.getCenterPos();
    }
}
