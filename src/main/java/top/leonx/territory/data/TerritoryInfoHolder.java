package top.leonx.territory.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import top.leonx.territory.TerritoryMod;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class TerritoryInfoHolder {
    public final Map<ChunkPos,HashSet<TerritoryArea>> chunkToAreaMap = new HashMap<>();
    public final World world;
    private final WorldTerritoryInfoComponent worldData;
    private TerritoryInfoHolder(World world)
    {
        this.world=world;
        worldData = ComponentTypes.WORLD_TERRITORY_INFO.get(world);

        for (TerritoryInfo territory : worldData.getTerritories()) {
            for (ChunkPos pos : territory.getMainArea().chunks) {
                if(chunkToAreaMap.containsKey(pos)){
                    TerritoryMod.LOGGER.error( String.format("There has been a main area in %s",pos.toString()));
                }else{
                    var set = new HashSet<TerritoryArea>();
                    set.add(territory.getMainArea());
                    chunkToAreaMap.put(pos,set);
                }
                deepVisit(territory.getMainArea());
            }
        }
    }

    private void deepVisit(TerritoryArea area){
        if (area.children == null) {
            return;
        }
        for (TerritoryArea child : area.children) {
            for (ChunkPos pos : child.chunks) {
                if(chunkToAreaMap.containsKey(pos)){
                    chunkToAreaMap.get(pos).add(child);
                }else{
                    TerritoryMod.LOGGER.error( String.format("There is not a main area in %s",pos.toString()));
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


    public boolean assignToChunk(ChunkPos pos,TerritoryArea area)
    {
        if (chunkToAreaMap.containsKey(pos)) {
            if (chunkToAreaMap.get(pos).stream().anyMatch(a-> a.getAreaLevel()>=area.getAreaLevel())) {
                return false;
            }
        }

        if(!chunkToAreaMap.containsKey(pos)){
            chunkToAreaMap.put(pos,new HashSet<>());
        }
        var areas = chunkToAreaMap.get(pos);
        area.chunks.add(pos);
        areas.add(area);
        var curArea = area;
        while(curArea.getParent() != null){
            curArea = curArea.getParent();
            curArea.chunks.add(pos);
            areas.add(curArea);
        }

        return true;
    }

    public Optional<TerritoryInfo> getChunkTerritoryInfo(Chunk chunk) {
        return getChunkTerritoryInfo(chunk.getPos());
    }

    public Optional<TerritoryInfo> getChunkTerritoryInfo(ChunkPos pos) {
        if (chunkToAreaMap.containsKey(pos)) {
            var mainArea = chunkToAreaMap.get(pos);
            Optional<TerritoryArea> first = mainArea.stream().findFirst();
            if (first.isPresent()) {
                return worldData.getTerritoryById(first.get().getTerritoryId());
            }
        }
        return Optional.empty();
    }

    public Optional<TerritoryInfo> createTerritory(BlockPos pos, UUID ownerId){
        ChunkPos chunkPos = new ChunkPos(pos);
        if (chunkToAreaMap.containsKey(chunkPos) && chunkToAreaMap.get(chunkPos).size()>0){
            return Optional.empty();
        }
        var territory = new TerritoryInfo();
        String s = ownerId.toString() + new Date().getTime();
        territory.territoryId = UUID.nameUUIDFromBytes(s.getBytes(StandardCharsets.UTF_8));
        territory.territoryName = "My Territory"; // todo
        territory.ownerId = ownerId;
        var area = new TerritoryArea(territory,pos);
        territory.setMainArea(area);
        territory.defaultPermission = PermissionFlag.ALL;
        //territory.groups
        return Optional.of(territory);
    }

    public Optional<TerritoryArea> getLowestLevelTerritoryArea(ChunkPos pos){
        if (chunkToAreaMap.containsKey(pos)) {
            var areas = chunkToAreaMap.get(pos);
            return areas.stream().max(Comparator.comparingInt(TerritoryArea::getAreaLevel));
        }
        return Optional.empty();
    }

    public boolean isChunkOccupied(ChunkPos pos){
        return chunkToAreaMap.containsKey(pos) && chunkToAreaMap.get(pos).size()>0;
    }

    public void setAreaChunks(TerritoryArea area,Collection<ChunkPos> chunks){
        for (ChunkPos pos : area.getChunks()) {
            chunkToAreaMap.get(pos).remove(area);
        }
        area.chunks.clear();
        area.chunks.addAll(chunks);
        for (ChunkPos pos : area.getChunks()) {
            addToChunkAreaMap(pos,area);
        }

        area.MarkModified();
    }

    private void addToChunkAreaMap(ChunkPos pos,TerritoryArea area){
        HashSet<TerritoryArea> areas;
        if(chunkToAreaMap.containsKey(pos)){
            areas = chunkToAreaMap.get(pos);
        }else {
            areas=new HashSet<>();
            chunkToAreaMap.put(pos,areas);
        }
        areas.add(area);
    }

    /*private TerritoryArea getLowestLevelTerritoryAreaInternal(TerritoryArea parentArea, ChunkPos pos){
        if(parentArea.children!=null && parentArea.children.size()>0){
            for (TerritoryArea child : parentArea.children) {
                if (!child.chunks.contains(pos)) {
                    continue;
                }
                return getLowestLevelTerritoryAreaInternal(child,pos);
            }
        }else{
            return parentArea;
        }
    }*/

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
