package top.leonx.territory.util;

import net.minecraftforge.common.UsernameCache;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserUtil {
    public static final String DEFAULT_NAME="All";
    public static final UUID DEFAULT_UUID=new UUID(0,0);
    public static boolean isDefaultUser(UUID uuid)
    {
        return DEFAULT_UUID.equals(uuid);
    }
    public static String getNameByUUID(UUID uuid)
    {
        if(uuid==null)return DEFAULT_NAME;
        String lastKnownUsername = UsernameCache.getLastKnownUsername(uuid);
        return lastKnownUsername==null?DEFAULT_NAME:lastKnownUsername;
    }
    public static UUID getUUIDByName(String name)
    {
        if(name.equals(DEFAULT_NAME))
            return DEFAULT_UUID;
        Optional<Map.Entry<UUID, String>> first = UsernameCache.getMap().entrySet().stream().filter(entry -> name.equals(entry.getValue())).findFirst();
        if(first.isPresent())
            return first.get().getKey();
        else
            return DEFAULT_UUID;
    }
    public static boolean hasPlayer(UUID uuid)
    {
        return UsernameCache.containsUUID(uuid);
    }
    public static boolean hasPlayer(String name)
    {
        return UsernameCache.getMap().values().stream().anyMatch(name::equals);
    }
}
