package top.leonx.territory.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.*;

import static top.leonx.territory.capability.ModCapabilities.TERRITORY_INFO_CAPABILITY;
import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryWorldSavedData extends WorldSavedData {

    static final  String                                    DATA_NAME          = "TERRITORY_WORLD_DATA";
    static final  String                                    RESERVED_TERRITORY = "RESERVED_TERRITORY";
    private final HashMap<TerritoryInfo, Set<ChunkPos>> territoryIndex     = new HashMap<>();
    private final World                                     world;

    public TerritoryWorldSavedData(World world) {
        super(DATA_NAME);
        this.world = world;
    }

    public static TerritoryWorldSavedData get(ServerWorld world) {
        return world.getSavedData().getOrCreate(() -> new TerritoryWorldSavedData(world), DATA_NAME);
    }


    public void addReservedTerritory(TerritoryInfo info, Set<ChunkPos> area) {
        if (!territoryIndex.containsKey(info))
            territoryIndex.put(info, area);
        else
            territoryIndex.replace(info, area);

        markDirty();
    }

    public void removeReservedTerritory(TerritoryInfo info) {
        if (territoryIndex.remove(info) != null)
            markDirty();
    }

    public void updateReservedTerritory(TerritoryInfo oldInfo, TerritoryInfo newInfo) {
        Set<ChunkPos> removed = territoryIndex.remove(oldInfo);
        if (removed != null) {
            territoryIndex.put(newInfo, removed);
            markDirty();
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        territoryIndex.clear();
        ListNBT reservedTerritory = nbt.getList(RESERVED_TERRITORY, 10);
        for (int i = 0; i < reservedTerritory.size(); i++) {
            CompoundNBT compound = reservedTerritory.getCompound(i);

            ListNBT           terListNBT  = compound.getList("territories", 10);
            HashSet<ChunkPos> territories = new HashSet<>();
            TerritoryInfo     info        = new TerritoryInfo();

            TERRITORY_INFO_CAPABILITY.readNBT(info, null, compound);

            for (int j = 0; j < terListNBT.size(); j++) {
                ChunkPos pos = ConvertNbtToPos(terListNBT.getCompound(j));
                territories.add(pos);
                TerritoryInfoHolder.get(world).addIndex(info, pos);
            }
            territoryIndex.put(info, territories);

        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        ListNBT reservedTerritory = new ListNBT();

        for (Map.Entry<TerritoryInfo, Set<ChunkPos>> entry : territoryIndex.entrySet()) {
            CompoundNBT nbt         = (CompoundNBT) TERRITORY_INFO_CAPABILITY.writeNBT(entry.getKey(), null);
            ListNBT     territories = new ListNBT();
            entry.getValue().forEach(t -> territories.add(ConvertPosToNbt(t)));
            nbt.put("territories", territories);
            reservedTerritory.add(nbt);
        }
        compound.put(RESERVED_TERRITORY, reservedTerritory);
        return compound;
    }
}
