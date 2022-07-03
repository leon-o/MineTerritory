package top.leonx.territory.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import top.leonx.territory.component.ComponentContainer;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.tileentities.ModTileEntityTypes;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.util.MessageUtil;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings({"NullableProblems", "deprecation"})
public class TerritoryTableBlock extends BlockWithEntity {
    public TerritoryTableBlock() {
        super(AbstractBlock.Settings.of(Material.STONE, MapColor.RED).hardness
              (5.0F).resistance(1200.0F));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND) getTerritoryTileEntity(world, pos).mapStack = player.getStackInHand(hand);
        if (world.isClient) return ActionResult.PASS;
        TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(world, pos);
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        /*if (tileEntity.getOwnerId().equals(player.getUuid()) || tileEntity.getTerritoryInfo().permissions.containsKey(
                player.getUuid()) && tileEntity.getTerritoryInfo().permissions.get(player.getUuid()).contain(
                PermissionFlag.MANAGE) || !tileEntity.getTerritoryInfo().permissions.containsKey(
                player.getUuid()) && tileEntity.getTerritoryInfo().defaultPermission.contain(PermissionFlag.MANAGE)) {
            tileEntity.drawMapData();

            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        }*/
        return ActionResult.PASS;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TerritoryTableTileEntity(pos,state);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? TerritoryTableBlock.checkType(type, ModTileEntityTypes.TERRITORY_TILE_ENTITY, TerritoryTableTileEntity::tick) : null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Chunk chunk = context.getWorld().getChunk(context.getBlockPos().getX() >> 4, context.getBlockPos().getZ() >> 4);
        TerritoryInfo info = ComponentContainer.TERRITORY_INFO.get(chunk);
        // todo Capability
        if (info.IsProtected()) {
            if (!context.getWorld().isClient) {
                context.getPlayer().sendMessage(new TranslatableText("message.territory.already_occupied").setStyle(MessageUtil.YELLOW),false);
            }
            return Blocks.AIR.getDefaultState();
        }

        return super.getPlacementState(context);
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(worldIn, pos);
        tileEntity.initTerritoryInfo(placer.getUuid());
        tileEntity.drawMapData();
        super.onPlaced(worldIn, pos, state, placer, stack);
    }


    private TerritoryTableTileEntity getTerritoryTileEntity(World worldIn, BlockPos pos) {
        return (TerritoryTableTileEntity) worldIn.getBlockEntity(pos);
    }
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public boolean shouldDropItemsOnExplosion(Explosion explosion) {
        return false;
    }


    public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        TerritoryTableTileEntity tileEntity = getTerritoryTileEntity(worldIn, pos);
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (i > -2 && i < 2 && j == -1) {
                    j = 2;
                }

                if (rand.nextInt(16) == 0) {
                    for (int k = 0; k <= 1; ++k) {
                        BlockPos blockpos = pos.add(i, k, j);
                        double   power    = tileEntity.getBlockPower(worldIn, blockpos);
                        if (power > 0) {
                            if (!worldIn.isAir(pos.add(i / 2, 0, j / 2))) {
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

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
