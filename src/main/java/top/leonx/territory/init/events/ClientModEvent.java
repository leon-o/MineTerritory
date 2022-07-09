package top.leonx.territory.init.events;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.client.renderer.TerritoryTableTileEntityRenderer;
import top.leonx.territory.client.screen.TerritoryScreen;
import top.leonx.territory.init.registry.ModMenuTypes;
import top.leonx.territory.init.registry.ModTiles;

@Mod.EventBusSubscriber(modid = TerritoryMod.MODID,bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientModEvent {
    @SubscribeEvent
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent event)
    {


        MenuScreens.register(ModMenuTypes.TERRITORY_CONTAINER.get(), TerritoryScreen::new);

        BlockEntityRenderers.register(ModTiles.TERRITORY_TILE_ENTITY.get(),
                TerritoryTableTileEntityRenderer::new);
    }
}
