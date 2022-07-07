package top.leonx.territory.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Level;
import net.minecraft.world.chunk.LevelChunk;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.server.ServerLevel;
import top.leonx.territory.util.UserUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static top.leonx.territory.init.registry.ModCaps.TERRITORY_INFO_CAPABILITY;

public class TerritoryInfoHolder {
    public final ConcurrentHashMap<TerritoryInfo,HashSet<ChunkPos>>  TERRITORY_CHUNKS=new ConcurrentHashMap<>();

    private final Level world;

    private TerritoryInfoHolder(Level world)
    {
        this.world=world;
    }
    private static final HashMap<Level,TerritoryInfoHolder> territoryInfoHolders=new HashMap<>();
    public static TerritoryInfoHolder get(Level world)
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
    public void assignToLevelChunk(ChunkPos pos,TerritoryInfo info)
    {
        assignToLevelChunk(world.getChunk(pos.x,pos.z),info);
    }
    public void assignToLevelChunk(ChunkPos pos, UUID ownerId, UUID territoryId, BlockPos tablePos, String name, PermissionFlag defaultPer, Map<UUID, PermissionFlag> specificPer)
    {
        assignToLevelChunk(world.getChunk(pos.x,pos.z),ownerId,territoryId,tablePos,name,defaultPer,specificPer);
    }
    public void assignToLevelChunk(LevelChunk chunk, TerritoryInfo info)
    {
        getChunkTerritoryInfo(chunk).getFrom(info);
        addIndex(info, chunk.getPos());
        chunk.markDirty();

        if(!world.isRemote)
            TerritoryInfoSynchronizer.UpdateInfoToClientTracked(chunk,info);
    }
    public void assignToLevelChunk(LevelChunk chunk, UUID ownerId, UUID territoryId, BlockPos tablePos, String name, PermissionFlag defaultPer, Map<UUID, PermissionFlag> specificPer)
    {
        TerritoryInfo info = getChunkTerritoryInfo(chunk);
        info.assignedTo(ownerId,territoryId,tablePos,name,defaultPer,specificPer);
        addIndex(info, chunk.getPos());
        chunk.markDirty();

        if(!world.isClientSide)
            TerritoryInfoSynchronizer.UpdateInfoToClientTracked(chunk,info);
    }
    public void deassignToLevelChunk(ChunkPos pos)
    {
        deassignToLevelChunk(world.getChunk(pos.x,pos.z));
    }
    public void deassignToLevelChunk(LevelChunk chunk)
    {
        TerritoryInfo info = getChunkTerritoryInfo(chunk);
        removeIndex(info,chunk.getPos());
        info.deassign();
        chunk.markDirty();

        if(!world.isClientSide)
            TerritoryInfoSynchronizer.UpdateInfoToClientTracked(chunk,info);
    }
    public TerritoryInfo getChunkTerritoryInfo(LevelChunk chunk) {
        return chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
    }

    public TerritoryInfo getChunkTerritoryInfo(ChunkPos pos) {
        return getChunkTerritoryInfo(world.getChunk(pos.x, pos.z));
    }

    public Set<ChunkPos> getAssociatedTerritory(TerritoryInfo info) {
        return TERRITORY_CHUNKS.get(info);
    }

    public void addReservedTerritory(TerritoryInfo info,ChunkPos pos)
    {
        if(world.isClientSide) return;
        ServerLevel serverLevel=(ServerLevel)world;
        TerritoryLevelSavedData.get(serverLevel).addReservedTerritory(info,Collections.singleton(pos));
        getChunkTerritoryInfo(pos).getFrom(info);
        addIndex(info, pos);
        TerritoryInfoSynchronizer.UpdateInfoToClientTracked(world.getChunk(pos.x,pos.z),info);
    }
    public void addReservedTerritory(String name, PermissionFlag defaultFlag, HashSet<ChunkPos> area) {
        if(world.isClientSide) return;
        ServerLevel serverLevel=(ServerLevel)world;
        TerritoryInfo info = new TerritoryInfo();
        info.assignedTo(UserUtil.DEFAULT_UUID, UUID.randomUUID(), null, name, defaultFlag, null);
        TerritoryLevelSavedData.get(serverLevel).addReservedTerritory(info,area);
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
        if(world.isClientSide) return;
        ServerLevel serverLevel=(ServerLevel)world;
        TerritoryLevelSavedData.get(serverLevel).removeReservedTerritory(info);
        Set<ChunkPos> tmp=new HashSet<>(area); // avoid ConcurrentModificationException
        for (ChunkPos pos : tmp) {
            removeIndex(info,pos);
            getChunkTerritoryInfo(pos).deassign();
            TerritoryInfoSynchronizer.UpdateDeassignationToClientTracked(world.getChunk(pos.x,pos.z));
        }
    }
    public void updateReserveTerritory(TerritoryInfo oldInfo,TerritoryInfo newInfo)
    {
        if(world.isClientSide) return;
        ServerLevel serverLevel=(ServerLevel)world;
        TerritoryLevelSavedData.get(serverLevel).updateReservedTerritory(oldInfo,newInfo);

        for (ChunkPos pos : getAssociatedTerritory(oldInfo)) {
            getChunkTerritoryInfo(pos).getFrom(newInfo);
            TerritoryInfoSynchronizer.UpdateInfoToClientTracked(world.getChunk(pos.x,pos.z),newInfo);
        }

        HashSet<ChunkPos> tmp = TERRITORY_CHUNKS.get(oldInfo);
        TERRITORY_CHUNKS.remove(oldInfo);
        TERRITORY_CHUNKS.put(newInfo,tmp);
    }
}
