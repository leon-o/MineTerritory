package top.leonx.territory.util;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.loading.FMLEnvironment;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.PowerProvider;

import java.util.*;

public class DataUtil {
    public static CompoundNBT ConvertPosToNbt(ChunkPos pos)
    {
        CompoundNBT posNbt=new CompoundNBT();
        posNbt.putLong("pos",pos.asLong());
        return posNbt;
    }
    public static ChunkPos ConvertNbtToPos(CompoundNBT nbt)
    {
        return new ChunkPos(nbt.getLong("pos"));
    }
    public static CompoundNBT ConvertUUIDPermissionToNbt(UUID uuid,PermissionFlag flag)
    {
        CompoundNBT nbt=new CompoundNBT();
        nbt.putUniqueId("uuid",uuid);
        nbt.putInt("flag",flag.getCode());
        return nbt;
    }

    public static Map.Entry<UUID,PermissionFlag> ConvertNbtToUUIDPermission(CompoundNBT nbt)
    {
        return new AbstractMap.SimpleEntry<>(nbt.getUniqueId("uuid"),
                new PermissionFlag(nbt.getInt("flag")));
    }

    @SuppressWarnings("AccessStaticViaInstance")
    public static double getBlockStateProtectPower(BlockState state, IWorld world, BlockPos pos)
    {
        ItemStack  stack;
        if(FMLEnvironment.dist.isClient())
            stack = state.getBlock().getPickBlock(state,null,world,pos,null);
        else {
            List<ItemStack> drops = state.getBlock().func_220070_a(state, (ServerWorld) world, pos, world.getTileEntity(pos));
            if(drops.size()>0)
                stack = drops.get(0);
            else
                return 0;
        }
        return getItemStackProtectPower(stack);
    }

    public static double getItemStackProtectPower(ItemStack stack)
    {
        double power=0;
        if(TerritoryConfig.powerProvider.containsKey(stack.getItem())){
            List<PowerProvider> providers = TerritoryConfig.powerProvider.get(stack.getItem());
            for (PowerProvider provider : providers) {

                if(provider.getTag()==null||stack.getTag()!=null && provider.getTag().keySet().stream().allMatch(t-> Objects.equals(stack.getTag().get(t),
                                                                                                                                    provider.getTag().get(t))))
                    power=Math.max(provider.getPower(),power);
            }
        }

        return power;
    }
}
