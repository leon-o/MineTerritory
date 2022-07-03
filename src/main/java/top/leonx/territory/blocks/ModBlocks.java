package top.leonx.territory.blocks;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.leonx.territory.TerritoryMod;

public class ModBlocks {
    /*public static final DeferredRegister<Block> BLOCKS          = DeferredRegister.create(ForgeRegistries.BLOCKS,
                                                                                          TerritoryMod.MODID);*/
    public static final Block   TERRITORY_BLOCK = new TerritoryTableBlock();

    public static void Register(){
        Registry.register(Registry.BLOCK, new Identifier(TerritoryMod.MOD_ID, "territory_table"), TERRITORY_BLOCK);
    }

    //public static final Block                   TERRITORY_BLOCK =
    //        new TerritoryTableBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance
     //(5.0F, 1200.0F));
}
