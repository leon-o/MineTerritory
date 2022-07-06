package top.leonx.territory.util;

import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class TerritoryUtil {

    public static HashSet<ChunkPos> computeCutChunk(ChunkPos center, Collection<ChunkPos> chunks) {
        if(chunks.size()<=1)
            return new HashSet<>();

        HashMap<ChunkPos, Boolean>  visit  = new HashMap<>();
        HashMap<ChunkPos, ChunkPos> parent = new HashMap<>();
        HashMap<ChunkPos, Integer>  dfn    = new HashMap<>();
        HashMap<ChunkPos, Integer>  low    = new HashMap<>();
        chunks.forEach(t -> {
            visit.put(t, false);
            parent.put(t, null);
            dfn.put(t, 0);
            low.put(t, 0);
        });
        HashSet<ChunkPos> result = new HashSet<>();
        computeCutChunk(center, chunks, visit, parent, dfn, low, 0, result);
        return result;
    }

    private static void computeCutChunk(ChunkPos current, Collection<ChunkPos> chunks, Map<ChunkPos, Boolean> visit, Map<ChunkPos, ChunkPos> parent, Map<ChunkPos,
            Integer> dfn, Map<ChunkPos, Integer> low, int dfsCount, HashSet<ChunkPos> result) {
        dfsCount++;
        int            children = 0;
        int            chunkX   = current.x;
        int            chunkZ   = current.z;
        ChunkPos       right    = new ChunkPos(chunkX + 1, chunkZ);
        ChunkPos       up       = new ChunkPos(chunkX, chunkZ + 1);
        ChunkPos       left     = new ChunkPos(chunkX - 1, chunkZ);
        ChunkPos       down     = new ChunkPos(chunkX, chunkZ - 1);
        List<ChunkPos> linked   = Arrays.asList(left, up, right, down);
        visit.replace(current, true);
        dfn.replace(current, dfsCount);
        low.replace(current, dfsCount);
        for (ChunkPos pos : linked) {
            if (!chunks.contains(pos)) continue;
            if (!visit.get(pos)) {
                children++;
                parent.replace(pos, current);
                computeCutChunk(pos, chunks, visit, parent, dfn, low, dfsCount, result);
                low.replace(current, Math.min(low.get(pos), low.get(current)));
                if (parent.get(current) == null && children > 1 || parent.get(current) != null && low.get(pos) >= dfn.get(current)) {
                    result.add(current);
                }
            } else if (pos != parent.get(current)) {
                low.replace(current, Math.min(low.get(current), dfn.get(pos)));
            }
        }
    }

    public static void computeSelectableBtn(Set<ChunkPos> result,Set<ChunkPos> occupied,Set<ChunkPos> forbidden){
        result.clear();
        for (ChunkPos pos : occupied) {
            int chunkX = pos.x;
            int chunkZ = pos.z;

            result.add(new ChunkPos(chunkX + 1, chunkZ));
            result.add(new ChunkPos(chunkX, chunkZ + 1));
            result.add(new ChunkPos(chunkX - 1, chunkZ));
            result.add(new ChunkPos(chunkX, chunkZ - 1));
        }
        result.removeIf(occupied::contains);
        result.removeIf(forbidden::contains);
    }

    public static void computeRemovableBtn(Set<ChunkPos> result,ChunkPos tileEntityChunkPos,Set<ChunkPos> occupied){
        result.clear();
        result.addAll(occupied);
        result.removeAll(TerritoryUtil.computeCutChunk(tileEntityChunkPos, occupied));
        result.remove(tileEntityChunkPos);
    }
}
