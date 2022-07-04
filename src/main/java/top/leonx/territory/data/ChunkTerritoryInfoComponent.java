package top.leonx.territory.data;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.util.Util;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChunkTerritoryInfoComponent implements Component {
    public static final String CHUNK_AREAS= "areas";
    public UUID territoryId = Util.NIL_UUID;
    public List<Integer> areas = Collections.emptyList();

    public ChunkTerritoryInfoComponent(Chunk chunk) {
    }

    public void readFromNbt(NbtCompound tag){
        territoryId = tag.getUuid(TerritoryInfo.TERRITORY_ID_KEY);
        var areaNbtArray =tag.get(CHUNK_AREAS);
        if(areaNbtArray instanceof NbtIntArray nbtIntArray){
            areas = new ArrayList<>();
            for (net.minecraft.nbt.NbtInt nbtInt : nbtIntArray) {
                areas.add(nbtInt.intValue());
            }
        }
    }

    public void writeToNbt(NbtCompound tag){
        tag.putUuid(TerritoryInfo.TERRITORY_ID_KEY,territoryId);
        tag.putIntArray(CHUNK_AREAS,areas);
    }
}
