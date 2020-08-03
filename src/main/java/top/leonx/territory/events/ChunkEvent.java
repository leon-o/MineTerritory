package top.leonx.territory.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;
import top.leonx.territory.data.TerritoryInfoSynchronizer;


public class ChunkEvent {
    public static void onServerChunkLoad(ChunkPos pos, ServerPlayerEntity player)
    {
        TerritoryInfo info  = TerritoryInfoHolder.get(player.getEntityWorld()).getChunkTerritoryInfo(pos);
        World         world = player.getEntityWorld();

        TerritoryInfoSynchronizer.UpdateInfoToClientPlayer(pos, info, player);

        if(info.IsProtected())
            TerritoryInfoHolder.get(world).addIndex(info, pos);
        else
            TerritoryInfoHolder.get(world).removeIndex(info,pos);
    }
}
