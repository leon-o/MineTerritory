package top.leonx.territory.data;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TerritoryInfoHolder {
    public final ConcurrentHashMap<TerritoryInfo,HashSet<ChunkPos>>  TERRITORY_CHUNKS=new ConcurrentHashMap<>();

    public final Map<ChunkPos,List<TerritoryArea>> chunkToAreaMap = new HashMap<>();
    public final World world;
    private final WorldTerritoryInfoComponent info;
    private TerritoryInfoHolder(World world)
    {
        this.world=world;
        info = ComponentTypes.WORLD_TERRITORY_INFO.get(world);

        for (TerritoryInfo territory : info.getTerritories()) {
            for (var area : territory.getAreas().values()) {
                for (ChunkPos pos : area.chunks) {
                    if (!chunkToAreaMap.containsKey(pos)) {
                        chunkToAreaMap.put(pos,List.of(area));
                    }else{
                        chunkToAreaMap.get(pos).add(area);
                    }
                }
            }
        }
    }
    private static final HashMap<World,TerritoryInfoHolder> territoryInfoHolders=new HashMap<>();
    public static TerritoryInfoHolder get(World world)
    {
        if(!territoryInfoHolders.containsKey(world))
        {
            TerritoryInfoHolder holder = new TerritoryInfoHolder(world);
            territoryInfoHolders.put(world,holder);
            return holder;
        }
        return territoryInfoHolders.get(world);
    }

    public void addIndex(TerritoryInfo info, ChunkPos chunkPos) {
        if (!TERRITORY_CHUNKS.containsKey(info))
            TERRITORY_CHUNKS.put(info,new HashSet<>());
        TERRITORY_CHUNKS.get(info).add(chunkPos);
    }

    public void removeIndex(TerritoryInfo info, ChunkPos pos) {
        if (!TERRITORY_CHUNKS.containsKey(info)) return;

        TERRITORY_CHUNKS.get(info).remove(pos);
        if(TERRITORY_CHUNKS.get(info).size()==0)
            TERRITORY_CHUNKS.remove(info);
    }
    public void assignToChunk(ChunkPos pos,TerritoryArea area)
    {
        area.chunks.add(pos);
    }

    public Optional<TerritoryInfo> getChunkTerritoryInfo(Chunk chunk) {
        return getChunkTerritoryInfo(chunk.getPos());
    }

    public Optional<TerritoryInfo> getChunkTerritoryInfo(ChunkPos pos) {
        if (chunkToAreaMap.containsKey(pos)) {
            List<TerritoryArea> areas = chunkToAreaMap.get(pos);
            if(areas.size()>0){
                TerritoryArea area = areas.get(0);
                info.getTerritoryById(area.territoryId);
            }
        }
        return Optional.empty();
    }

    public Set<ChunkPos> getAssociatedTerritory(TerritoryInfo info) {
        return TERRITORY_CHUNKS.get(info);
    }

    /*public void addReservedTerritory(TerritoryInfo info,ChunkPos pos)
    {
        if(world.isClient) return;
        ServerWorld serverWorld=(ServerWorld)world;
        TerritoryWorldSavedData.get(serverWorld).addReservedTerritory(info,Collections.singleton(pos));
        getChunkTerritoryInfo(pos).getFrom(info);
        addIndex(info, pos);
        TerritoryInfoSynchronizer.UpdateInfoToClientTracked(world.getChunk(pos.x,pos.z),info);
    }
    public void addReservedTerritory(String name, PermissionFlag defaultFlag, HashSet<ChunkPos> area) {
        if(world.isClient) return;
        ServerWorld serverWorld=(ServerWorld)world;
        TerritoryInfo info = new TerritoryInfo();
        info.assignedTo(UserUtil.DEFAULT_UUID, UUID.randomUUID(), null, name, defaultFlag, null);
        TerritoryWorldSavedData.get(serverWorld).addReservedTerritory(info,area);
        for (ChunkPos pos : area) {
            addIndex(info, pos);
            getChunkTerritoryInfo(pos).getFrom(info);
            TerritoryInfoSynchronizer.UpdateInfoToClientTracked(world.getChunk(pos.x,pos.z),info);
        }
    }

    public void removeReservedTerritory(TerritoryInfo info)
    {
        removeReservedTerritory(info,getAssociatedTerritory(info));
    }
    public void removeReservedTerritory(TerritoryInfo info,Set<ChunkPos> area)
    {
        if(world.isClient) return;
        ServerWorld serverWorld=(ServerWorld)world;
        TerritoryWorldSavedData.get(serverWorld).removeReservedTerritory(info);
        Set<ChunkPos> tmp=new HashSet<>(area); // avoid ConcurrentModificationException
        for (ChunkPos pos : tmp) {
            removeIndex(info,pos);
            getChunkTerritoryInfo(pos).deassign();
            TerritoryInfoSynchronizer.UpdateDeassignationToClientTracked(world.getChunk(pos.x,pos.z));
        }
    }
    public void updateReserveTerritory(TerritoryInfo oldInfo,TerritoryInfo newInfo)
    {
        if(world.isClient) return;
        ServerWorld serverWorld=(ServerWorld)world;
        TerritoryWorldSavedData.get(serverWorld).updateReservedTerritory(oldInfo,newInfo);

        for (ChunkPos pos : getAssociatedTerritory(oldInfo)) {
            getChunkTerritoryInfo(pos).getFrom(newInfo);
            TerritoryInfoSynchronizer.UpdateInfoToClientTracked(world.getChunk(pos.x,pos.z),newInfo);
        }

        HashSet<ChunkPos> tmp = TERRITORY_CHUNKS.get(oldInfo);
        TERRITORY_CHUNKS.remove(oldInfo);
        TERRITORY_CHUNKS.put(newInfo,tmp);
    }*/
}
