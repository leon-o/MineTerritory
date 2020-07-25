package top.leonx.territory.events;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.client.renderer.TerritoryTableTileEntityRenderer;
import top.leonx.territory.client.screen.TerritoryScreen;
import top.leonx.territory.container.ModContainerTypes;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;

@Mod.EventBusSubscriber(modid = TerritoryMod.MODID,bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientModEvent {
    @SubscribeEvent
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(ModContainerTypes.TERRITORY_CONTAINER, TerritoryScreen::new);
        ClientRegistry.bindTileEntitySpecialRenderer(TerritoryTableTileEntity.class,new TerritoryTableTileEntityRenderer());
    }
}
