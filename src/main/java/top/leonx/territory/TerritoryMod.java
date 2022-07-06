package top.leonx.territory;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.leonx.territory.blocks.ModBlocks;
import top.leonx.territory.container.ModContainerTypes;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.network.TerritoryNetworkHandler;
import top.leonx.territory.tileentities.ModTileEntityTypes;

public class TerritoryMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "territory";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		TerritoryNetworkHandler.register();
		ModBlocks.Register();
		ModTileEntityTypes.Register();
		ModItems.register();
		ModContainerTypes.register();
	}
}
