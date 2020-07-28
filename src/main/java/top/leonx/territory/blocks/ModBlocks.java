package top.leonx.territory.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class ModBlocks {
    public static final Block TERRITORY_BLOCK=new TerritoryTableBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance(5.0F, 1200.0F));
}
