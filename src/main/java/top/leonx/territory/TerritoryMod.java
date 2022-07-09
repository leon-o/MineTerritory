package top.leonx.territory;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.leonx.territory.init.config.TerritoryConfig;
import top.leonx.territory.init.registry.ModBlocks;
import top.leonx.territory.init.registry.ModItems;
import top.leonx.territory.init.registry.ModMenuTypes;
import top.leonx.territory.init.registry.ModTiles;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TerritoryMod.MODID)
public class TerritoryMod
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID= "territory";
    public TerritoryMod() {
        final var modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, TerritoryConfig.ConfigHolder.CLIENT_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, TerritoryConfig.ConfigHolder.SERVER_SPEC);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenuTypes.CONTAINER_TYPES.register(modEventBus);
        ModTiles.TILE_ENTITY_TYPES.register(modEventBus);
    }
}
