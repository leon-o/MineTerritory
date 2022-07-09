package top.leonx.territory.init.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.core.TerritoryInfoSynchronizer;
import top.leonx.territory.init.config.TerritoryConfig;
import top.leonx.territory.init.handler.TerritoryPacketHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        TerritoryPacketHandler.Init();
        TerritoryInfoSynchronizer.register();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientStart(FMLClientSetupEvent event)
    {
        TerritoryInfoSynchronizer.register();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        final ModConfig config = event.getConfig();
        if (config.getSpec() == TerritoryConfig.ConfigHolder.CLIENT_SPEC) {
            TerritoryConfig.ConfigHelper.bakeClient(config);
            TerritoryMod.LOGGER.debug("Load territory client config");
        } else if (config.getSpec() == TerritoryConfig.ConfigHolder.SERVER_SPEC) {
            TerritoryConfig.ConfigHelper.bakeServer(config);
            TerritoryMod.LOGGER.debug("Load territory server config");
        }
    }
}
