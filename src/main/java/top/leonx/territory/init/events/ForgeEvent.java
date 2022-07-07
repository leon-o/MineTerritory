package top.leonx.territory.init.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.common.command.TerritoryCommand;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.core.TerritoryInfoHolder;
import top.leonx.territory.init.config.TerritoryConfig;

import java.util.List;

@Mod.EventBusSubscriber(modid = TerritoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    @SubscribeEvent
    public static void onServerStarting(RegisterCommandsEvent event) {
        TerritoryCommand.Register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        TerritoryConfig.ConfigHelper.processPowerProvider();
    }

    @SubscribeEvent
    public static void onExplosionCreated(ExplosionEvent.Detonate event){
        if(!TerritoryConfig.preventExplosion)
            return;
        List<BlockPos>      affectedBlocks = event.getAffectedBlocks();
        TerritoryInfoHolder holder         = TerritoryInfoHolder.get(event.getWorld());
        affectedBlocks.removeIf(t->{
            TerritoryInfo info = holder.getChunkTerritoryInfo(new ChunkPos(t.getX() >> 4, t.getZ() >> 4));
            return info.IsProtected();
        });
    }
    @SubscribeEvent
    public static void onFluidSpread(BlockEvent.FluidPlaceBlockEvent event)
    {
        if(!TerritoryConfig.preventFire)
            return;

        BlockPos      pos = event.getLiquidPos();
        if(event.getNewState().getFluidState().getType() instanceof LavaFluid || event.getNewState().getBlock() instanceof FireBlock)
        {
            TerritoryInfo info = TerritoryInfoHolder.get((Level) event.getWorld()).getChunkTerritoryInfo(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
            if(info.IsProtected())
                event.setNewState(event.getOriginalState());
        }
    }
}
