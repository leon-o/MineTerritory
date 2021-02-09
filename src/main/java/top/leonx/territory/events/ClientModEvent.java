package top.leonx.territory.events;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.client.renderer.TerritoryTableTileEntityRenderer;
import top.leonx.territory.client.screen.TerritoryScreen;
import top.leonx.territory.container.ModContainerTypes;
import top.leonx.territory.tileentities.ModTileEntityTypes;

@Mod.EventBusSubscriber(modid = TerritoryMod.MODID,bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientModEvent {
    @SubscribeEvent
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent event)
    {

        DeferredWorkQueue.runLater(()-> {
            ScreenManager.registerFactory(ModContainerTypes.TERRITORY_CONTAINER.get(), TerritoryScreen::new);
        });
        ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.TERRITORY_TILE_ENTITY.get(),
                                              TerritoryTableTileEntityRenderer::new);
    }
}
