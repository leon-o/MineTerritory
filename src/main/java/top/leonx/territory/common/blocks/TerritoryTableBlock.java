package top.leonx.territory.common.blocks;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import top.leonx.territory.common.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.core.PermissionFlag;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.init.registry.ModCaps;
import top.leonx.territory.init.registry.ModTiles;
import top.leonx.territory.util.MessageUtil;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings({"NullableProblems", "deprecation"})
public class TerritoryTableBlock extends BaseEntityBlock {
    public TerritoryTableBlock() {
        super(Block.Properties.of(Material.STONE, MaterialColor.COLOR_RED)
                .strength(5.0F, 1200.0F));
    }



    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (handIn == InteractionHand.MAIN_HAND) getTerritoryTileEntity(worldIn, pos).mapStack = player.getItemInHand(handIn);
        if (worldIn.isClientSide) return InteractionResult.PASS;
        TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(worldIn, pos);
        if (tileEntity.getOwnerId().equals(player.getUUID()) || tileEntity.getTerritoryInfo().permissions.containsKey(
                player.getUUID()) && tileEntity.getTerritoryInfo().permissions.get(player.getUUID()).contain(
                PermissionFlag.MANAGE) || !tileEntity.getTerritoryInfo().permissions.containsKey(
                player.getUUID()) && tileEntity.getTerritoryInfo().defaultPermission.contain(PermissionFlag.MANAGE)) {
            tileEntity.drawMapData();
            NetworkHooks.openGui((ServerPlayer) player, tileEntity, pos);
        }
        return InteractionResult.SUCCESS;
    }



    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {


        var chunk = context.getLevel().getChunk(context.getClickedPos().getX() >> 4, context.getClickedPos().getZ() >> 4);
        TerritoryInfo info = chunk.getCapability(ModCaps.TERRITORY_INFO_CAPABILITY).orElse(
                new TerritoryInfo());
        if (info.IsProtected()) {
            if (!context.getLevel().isClientSide) {
                context.getPlayer().sendMessage(new TranslatableComponent("message.territory.already_occupied").setStyle(MessageUtil.YELLOW), Util.NIL_UUID);
            }
            return Blocks.AIR.defaultBlockState();
        }

        return super.getStateForPlacement(context);
    }


    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(worldIn, pos);
        tileEntity.initTerritoryInfo(placer.getUUID());
        tileEntity.drawMapData();
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }


    private TerritoryTableTileEntity getTerritoryTileEntity(BlockGetter worldIn, BlockPos pos) {
        return (TerritoryTableTileEntity) worldIn.getBlockEntity(pos);
    }
    private final VoxelShape SHAPE= Shapes.box(0, 0, 0, 16, 12, 16);


    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return super.getShape(pState, pLevel, pPos, pContext);
    }


    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return false;
    }



    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
        super.animateTick(stateIn, worldIn, pos, rand);
        TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(worldIn, pos);
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (i > -2 && i < 2 && j == -1) {
                    j = 2;
                }

                if (rand.nextInt(16) == 0) {
                    for (int k = 0; k <= 1; ++k) {
                        BlockPos blockpos = pos.offset(i, k, j);
                        double   power    = tileEntity.getBlockPower(worldIn, blockpos);
                        if (power > 0) {
                            if (!worldIn.isEmptyBlock(pos.offset(i / 2, 0, j / 2))) {
                                break;
                            }
                            for (int t = 0; t < power; t++)
                                worldIn.addParticle(ParticleTypes.ENCHANT, (double) pos.getX() + 0.5D, (double) pos.getY() + 2.0D, (double) pos.getZ() + 0.5D,
                                                    (double) ((float) i + rand.nextFloat()) - 0.5D, (float) k - rand.nextFloat() - 1.0F,
                                                    (double) ((float) j + rand.nextFloat()) - 0.5D);
                        }
                    }
                }
            }
        }

    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }


//    public boolean isTransparent(BlockState state) {
//        return true;
//    }
//
//
//
//    public BlockRenderType getRenderType(BlockState state) {
//        return BlockRenderType.MODEL;
//    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return ModTiles.TERRITORY_TILE_ENTITY.get().create(pPos, pState);
    }
}
