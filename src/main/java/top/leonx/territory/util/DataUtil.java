package top.leonx.territory.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;

public class DataUtil {
    public static CompoundNBT ConvertPosToNbt(ChunkPos pos)
    {
        CompoundNBT posNbt=new CompoundNBT();
        posNbt.putInt("x",pos.x);
        posNbt.putInt("z",pos.z);
        return posNbt;
    }
    public static ChunkPos ConvertNbtToPos(CompoundNBT nbt)
    {
        return new ChunkPos(nbt.getInt("x"),nbt.getInt("z"));
    }
}
