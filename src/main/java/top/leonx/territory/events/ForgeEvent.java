package top.leonx.territory.events;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import top.leonx.territory.command.TerritoryCommand;
import top.leonx.territory.TerritoryMod;

@Mod.EventBusSubscriber(modid = TerritoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {

        TerritoryCommand.Register(event.getCommandDispatcher());
    }
}
