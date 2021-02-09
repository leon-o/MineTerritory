package top.leonx.territory.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.blocks.ModBlocks;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TerritoryMod.MODID);

    public static final RegistryObject<Item> TerritoryBlockItem=ITEMS.register("territory_table",
                                                                          ()->createBlockItem(ModBlocks.TERRITORY_BLOCK,new Item.Properties().group(ItemGroup.DECORATIONS)));

    public static BlockItem createBlockItem(RegistryObject<Block> block, Item.Properties properties)
    {
        return new BlockItem(block.get(),properties);
    }
}
