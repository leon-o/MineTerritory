package top.leonx.territory.transform;



// ChunkDataEvent.Load never fired.
/*public class SendChunkDataTransform {
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
}*/
