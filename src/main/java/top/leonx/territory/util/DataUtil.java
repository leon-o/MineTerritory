package top.leonx.territory.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLEnvironment;
import top.leonx.territory.core.PermissionFlag;
import top.leonx.territory.core.PowerProvider;
import top.leonx.territory.init.config.TerritoryConfig;

import java.util.*;

public class DataUtil {
    public static CompoundTag ConvertPosToNbt(ChunkPos pos)
    {
        CompoundTag posNbt=new CompoundTag();
        posNbt.putLong("pos",pos.toLong());
        return posNbt;
    }
    public static ChunkPos ConvertNbtToPos(CompoundTag nbt)
    {
        return new ChunkPos(nbt.getLong("pos"));
    }
    public static CompoundTag ConvertUUIDPermissionToNbt(UUID uuid,PermissionFlag flag)
    {
        CompoundTag nbt=new CompoundTag();
        nbt.putUUID("uuid",uuid);
        nbt.putInt("flag",flag.getCode());
        return nbt;
    }

    public static Map.Entry<UUID,PermissionFlag> ConvertNbtToUUIDPermission(CompoundTag nbt)
    {
        return new AbstractMap.SimpleEntry<>(nbt.getUUID("uuid"),
                new PermissionFlag(nbt.getInt("flag")));
    }

    @SuppressWarnings("AccessStaticViaInstance")
    public static double getBlockStateProtectPower(BlockState state, LevelAccessor world, BlockPos pos)
    {
        ItemStack stack;
        if(FMLEnvironment.dist.isClient())
            stack = state.getBlock().getCloneItemStack(state,null,world,pos,null);
        else {
            List<ItemStack> drops = state.getBlock().getDrops(state, (ServerLevel) world, pos, world.getBlockEntity(pos));
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

                if(provider.getTag()==null||stack.getTag()!=null && provider.getTag().getAllKeys().stream().allMatch(t-> Objects.equals(stack.getTag().get(t),
                                                                                                                                    provider.getTag().get(t))))
                    power=Math.max(provider.getPower(),power);
            }
        }

        return power;
    }
}
