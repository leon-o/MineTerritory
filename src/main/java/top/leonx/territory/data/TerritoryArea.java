package top.leonx.territory.data;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.*;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TerritoryArea {
    public static final String TERRITORY_ID = "ter_id";
    public static final String AREA_ID = "area_id";
    public static final String CHILDREN = "child";
    public static final String PER_MAP = "per_map";
    public static final String CHUNK_POS_LIST = "chunk_list";
    public static final String CENTER_POS="center_pos";

    private UUID territoryId = Util.NIL_UUID;
    private UUID areaId = Util.NIL_UUID;

    @Nullable
    private TerritoryArea parent = null;

    public Map<UUID,Permission> permissionFlagMap = Collections.emptyMap();

    @Nullable
    private Map<UUID,PermissionFlag> bakedPermissionFlagMap = null;

    List<ChunkPos> chunks = new ArrayList<>();
    @Nullable
    List<TerritoryArea> children = null;

    private BlockPos centerPos = BlockPos.ORIGIN;

    private int areaLevel = 0;
    private boolean modified = false;
    public UUID getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(UUID territoryId) {
        this.territoryId = territoryId;
        if(children!=null){
            for (TerritoryArea child : children) {
                child.setTerritoryId(territoryId);
            }
        }
        MarkModified();
    }

    public UUID getAreaId() {
        return areaId;
    }

    public @Nullable TerritoryArea getParent() {
        return parent;
    }
    public TerritoryArea(){}
    public TerritoryArea(TerritoryArea parent) {
        this(parent,BlockPos.ORIGIN);
    }

    public TerritoryArea(TerritoryArea parent,BlockPos centerPos) {
        this.parent = parent;
        this.areaLevel = parent.areaLevel+1;
        this.territoryId = parent.territoryId;
        this.centerPos = centerPos;
    }

    public TerritoryArea(TerritoryInfo info,BlockPos centerPos){
        this.areaLevel = 0;
        this.territoryId = info.territoryId;
        this.centerPos = centerPos;
    }

    public int getAreaLevel() {
        return areaLevel;
    }

    public void readFromNbt(NbtCompound tag){
        territoryId = tag.getUuid(TERRITORY_ID);
        areaId = tag.getUuid(AREA_ID);
        centerPos = BlockPos.fromLong(tag.getLong(CENTER_POS));
        NbtList mapList = tag.getList(PER_MAP,NbtElement.COMPOUND_TYPE);
        permissionFlagMap = new HashMap<>();
        for (NbtElement element : mapList) {
            NbtCompound compound = (NbtCompound)element;
            permissionFlagMap.put(compound.getUuid(TerritoryGroup.GROUP_ID),
                    Permission.readFromNbt(compound));
        }

        NbtList chunkList = tag.getList(CHUNK_POS_LIST,NbtElement.LONG_TYPE);

        chunks = new ArrayList<>();
        if(chunkList!=null && chunkList.size()>0){
            for (NbtElement nbtElement : chunkList) {
                chunks.add(new ChunkPos(((NbtLong)nbtElement).longValue()));
            }
        }

        NbtList childrenList = tag.getList(CHILDREN,NbtElement.COMPOUND_TYPE);
        if(childrenList.size()>0){
            children = new ArrayList<>();
            for (NbtElement nbtElement : childrenList) {
                if(nbtElement instanceof NbtCompound compound){
                    TerritoryArea area = new TerritoryArea(this);
                    area.readFromNbt(compound);
                    children.add(area);
                }
            }
        }
        bakePermissionFlag();
    }

    public void writeToNbt(NbtCompound tag){
        tag.putUuid(TERRITORY_ID,territoryId);
        tag.putUuid(AREA_ID,areaId);
        tag.putLong(CENTER_POS,centerPos.asLong());
        NbtList list = new NbtList();

        if(permissionFlagMap!=null){
            for (UUID key : permissionFlagMap.keySet()) {
                NbtCompound compound = new NbtCompound();
                compound.putUuid(TerritoryGroup.GROUP_ID,key);
                permissionFlagMap.get(key).writeToNbt(compound);
                list.add(compound);
            }
        }
        tag.put(PER_MAP,list);

        NbtList chunkList = new NbtList();
        for (ChunkPos chunkPos : chunks) {
            chunkList.add(NbtLong.of(chunkPos.toLong()));
        }
        tag.put(CHUNK_POS_LIST,chunkList);

        NbtList childrenList = new NbtList();
        if(children!=null){
            for (TerritoryArea child : children) {
                NbtCompound compound= new NbtCompound();
                child.writeToNbt(compound);
                childrenList.add(compound);
            }
        }
        tag.put(CHILDREN,childrenList);
    }

    public BlockPos getCenterPos() {
        return centerPos;
    }

    private void bakePermissionFlag(){
        bakedPermissionFlagMap = new HashMap<>();
        if(parent!=null){
            var parentMap = parent.getPermissionFlagMap();
            bakedPermissionFlagMap.putAll(parentMap);
            for (var entry : permissionFlagMap.entrySet()) {
                Permission permission = permissionFlagMap.get(entry.getKey());
                PermissionFlag finalPer = new PermissionFlag();
                if(parentMap.containsKey(entry.getKey())){
                    PermissionFlag parentPer = parentMap.get(entry.getKey());
                    finalPer.combine(parentPer);
                }
                finalPer.combine(permission.getAllow());
                finalPer.remove(permission.getForbid());
            }
        }else{
            for (Map.Entry<UUID, Permission> entry : permissionFlagMap.entrySet()) {
                PermissionFlag flag = new PermissionFlag(entry.getValue().getAllow().getCode());
                flag.remove(entry.getValue().getForbid());
                bakedPermissionFlagMap.put(entry.getKey(),flag);
            }
        }
    }

    public Map<UUID,PermissionFlag> getPermissionFlagMap(){
        if(bakedPermissionFlagMap==null){
            bakePermissionFlag();
        }
        return bakedPermissionFlagMap;
    }

    public void MarkModified(){
        this.modified = true;
        if (children!=null) {
            for (TerritoryArea child : children) {
                child.MarkModified();
            }
        }
    }

    public TerritoryArea createChild(BlockPos centerPos) {
        var area = new TerritoryArea(this,centerPos);
        if(children==null){
            children=new ArrayList<>();
        }
        this.children.add(area);
        this.MarkModified();
        return area;
    }

    public List<ChunkPos> getChunks() {
        return Collections.unmodifiableList(chunks);
    }

    public List<TerritoryArea> getChildren() {
        if (children==null){
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(children);
    }
}
