package top.leonx.territory.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.reflect.Array;
import java.util.*;

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
        if(FMLEnvironment.dist.isClient())
        {
            PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(uuid);
            if(player!=null)
                return player.getName().getString();
        }
        String lastKnownUsername = UsernameCache.getLastKnownUsername(uuid);
        return lastKnownUsername==null?DEFAULT_NAME:lastKnownUsername;
    }
    public static UUID getUUIDByName(String name)
    {
        if(FMLEnvironment.dist== Dist.CLIENT)
        {
            List<AbstractClientPlayerEntity> players = Minecraft.getInstance().world.getPlayers();
            Optional<AbstractClientPlayerEntity> first = players.stream().filter(t -> t.getName().toString().equals(name)).findFirst();
            if(first.isPresent())
                return first.get().getUniqueID();
        }
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
        if(UsernameCache.containsUUID(uuid))
            return true;

        if(FMLEnvironment.dist.isClient())
        {
            PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(uuid);
            return player!=null;
        }
        return false;
    }
    public static boolean hasPlayer(String name)
    {
        if(UsernameCache.getMap().values().stream().anyMatch(name::equals))
            return true;
        if(FMLEnvironment.dist.isClient())
        {
            return Minecraft.getInstance().world.getPlayers().stream().anyMatch(t->t.getName().toString().equals(name));
        }

        return false;
    }
    public static List<String> getAllPlayerName()
    {
        List<String> result = new ArrayList<>(UsernameCache.getMap().values());
        if(FMLEnvironment.dist.isClient())
        {
            Collections.addAll(result,Minecraft.getInstance().world.getServer().getPlayerList().getOnlinePlayerNames());
        }
        return result;
    }
}
