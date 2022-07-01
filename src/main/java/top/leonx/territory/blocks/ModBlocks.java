package top.leonx.territory.blocks;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import top.leonx.territory.TerritoryMod;

public class ModBlocks {
    /*public static final DeferredRegister<Block> BLOCKS          = DeferredRegister.create(ForgeRegistries.BLOCKS,
                                                                                          TerritoryMod.MODID);*/
    public static final Block   TERRITORY_BLOCK = new TerritoryTableBlock();

    public void Register(){
        Registry.register(Registry.BLOCK,"territory_table", TERRITORY_BLOCK);
    }

    //public static final Block                   TERRITORY_BLOCK =
    //        new TerritoryTableBlock(Block.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance
     //(5.0F, 1200.0F));
}
