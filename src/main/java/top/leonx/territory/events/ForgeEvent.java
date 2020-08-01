package top.leonx.territory.events;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import top.leonx.territory.capability.ChunkCapabilityProvider;
import top.leonx.territory.command.TerritoryCommand;
import top.leonx.territory.TerritoryMod;

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
}
