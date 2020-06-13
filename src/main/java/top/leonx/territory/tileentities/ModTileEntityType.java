package top.leonx.territory.tileentities;

import net.minecraft.tileentity.TileEntityType;
import top.leonx.territory.blocks.ModBlocks;

public class ModTileEntityType {
    public static final TileEntityType<TerritoryTileEntity> TERRITORY_TILE_ENTITY=
            TileEntityType.Builder.create(TerritoryTileEntity::new,ModBlocks.TERRITORY_BLOCK).build(null);
}
