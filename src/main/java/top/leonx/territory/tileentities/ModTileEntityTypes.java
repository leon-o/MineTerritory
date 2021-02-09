package top.leonx.territory.tileentities;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.blocks.ModBlocks;

public class ModTileEntityTypes {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES=
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, TerritoryMod.MODID);

    public static final RegistryObject<TileEntityType<TerritoryTableTileEntity>> TERRITORY_TILE_ENTITY=
            TILE_ENTITY_TYPES.register("territory_table",()->TileEntityType.Builder.create(TerritoryTableTileEntity::new,
                                                              ModBlocks.TERRITORY_BLOCK.get()).build(null));

}
