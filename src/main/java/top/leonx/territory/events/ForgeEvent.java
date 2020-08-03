package top.leonx.territory.events;

import net.minecraft.block.FireBlock;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import top.leonx.territory.capability.ChunkCapabilityProvider;
import top.leonx.territory.command.TerritoryCommand;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;

import java.util.List;

@Mod.EventBusSubscriber(modid = TerritoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {

        TerritoryCommand.Register(event.getCommandDispatcher());
    }
    @SubscribeEvent
    public static void attachCapabilitiesToChunk(final AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(new ResourceLocation("territory_info_cap"), new ChunkCapabilityProvider());
    }

    @SubscribeEvent
    public static void onExplosionCreated(ExplosionEvent.Detonate event){
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
        BlockPos      pos = event.getLiquidPos();
        if(event.getNewState().getFluidState() instanceof LavaFluid || event.getNewState().getBlock() instanceof FireBlock)
        {
            TerritoryInfo info = TerritoryInfoHolder.get((World)event.getWorld()).getChunkTerritoryInfo(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
            if(info.IsProtected())
                event.setNewState(event.getOriginalState());
        }
    }
}
