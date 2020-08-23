package top.leonx.territory.events;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.blocks.ModBlocks;
import top.leonx.territory.capability.ModCapabilities;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.container.ModContainerTypes;
import top.leonx.territory.data.TerritoryInfoSynchronizer;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.tileentities.ModTileEntityType;

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
