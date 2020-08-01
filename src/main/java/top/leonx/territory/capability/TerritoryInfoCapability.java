package top.leonx.territory.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static top.leonx.territory.util.DataUtil.*;
import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;

public class TerritoryInfoCapability {
    public static class Storage implements Capability.IStorage<TerritoryInfo> {
        private static final String OWNER_ID_KEY="owner_id";
        private static final String TERRITORY_POS_KEY ="territories";
        private static final String PERMISSION_KEY="permission";
        private static final String DEFAULT_PERMISSION_KEY="def_permission";
        private static final String TERRITORY_NAME_KEY="name";
        private static final String CENTER_POS="center_pos";
        private static final String IS_PROTECTED_KEY="protect";
        //private HashSet<HashSet<ChunkPos>> territoriesCache=new HashSet<>();
        @Nullable
        @Override
        public INBT writeNBT(Capability<TerritoryInfo> capability, TerritoryInfo instance, Direction side) {
            CompoundNBT compound=new CompoundNBT();
            ListNBT territoryListNBT=new ListNBT();
            if(instance.territories!=null)
            {
                instance.territories.forEach(t->{
                    CompoundNBT nbt=ConvertPosToNbt(t);
                    territoryListNBT.add(nbt);
                });
                compound.put(TERRITORY_POS_KEY, territoryListNBT);
            }
            if(instance.permissions!=null)
            {
                ListNBT permissionListNBT=new ListNBT();
                instance.permissions.forEach((k, v)-> permissionListNBT.add(ConvertUUIDPermissionToNbt(k,v)));
                compound.put(PERMISSION_KEY,permissionListNBT);
            }
            if(instance.getOwnerId()!=null)
                compound.putUniqueId(OWNER_ID_KEY, instance.getOwnerId());
            if(instance.defaultPermission!=null)
                compound.putInt(DEFAULT_PERMISSION_KEY,instance.defaultPermission.getCode());
            if(instance.territoryName!=null)
                compound.putString(TERRITORY_NAME_KEY,instance.territoryName);
            if(instance.centerPos!=null)
                compound.putLong(CENTER_POS,instance.centerPos.toLong());
            compound.putBoolean(IS_PROTECTED_KEY,instance.IsProtected());
            return compound;
        }

        @Override
        public void readNBT(Capability<TerritoryInfo> capability, TerritoryInfo instance, Direction side, INBT nbt) {
            if(nbt.getId()!=10) return;
            CompoundNBT compound=(CompoundNBT) nbt;
            boolean isProtected = compound.getBoolean(IS_PROTECTED_KEY);
            if(isProtected)
            {
                UUID ownerId=compound.getUniqueId(OWNER_ID_KEY);

                HashSet<ChunkPos> territoriesTmp;
                ListNBT territoryList = compound.getList(TERRITORY_POS_KEY, 10);
                territoriesTmp=new HashSet<>();
                for (INBT t : territoryList) {
                    territoriesTmp.add(ConvertNbtToPos((CompoundNBT) t));
                }

                HashMap<UUID, PermissionFlag> permissionFlagHashMap;
                ListNBT permissionList = compound.getList(PERMISSION_KEY, 10);
                permissionFlagHashMap=new HashMap<>();
                for (INBT t : permissionList) {
                    Map.Entry<UUID, PermissionFlag> entry = ConvertNbtToUUIDPermission((CompoundNBT) t);
                    permissionFlagHashMap.put(entry.getKey(), entry.getValue());
                }

                String territoryName=compound.getString(TERRITORY_NAME_KEY);
                PermissionFlag defPermissionFlag=new PermissionFlag(compound.getInt(DEFAULT_PERMISSION_KEY));
                BlockPos centerPos= BlockPos.fromLong(compound.getLong(CENTER_POS));

                instance.assignedTo(ownerId,centerPos,territoryName,defPermissionFlag,territoriesTmp,permissionFlagHashMap);
            }
        }
    }

    public static class TerritoryInfoFactory implements Callable<TerritoryInfo>
    {
        @Override
        public TerritoryInfo call() {
            return new TerritoryInfo();
        }
    }
}
