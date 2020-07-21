package top.leonx.territory;

import com.google.common.collect.ConcurrentHashMultiset;
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
import top.leonx.territory.data.TerritoryData;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.tileentities.ModTileEntityType;
import top.leonx.territory.tileentities.TerritoryTileEntity;

import java.util.concurrent.ConcurrentHashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TerritoryMod.MODID)
public class TerritoryMod
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID= "territory";
    public static final ConcurrentHashMap<ChunkPos, TerritoryData> TERRITORY_TILE_ENTITY_HASH_MAP=
            new ConcurrentHashMap<>();
    public TerritoryMod() {

    }
}
