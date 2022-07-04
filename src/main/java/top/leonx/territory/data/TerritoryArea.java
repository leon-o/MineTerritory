package top.leonx.territory.data;

import net.minecraft.nbt.*;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class TerritoryArea {
    public static final String TERRITORY_ID = "ter_id";
    public static final String AREA_ID = "area_id";
    public static final String PER_MAP = "per_map";
    public static final String CHUNK_POS_LIST = "chunk_list";
    public UUID territoryId = Util.NIL_UUID;
    public int areaId = 0;
    public Map<Integer,PermissionFlag> permissionFlagMap = Collections.emptyMap();
    public List<ChunkPos> chunks = Collections.emptyList();

    public void readFromNbt(NbtCompound tag){
        territoryId = tag.getUuid(TERRITORY_ID);
        areaId = tag.getInt(AREA_ID);
        NbtList mapList = tag.getList(PER_MAP,NbtElement.COMPOUND_TYPE);
        permissionFlagMap = new HashMap<>();
        for (NbtElement element : mapList) {
            NbtCompound compound = (NbtCompound)element;
            permissionFlagMap.put(compound.getInt(TerritoryGroup.GROUP_ID),
                    PermissionFlag.readFromNbt(compound));
        }

        NbtList chunkList = tag.getList(CHUNK_POS_LIST,NbtElement.LONG_TYPE);
        if(chunkList!=null && chunkList.size()>0){
            chunks = new ArrayList<>();
            for (NbtElement nbtElement : chunkList) {
                chunks.add(new ChunkPos(((NbtLong)nbtElement).longValue()));
            }
        }
    }

    public void writeToNbt(NbtCompound tag){
        tag.putUuid(TERRITORY_ID,territoryId);
        tag.putInt(AREA_ID,areaId);
        NbtList list = new NbtList();

        for (Integer key : permissionFlagMap.keySet()) {
            NbtCompound compound = new NbtCompound();
            compound.putInt(TerritoryGroup.GROUP_ID,key);
            permissionFlagMap.get(key).writeToNbt(compound);
            list.add(compound);
        }
        tag.put(PER_MAP,list);

        NbtList chunkList = new NbtList();
        for (ChunkPos chunkPos : chunks) {
            chunkList.add(NbtLong.of(chunkPos.toLong()));
        }
        tag.put(CHUNK_POS_LIST,chunkList);
    }
}
