package top.leonx.territory.transform;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;

public class FireTransform {

    @SuppressWarnings("unused")
    public static boolean canBurn(BlockState state, IWorldReader worldIn, BlockPos pos)
    {
        if(TerritoryConfig.preventFire&& !state.getBlock().equals(Blocks.OBSIDIAN) && !state.getBlock().equals(Blocks.NETHERRACK)  && worldIn instanceof World)
        {
            TerritoryInfo info = TerritoryInfoHolder.get((World) worldIn).getChunkTerritoryInfo(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
            return !info.IsProtected();
        }
        return true;
    }
}
