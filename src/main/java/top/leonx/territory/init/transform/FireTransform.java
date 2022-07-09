package top.leonx.territory.init.transform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.core.TerritoryInfoHolder;
import top.leonx.territory.init.config.TerritoryConfig;

public class FireTransform {

    @SuppressWarnings("unused")
    public static boolean canBurn(BlockState state, LevelAccessor worldIn, BlockPos pos)
    {
        if(TerritoryConfig.preventFire&& !state.getBlock().equals(Blocks.OBSIDIAN) && !state.getBlock().equals(Blocks.NETHERRACK)  && worldIn instanceof Level)
        {
            TerritoryInfo info = TerritoryInfoHolder.get((Level) worldIn).getChunkTerritoryInfo(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
            return !info.IsProtected();
        }
        return true;
    }
}
