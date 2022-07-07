package top.leonx.territory.init.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.leonx.territory.TerritoryMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TerritoryMod.MODID);

    public static final RegistryObject<Item> TerritoryBlockItem=ITEMS.register("territory_table",
                                                                          ()->createBlockItem(ModBlocks.TERRITORY_BLOCK,new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static BlockItem createBlockItem(RegistryObject<Block> block, Item.Properties properties)
    {
        return new BlockItem(block.get(),properties);
    }
}
