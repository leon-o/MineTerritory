package top.leonx.territory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import top.leonx.territory.client.renderer.TerritoryTableTileEntityRenderer;
import top.leonx.territory.client.screen.TerritoryTableScreen;
import top.leonx.territory.container.ModContainerTypes;
import top.leonx.territory.container.TerritoryTableContainer;
import top.leonx.territory.network.TerritoryNetworkHandler;
import top.leonx.territory.tileentities.ModTileEntityTypes;

@Environment(EnvType.CLIENT)
public class TerritoryModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModTileEntityTypes.TERRITORY_TILE_ENTITY,
                                             ctx -> new TerritoryTableTileEntityRenderer());

        //ScreenRegistry.<ExampleGuiDescription, ExampleBlockScreen>register(MyMod.SCREEN_HANDLER_TYPE, (gui, inventory, title) -> new ExampleBlockScreen(gui, inventory.player, title));
        HandledScreens.register(ModContainerTypes.TERRITORY_CONTAINER,
                                (HandledScreens.Provider<TerritoryTableContainer, HandledScreen<TerritoryTableContainer>>) TerritoryTableScreen::new);

        TerritoryNetworkHandler.registerClient();
    }
}
