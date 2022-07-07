package top.leonx.territory.init.registry;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.common.blocks.TerritoryTableBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS          = DeferredRegister.create(ForgeRegistries.BLOCKS,
                                                                                          TerritoryMod.MODID);
    public static final RegistryObject<Block> TERRITORY_BLOCK = BLOCKS.register("territory_table",
                                                                                  TerritoryTableBlock::new);

    //public static final Block                   TERRITORY_BLOCK =
    //        new TerritoryTableBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance
     //(5.0F, 1200.0F));
}
