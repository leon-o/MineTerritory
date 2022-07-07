package top.leonx.territory.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.server.ServerLevel;
import net.minecraft.world.storage.LevelSavedData;

import javax.annotation.Nonnull;
import java.util.*;

import static top.leonx.territory.init.registry.ModCaps.TERRITORY_INFO_CAPABILITY;
import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryLevelSavedData extends SavedData {

    static final  String                                    DATA_NAME          = "TERRITORY_WORLD_DATA";
    static final  String                                    RESERVED_TERRITORY = "RESERVED_TERRITORY";
    private final HashMap<TerritoryInfo, Set<ChunkPos>> territoryIndex     = new HashMap<>();
    private final Level world;

    public TerritoryLevelSavedData(Level world) {
        super(DATA_NAME);
        this.world = world;
    }

    public static TerritoryLevelSavedData get(ServerLevel world) {
        return world.getSavedData().getOrCreate(() -> new TerritoryLevelSavedData(world), DATA_NAME);
    }


    public void addReservedTerritory(TerritoryInfo info, Set<ChunkPos> area) {
        if (!territoryIndex.containsKey(info))
            territoryIndex.put(info, area);
        else
            territoryIndex.replace(info, area);

        setDirty();
    }

    public void removeReservedTerritory(TerritoryInfo info) {
        if (territoryIndex.remove(info) != null)
            setDirty();
    }

    public void updateReservedTerritory(TerritoryInfo oldInfo, TerritoryInfo newInfo) {
        Set<ChunkPos> removed = territoryIndex.remove(oldInfo);
        if (removed != null) {
            territoryIndex.put(newInfo, removed);
            setDirty();
        }
    }

    @Override
    public void read(CompoundTag nbt) {
        territoryIndex.clear();
        ListTag reservedTerritory = nbt.getList(RESERVED_TERRITORY, 10);
        for (int i = 0; i < reservedTerritory.size(); i++) {
            CompoundTag compound = reservedTerritory.getCompound(i);

            ListTag           terListTag  = compound.getList("territories", 10);
            HashSet<ChunkPos> territories = new HashSet<>();
            TerritoryInfo     info        = new TerritoryInfo();

            TERRITORY_INFO_CAPABILITY.readNBT(info, null, compound);

            for (int j = 0; j < terListTag.size(); j++) {
                ChunkPos pos = ConvertNbtToPos(terListTag.getCompound(j));
                territories.add(pos);
                TerritoryInfoHolder.get(world).addIndex(info, pos);
            }
            territoryIndex.put(info, territories);

        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag reservedTerritory = new ListTag();

        for (Map.Entry<TerritoryInfo, Set<ChunkPos>> entry : territoryIndex.entrySet()) {
            CompoundTag nbt         = (CompoundTag) TERRITORY_INFO_CAPABILITY.writeNBT(entry.getKey(), null);
            ListTag     territories = new ListTag();
            entry.getValue().forEach(t -> territories.add(ConvertPosToNbt(t)));
            nbt.put("territories", territories);
            reservedTerritory.add(nbt);
        }
        compound.put(RESERVED_TERRITORY, reservedTerritory);
        return compound;
    }
}
