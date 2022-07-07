package top.leonx.territory.init.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.common.tileentities.TerritoryTableTileEntity;

public class ModTiles {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES=
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, TerritoryMod.MODID);

    public static final RegistryObject<BlockEntityType<TerritoryTableTileEntity>> TERRITORY_TILE_ENTITY=
            TILE_ENTITY_TYPES.register("territory_table",()->BlockEntityType.Builder.of(TerritoryTableTileEntity::new,
                                                              ModBlocks.TERRITORY_BLOCK.get()).build(null));

}
