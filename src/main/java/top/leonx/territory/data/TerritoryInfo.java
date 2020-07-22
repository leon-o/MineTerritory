package top.leonx.territory.data;

import net.minecraftforge.common.UsernameCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerritoryInfo{
    public final static TerritoryInfo defaultTerritoryInfo=new TerritoryInfo(null, new HashMap<>());
    public TerritoryInfo(@Nullable UUID ownerId, @Nonnull Map<UUID,PermissionFlag> permissions)
    {
        this.ownerId=ownerId;
        this.permissions = permissions;
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
    @Nonnull
    public Map<UUID,PermissionFlag> permissions;

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
        return new TerritoryInfo(ownerId,flags);
    }
}
