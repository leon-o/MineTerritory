package top.leonx.territory.events;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.blocks.ModBlocks;
import top.leonx.territory.capability.ChunkCapabilityProvider;
import top.leonx.territory.capability.ModCapabilities;
import top.leonx.territory.container.ModContainerTypes;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.tileentities.ModTileEntityType;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        TerritoryPacketHandler.Init();
        ModCapabilities.register();
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
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {

        blockRegistryEvent.getRegistry().registerAll(
                ModBlocks.TERRITORY_BLOCK.setRegistryName("territory_table")
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
    public static void onContainerTypeRegistry(final RegistryEvent.Register<ContainerType<?>> containerTypeRegister) {
        containerTypeRegister.getRegistry().registerAll(
                ModContainerTypes.TERRITORY_CONTAINER.setRegistryName(
                        ModBlocks.TERRITORY_BLOCK.getRegistryName())
        );
    }
}
