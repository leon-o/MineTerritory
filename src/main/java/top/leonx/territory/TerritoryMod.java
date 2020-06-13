package top.leonx.territory;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.leonx.territory.blocks.ModBlocks;
import top.leonx.territory.container.ModContainerTypes;
import top.leonx.territory.container.TerritoryContainer;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.tileentities.ModTileEntityType;
import top.leonx.territory.tileentities.TerritoryTileEntity;

import java.util.concurrent.ConcurrentHashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TerritoryMod.MODID)
public class TerritoryMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID= "territory";
    public static final ConcurrentHashMap<ChunkPos, TerritoryTileEntity> TERRITORY_TILE_ENTITY_HASH_MAP=new ConcurrentHashMap<>();
    public TerritoryMod() {

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

//        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
//
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        TerritoryPacketHandler.Init();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

//    private void enqueueIMC(final InterModEnqueueEvent event)
//    {
//        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
//    }
//
//    private void processIMC(final InterModProcessEvent event)
//    {
//        LOGGER.info("Got IMC {}", event.getIMCStream().
//                map(m->m.getMessageSupplier().get()).
//                collect(Collectors.toList()));
//    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

        LOGGER.info("HELLO from server starting");
    }


    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {

            blockRegistryEvent.getRegistry().registerAll(
                    ModBlocks.TERRITORY_BLOCK.setRegistryName("territory")
            );
        }
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> blockRegistryEvent) {

            blockRegistryEvent.getRegistry().registerAll(
                    ModItems.TerritoryBlockItem.setRegistryName(ModBlocks.TERRITORY_BLOCK.getRegistryName())
            );
        }
        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> blockRegistryEvent) {

            blockRegistryEvent.getRegistry().registerAll(
                    ModTileEntityType.TERRITORY_TILE_ENTITY.setRegistryName(ModBlocks.TERRITORY_BLOCK.getRegistryName())
            );
        }
        @SubscribeEvent
        public static void onContainerTypeRegistry(final RegistryEvent.Register<ContainerType<?>> containerTypeRegister)
        {
            containerTypeRegister.getRegistry().registerAll(
                    ModContainerTypes.TERRITORY_CONTAINER.setRegistryName(
                            ModBlocks.TERRITORY_BLOCK.getRegistryName())
            );
        }
    }
}
