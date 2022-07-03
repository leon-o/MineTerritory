package top.leonx.territory.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.PowerProvider;

import java.util.*;

public class DataUtil {
    public static NbtCompound ConvertPosToNbt(ChunkPos pos)
    {
        NbtCompound posNbt=new NbtCompound();
        posNbt.putLong("pos",pos.toLong());
        return posNbt;
    }
    public static ChunkPos ConvertNbtToPos(NbtCompound nbt)
    {
        return new ChunkPos(nbt.getLong("pos"));
    }
    public static NbtCompound ConvertUUIDPermissionToNbt(UUID uuid,PermissionFlag flag)
    {
        NbtCompound nbt=new NbtCompound();
        nbt.putUuid("uuid",uuid);
        nbt.putInt("flag",flag.getCode());
        return nbt;
    }

    public static Map.Entry<UUID,PermissionFlag> ConvertNbtToUUIDPermission(NbtCompound nbt)
    {
        return new AbstractMap.SimpleEntry<>(nbt.getUuid("uuid"),
                new PermissionFlag(nbt.getInt("flag")));
    }

    @SuppressWarnings("AccessStaticViaInstance")
    public static double getBlockStateProtectPower(BlockState state, WorldAccess world, BlockPos pos)
    {
        ItemStack  stack;
        if(FabricLoader.getInstance().getEnvironmentType()== EnvType.CLIENT)
            stack = state.getBlock().getPickStack(world,pos,null);
        else {
            List<ItemStack> drops = state.getBlock().getDroppedStacks(state, (ServerWorld) world, pos, world.getBlockEntity(pos));
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

                if(provider.getTag()==null||stack.getNbt()!=null && provider.getTag().getKeys().stream().allMatch(t-> Objects.equals(stack.getNbt().get(t),
                                                                                                                                    provider.getTag().get(t))))
                    power=Math.max(provider.getPower(),power);
            }
        }

        return power;
    }
}
