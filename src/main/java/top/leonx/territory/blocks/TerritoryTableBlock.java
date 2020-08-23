package top.leonx.territory.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import top.leonx.territory.capability.ModCapabilities;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.tileentities.ModTileEntityType;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings({"NullableProblems", "deprecation"})
public class TerritoryTableBlock extends Block {
    public TerritoryTableBlock(Properties properties) {
        super(properties);
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(handIn==Hand.MAIN_HAND)
            getTerritoryTileEntity(worldIn,pos).mapStack=player.getHeldItem(handIn);
        if(worldIn.isRemote)return ActionResultType.PASS;
        TerritoryTableTileEntity tileEntity=getTerritoryTileEntity(worldIn,pos);
        if(tileEntity.getOwnerId().equals(player.getUniqueID())
            ||tileEntity.getTerritoryInfo().permissions.containsKey(player.getUniqueID())&&
                tileEntity.getTerritoryInfo().permissions.get(player.getUniqueID()).contain(PermissionFlag.MANAGE)
            ||!tileEntity.getTerritoryInfo().permissions.containsKey(player.getUniqueID())&&
                tileEntity.getTerritoryInfo().defaultPermission.contain(PermissionFlag.MANAGE))
        {
            tileEntity.drawMapData();
            NetworkHooks.openGui((ServerPlayerEntity) player,tileEntity,pos);
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityType.TERRITORY_TILE_ENTITY.create();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {


        Chunk chunk = context.getWorld().getChunk(context.getPos().getX() >> 4, context.getPos().getZ() >> 4);
        TerritoryInfo info =
                chunk.getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY).orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());
        if (info.IsProtected()) {
            if (!context.getWorld().isRemote) {
                context.getPlayer().sendMessage(new StringTextComponent("Already occupied"));
            }
            return Blocks.AIR.getDefaultState();
        }

        return super.getStateForPlacement(context);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(worldIn, pos);
        tileEntity.initTerritoryInfo(placer.getUniqueID());
        tileEntity.drawMapData();
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }


    private TerritoryTableTileEntity getTerritoryTileEntity(IBlockReader worldIn, BlockPos pos)
    {
        return (TerritoryTableTileEntity)worldIn.getTileEntity(pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return makeCuboidShape(0,0,0,16,12,16);
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return false;
    }

//    @Override
//    public BlockRenderType getRenderType(BlockState state) {
//        return BlockRenderType.INVISIBLE;
//    }

    //    @Override
//    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
//        return getTerritoryTileEntity(world,pos).getOwnerId()==player.getUniqueID();
//    }

//    @Override
//    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
//        return getTerritoryTileEntity(worldIn,pos).getItem(state);
//    }
@OnlyIn(Dist.CLIENT)
public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
    super.animateTick(stateIn, worldIn, pos, rand);
    TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(worldIn, pos);
    for(int i = -2; i <= 2; ++i) {
        for(int j = -2; j <= 2; ++j) {
            if (i > -2 && i < 2 && j == -1) {
                j = 2;
            }

            if (rand.nextInt(16) == 0) {
                for(int k = 0; k <= 1; ++k) {
                    BlockPos blockpos = pos.add(i, k, j);
                    if (tileEntity.getBlockPower(worldIn,blockpos) > 0) {
                        if (!worldIn.isAirBlock(pos.add(i / 2, 0, j / 2))) {
                            break;
                        }

                        worldIn.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5D, (double)pos.getY() + 2.0D, (double)pos.getZ() + 0.5D, (double)((float)i + rand.nextFloat()) - 0.5D,
                                            (float)k - rand.nextFloat() - 1.0F, (double)((float)j + rand.nextFloat()) - 0.5D);
                    }
                }
            }
        }
    }

}

}
