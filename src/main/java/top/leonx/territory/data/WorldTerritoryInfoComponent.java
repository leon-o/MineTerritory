package top.leonx.territory.data;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WorldTerritoryInfoComponent implements Component {
    public static final String TERRITORY_LIST = "territory_list";
    private final List<TerritoryInfo> territories = new ArrayList<>();
    private final Map<String,TerritoryInfo> nameToTerritoryInfo = new HashMap<>();
    private final Map<UUID,TerritoryInfo> idToTerritoryInfo = new HashMap<>();
    public WorldTerritoryInfoComponent(World world) {
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        NbtList nbtList = tag.getList(TERRITORY_LIST, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); i++) {
            var component = nbtList.getCompound(i);
            TerritoryInfo infoComponent=new TerritoryInfo();
            infoComponent.readFromNbt(component);
            territories.add(infoComponent);
            nameToTerritoryInfo.put(infoComponent.territoryName,infoComponent);
            idToTerritoryInfo.put(infoComponent.territoryId,infoComponent);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtList nbtList = new NbtList();
        for (TerritoryInfo territory : territories) {
            NbtCompound compound = new NbtCompound();
            territory.writeToNbt(compound);
        }
        tag.put(TERRITORY_LIST,nbtList);
    }

    public List<TerritoryInfo> getTerritories() {
        return territories;
    }

    public Optional<TerritoryInfo> getTerritoryById(UUID uuid){
        if(idToTerritoryInfo.containsKey(uuid)){
            return Optional.of(idToTerritoryInfo.get(uuid));
        }
        return Optional.empty();
    }
}
