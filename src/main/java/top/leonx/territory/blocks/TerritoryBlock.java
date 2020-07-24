package top.leonx.territory.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.tileentities.ModTileEntityType;
import top.leonx.territory.tileentities.TerritoryTileEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerritoryBlock extends Block {
    //public static final IntegerProperty X=IntegerProperty.create("X",Integer.MIN_VALUE,Integer.MAX_VALUE);
    //public static final IntegerProperty Y=IntegerProperty.create("X",Integer.MIN_VALUE,Integer.MAX_VALUE);
    public TerritoryBlock() {
        super(Properties.create(Material.IRON).sound(SoundType.GLASS).hardnessAndResistance(3f));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

        if(worldIn.isRemote)return false;
        TerritoryTileEntity tileEntity=getTerritoryTileEntity(worldIn,pos);
        if(tileEntity.getOwnerId().equals(player.getUniqueID())
            ||tileEntity.getTerritoryInfo().permissions.containsKey(player.getUniqueID())&&
                tileEntity.getTerritoryInfo().permissions.get(player.getUniqueID()).contain(PermissionFlag.MANAGE))
        {
            NetworkHooks.openGui((ServerPlayerEntity) player,tileEntity,pos);
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntityType.TERRITORY_TILE_ENTITY.create();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {

        {
            if(TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(context.getWorld().getChunkAt(context.getPos()).getPos() ))
            {
                if(!context.getWorld().isRemote)
                {
                    context.getPlayer().sendMessage(new StringTextComponent("这个区块已被其他玩家拥有"));
                }

                return Blocks.AIR.getDefaultState();
            }
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        getTerritoryTileEntity(worldIn,pos).setOwnerId(placer.getUniqueID());
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }


    private TerritoryTileEntity getTerritoryTileEntity(IBlockReader worldIn, BlockPos pos)
    {
        return (TerritoryTileEntity)worldIn.getTileEntity(pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return makeCuboidShape(0,0,0,16,12,16);
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getTerritoryTileEntity(world,pos).getOwnerId()==player.getUniqueID();
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return getTerritoryTileEntity(worldIn,pos).getItem(state);
    }


}
