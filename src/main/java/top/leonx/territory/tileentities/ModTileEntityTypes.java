package top.leonx.territory.tileentities;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.blocks.ModBlocks;

public class ModTileEntityTypes {
    public static BlockEntityType<TerritoryTableTileEntity> TERRITORY_TILE_ENTITY;
    public static void Register(){
        TERRITORY_TILE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(TerritoryMod.MOD_ID, "territory_table_block_entity"),
                                                  FabricBlockEntityTypeBuilder.create(TerritoryTableTileEntity::new,ModBlocks.TERRITORY_BLOCK).build());
    }
}
