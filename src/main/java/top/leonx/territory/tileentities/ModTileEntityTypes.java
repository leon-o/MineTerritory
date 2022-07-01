package top.leonx.territory.tileentities;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import top.leonx.territory.blocks.ModBlocks;

public class ModTileEntityTypes {
    /*public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES=
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MineTerritoryMod.MODID);*/

    public static final BlockEntityType<TerritoryTableTileEntity> TERRITORY_TILE_ENTITY= Registry.register(Registry.BLOCK_ENTITY_TYPE,"territory_table",()->BlockEntityType.Builder.create(TerritoryTableTileEntity::new,
                                                              ModBlocks.TERRITORY_BLOCK.get()).build(null));

}
