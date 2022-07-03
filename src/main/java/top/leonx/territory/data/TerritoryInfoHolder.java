package top.leonx.territory.data;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import top.leonx.territory.component.ComponentContainer;
import top.leonx.territory.util.UserUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TerritoryInfoHolder {
    public final ConcurrentHashMap<TerritoryInfo,HashSet<ChunkPos>>  TERRITORY_CHUNKS=new ConcurrentHashMap<>();

    private final World world;

    private TerritoryInfoHolder(World world)
    {
        this.world=world;
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
    public void assignToChunk(ChunkPos pos,TerritoryInfo info)
    {
        assignToChunk(world.getChunk(pos.x,pos.z),info);
    }
    public void assignToChunk(ChunkPos pos, UUID ownerId, UUID territoryId, BlockPos tablePos, String name, PermissionFlag defaultPer, Map<UUID, PermissionFlag> specificPer)
    {
        assignToChunk(world.getChunk(pos.x,pos.z),ownerId,territoryId,tablePos,name,defaultPer,specificPer);
    }
    public void assignToChunk(Chunk chunk,TerritoryInfo info)
    {
        getChunkTerritoryInfo(chunk).getFrom(info);
        addIndex(info, chunk.getPos());
        chunk.setNeedsSaving(true);

        //if(!world.isClient)
            //TerritoryInfoSynchronizer.UpdateInfoToClientTracked(chunk,info);
    }
    public void assignToChunk(Chunk chunk, UUID ownerId, UUID territoryId, BlockPos tablePos, String name, PermissionFlag defaultPer, Map<UUID, PermissionFlag> specificPer)
    {
        TerritoryInfo info = getChunkTerritoryInfo(chunk);
        info.assignedTo(ownerId,territoryId,tablePos,name,defaultPer,specificPer);
        addIndex(info, chunk.getPos());
        chunk.setNeedsSaving(true);

        //if(!world.isClient)
            //TerritoryInfoSynchronizer.UpdateInfoToClientTracked(chunk,info);
    }
    public void deassignToChunk(ChunkPos pos)
    {
        deassignToChunk(world.getChunk(pos.x,pos.z));
    }
    public void deassignToChunk(Chunk chunk)
    {
        TerritoryInfo info = getChunkTerritoryInfo(chunk);
        removeIndex(info,chunk.getPos());
        info.deassign();
        chunk.setNeedsSaving(true);

        //if(!world.isClient)
            //TerritoryInfoSynchronizer.UpdateInfoToClientTracked(chunk,info);
    }
    public TerritoryInfo getChunkTerritoryInfo(Chunk chunk) {
        return ComponentContainer.TERRITORY_INFO.get(chunk);
        //return chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
    }

    public TerritoryInfo getChunkTerritoryInfo(ChunkPos pos) {
        return getChunkTerritoryInfo(world.getChunk(pos.x, pos.z));
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
