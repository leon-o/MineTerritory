package top.leonx.territory.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import top.leonx.territory.TerritoryMod;

import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryWorldSavedData extends WorldSavedData {
    static final String DATA_NAME="TERRITORY_WORLD_DATA";
    static final String RESERVED_TERRITORY="RESERVED_TERRITORY";
    public TerritoryWorldSavedData() {
        super(DATA_NAME);
    }
    public String testStr;

    public ListNBT reservedTerritory;


    public void addReservedTerritory(ChunkPos pos)
    {
        CompoundNBT posNbt=ConvertPosToNbt(pos);
        if(reservedTerritory.contains(posNbt))
            return;
        reservedTerritory.add(posNbt);
        if(!TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
        {
            TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.put(pos,new TerritoryData(null,pos,null));
        }
        markDirty();
    }
    public void removeReservedTerritory(ChunkPos pos)
    {
        CompoundNBT posNbt=ConvertPosToNbt(pos);
        reservedTerritory.remove(posNbt);
        if(TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
        {
            TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.remove(pos);
        }
        markDirty();
    }

    @Override
    public void read(CompoundNBT nbt) {
        reservedTerritory=nbt.getList(RESERVED_TERRITORY,10);

        for(int i=0;i<reservedTerritory.size();i++)
        {
            ChunkPos pos = ConvertNbtToPos(reservedTerritory.getCompound(i));
            if(!TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
            {
                TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.put(pos,new TerritoryData(null,pos,null));
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(RESERVED_TERRITORY,reservedTerritory);
        //compound.putString("test",testStr);
        return compound;
    }

    public static TerritoryWorldSavedData get(ServerWorld world) {
        TerritoryWorldSavedData instance=world.getSavedData().getOrCreate(TerritoryWorldSavedData::new,DATA_NAME);
        return instance;
    }
}
