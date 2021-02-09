package top.leonx.territory.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.capability.ModCapabilities;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.data.TerritoryInfoSynchronizer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        TerritoryPacketHandler.Init();
        TerritoryInfoSynchronizer.register();
        ModCapabilities.register();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientStart(FMLClientSetupEvent event)
    {
        TerritoryInfoSynchronizer.register();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
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
