package top.leonx.territory.init.transform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.core.TerritoryInfoHolder;
import top.leonx.territory.core.TerritoryInfoSynchronizer;

// ChunkDataEvent.Load never fired.
public class SendChunkDataTransform {
    public static void onServerChunkLoad(ChunkPos pos, ServerPlayer player)
    {
        TerritoryInfo info  = TerritoryInfoHolder.get(player.getCommandSenderWorld()).getChunkTerritoryInfo(pos);
        Level world = player.getCommandSenderWorld();

        TerritoryInfoSynchronizer.UpdateInfoToClientPlayer(pos, info, player);

        if(info.IsProtected())
            TerritoryInfoHolder.get(world).addIndex(info, pos);
        else
            TerritoryInfoHolder.get(world).removeIndex(info,pos);
    }
}
