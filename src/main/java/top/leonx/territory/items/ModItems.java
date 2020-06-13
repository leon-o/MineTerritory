package top.leonx.territory.items;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import top.leonx.territory.blocks.ModBlocks;

public class ModItems {
    public static final Item TerritoryBlockItem= new BlockItem(ModBlocks.TERRITORY_BLOCK,new Item.Properties().group(ItemGroup.FOOD));
}
