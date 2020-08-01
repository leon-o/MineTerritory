package top.leonx.territory.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import top.leonx.territory.data.PermissionFlag;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

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
}
