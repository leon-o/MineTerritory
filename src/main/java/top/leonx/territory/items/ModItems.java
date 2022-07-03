package top.leonx.territory.items;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.blocks.ModBlocks;

public class ModItems {
    public static final BlockItem TerritoryBlockItem = new BlockItem(ModBlocks.TERRITORY_BLOCK,new Item.Settings().group(ItemGroup.DECORATIONS));

    public static void register(){
        Registry.register(Registry.ITEM, new Identifier(TerritoryMod.MOD_ID, "territory_table"), TerritoryBlockItem);
    }
}
