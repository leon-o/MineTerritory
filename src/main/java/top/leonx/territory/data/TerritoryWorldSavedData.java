package top.leonx.territory.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;

import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryWorldSavedData extends WorldSavedData {
    static final String DATA_NAME="TERRITORY_WORLD_DATA";
    static final String RESERVED_TERRITORY="RESERVED_TERRITORY";
    public TerritoryWorldSavedData() {
        super(DATA_NAME);
    }
    public Collection<TerritoryInfo> territoryInfos=new HashSet<>();

    public void addReservedTerritory(TerritoryInfo info)
    {
        territoryInfos.add(info);
        markDirty();
    }
    public void removeReservedTerritory(TerritoryInfo info)
    {
        territoryInfos.remove(info);
        markDirty();
    }
    public void updateReservedTerritory(TerritoryInfo oldInfo,TerritoryInfo newInfo)
    {
        territoryInfos.remove(oldInfo);
        territoryInfos.add(newInfo);
        markDirty();
    }
    @Override
    public void read(CompoundNBT nbt) {
        ListNBT reservedTerritory=nbt.getList(RESERVED_TERRITORY,10);
        HashSet<TerritoryInfo> infosTmp=new HashSet<>();
        for(int i=0;i<reservedTerritory.size();i++)
        {
            CompoundNBT compound = reservedTerritory.getCompound(i);
            PermissionFlag defaultPm=new PermissionFlag(compound.getInt("def_pm"));
            ListNBT terListNBT=compound.getList("territories",10);
            String territoryName=compound.getString("name");
            HashSet<ChunkPos> territories=new HashSet<>();
            for(int j=0;j<terListNBT.size();j++)
            {
                ChunkPos pos=ConvertNbtToPos(terListNBT.getCompound(j));
                territories.add(pos);
            }

            TerritoryInfo info=new TerritoryInfo();
            info.assignedTo(null,null,territoryName,defaultPm,territories,null);
            infosTmp.add(info);
        }
        setTerritories(infosTmp);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        ListNBT reservedTerritory=new ListNBT();
        for(TerritoryInfo info:territoryInfos)
        {
            CompoundNBT nbt=new CompoundNBT();
            ListNBT territories=new ListNBT();
            nbt.putInt("def_pm",info.defaultPermission.getCode());
            info.territories.forEach(t-> territories.add(ConvertPosToNbt(t)));
            nbt.put("territories",territories);
            nbt.putString("name",info.territoryName);
            reservedTerritory.add(nbt);
        }
        compound.put(RESERVED_TERRITORY,reservedTerritory);
        return compound;
    }
    public void setTerritories(Collection<TerritoryInfo> infos)
    {
        territoryInfos=infos;
    }
    public static TerritoryWorldSavedData get(ServerWorld world) {
        return world.getSavedData().getOrCreate(TerritoryWorldSavedData::new,DATA_NAME);
    }
}
