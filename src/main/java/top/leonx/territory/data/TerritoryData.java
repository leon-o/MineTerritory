package top.leonx.territory.data;

import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TerritoryData {
    public TerritoryData(UUID ownerId,ChunkPos pos,List<UUID> userId)
    {
        this.ownerId=ownerId;
        this.pos=pos;
        this.userId=userId;
    }
    @Nullable
    public UUID ownerId;

    public ChunkPos pos;

    @Nullable
    public List<UUID> userId;

    @Override
    public boolean equals(Object obj) {
        if(obj==this)
            return true;
        if(obj instanceof TerritoryData)
        {
            TerritoryData data=(TerritoryData)obj;
            if(data.ownerId==ownerId && data.pos==pos && data.userId==userId)
                return true;
        }
        return false;
    }
}
