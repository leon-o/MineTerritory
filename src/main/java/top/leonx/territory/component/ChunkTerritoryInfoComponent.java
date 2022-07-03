package top.leonx.territory.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static top.leonx.territory.util.DataUtil.ConvertNbtToUUIDPermission;
import static top.leonx.territory.util.DataUtil.ConvertUUIDPermissionToNbt;

public class ChunkTerritoryInfoComponent extends TerritoryInfo implements Component {
    public ChunkTerritoryInfoComponent(Chunk chunk) {
    }

    private static final String VERSION_KEY = "version";
    private static final String OWNER_ID_KEY="owner_id";
    private static final String TERRITORY_ID_KEY="te_id";
    //private static final String TERRITORY_POS_KEY ="territories";
    private static final String PERMISSION_KEY="permission";
    private static final String DEFAULT_PERMISSION_KEY="def_permission";
    private static final String TERRITORY_NAME_KEY="name";
    private static final String CENTER_POS="center_pos";
    private static final String IS_PROTECTED_KEY="protect";


    @Override
    public void readFromNbt(NbtCompound compound) {
        ChunkTerritoryInfoComponent instance = this;
        boolean isProtected = compound.getBoolean(IS_PROTECTED_KEY);
        instance.version= compound.getInt(VERSION_KEY);
        if(isProtected)
        {
            UUID ownerId=compound.getUuid(OWNER_ID_KEY);
            UUID territoryId=compound.getUuid(TERRITORY_ID_KEY);
            HashMap<UUID, PermissionFlag> permissionFlagHashMap;
            NbtList permissionList = compound.getList(PERMISSION_KEY, 10);
            permissionFlagHashMap=new HashMap<>();
            for (NbtElement t : permissionList) {
                Map.Entry<UUID, PermissionFlag> entry = ConvertNbtToUUIDPermission((NbtCompound) t);
                permissionFlagHashMap.put(entry.getKey(), entry.getValue());
            }

            String territoryName=compound.getString(TERRITORY_NAME_KEY);
            PermissionFlag defPermissionFlag=new PermissionFlag(compound.getInt(DEFAULT_PERMISSION_KEY));
            BlockPos centerPos= BlockPos.fromLong(compound.getLong(CENTER_POS));

            instance.assignedTo(ownerId,territoryId,centerPos,territoryName,defPermissionFlag,permissionFlagHashMap);
        }
    }

    @Override
    public void writeToNbt(NbtCompound compound) {
        ChunkTerritoryInfoComponent instance = this;
        compound.putInt(VERSION_KEY,instance.version);
        if(instance.permissions!=null)
        {
            NbtList permissionListNBT=new NbtList();
            instance.permissions.forEach((k, v)-> permissionListNBT.add(ConvertUUIDPermissionToNbt(k,v)));
            compound.put(PERMISSION_KEY,permissionListNBT);
        }
        if(instance.ownerId!=null)
            compound.putUuid(OWNER_ID_KEY, instance.ownerId);
        if(instance.territoryId!=null)
            compound.putUuid(TERRITORY_ID_KEY,instance.territoryId);
        if(instance.defaultPermission!=null)
            compound.putInt(DEFAULT_PERMISSION_KEY,instance.defaultPermission.getCode());
        if(instance.territoryName!=null)
            compound.putString(TERRITORY_NAME_KEY,instance.territoryName);
        if(instance.centerPos!=null)
            compound.putLong(CENTER_POS,instance.centerPos.asLong());
        compound.putBoolean(IS_PROTECTED_KEY,instance.IsProtected());
    }
}
