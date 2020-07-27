package top.leonx.territory.data;

import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.UsernameCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TerritoryInfo{
    //public final static TerritoryInfo defaultTerritoryInfo=new TerritoryInfo(null,new HashSet<>());

    public Set<ChunkPos> territories;
    public TerritoryInfo(String territoryName,@Nullable UUID ownerId, @Nonnull Set<ChunkPos> territoryMap, @Nonnull Map<UUID,PermissionFlag> permissions,
                         @Nonnull PermissionFlag defaultPermission)
    {
        this.ownerId=ownerId;
        this.permissions = permissions;
        this.territories=territoryMap;
        this.defaultPermission=defaultPermission;
        this.territoryName=territoryName;
    }
    public TerritoryInfo(String territoryName,@Nullable UUID ownerId,@Nonnull Set<ChunkPos> territoryMap)
    {
        this(territoryName,ownerId,territoryMap,new HashMap<>(),new PermissionFlag());
    }
    @Nullable
    private UUID ownerId;
    private String ownerName;
    @Nullable
    public UUID getOwnerId(){return ownerId;}
    public void setOwnerId(UUID id)
    {
        this.ownerId=id;
        ownerName= UsernameCache.getLastKnownUsername(id);
    }
    public String getOwnerName()
    {
        return ownerName;
    }
    public String territoryName;
    @Nonnull
    public Map<UUID,PermissionFlag> permissions;
    @Nonnull
    public PermissionFlag defaultPermission;
    @Override
    public boolean equals(Object obj) {
        if(obj==this)
            return true;
        if(obj instanceof TerritoryInfo)
        {
            TerritoryInfo data=(TerritoryInfo)obj;
            return data.ownerId == ownerId && data.permissions == permissions;
        }
        return false;
    }

    public TerritoryInfo copy() {
        Map<UUID,PermissionFlag> flags=new HashMap<>();
        permissions.forEach(flags::put);
        return new TerritoryInfo(territoryName,ownerId,new HashSet<>(territories),flags,new PermissionFlag(defaultPermission.getCode()));
    }
}
