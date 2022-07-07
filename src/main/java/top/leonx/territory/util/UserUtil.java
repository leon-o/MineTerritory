package top.leonx.territory.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.entity.player.Player;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
            Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
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
            List<AbstractClientPlayer> players = Minecraft.getInstance().level.players();
            Optional<AbstractClientPlayer> first = players.stream().filter(t -> t.getName().getString().equals(name)).findFirst();
            if(first.isPresent())
                return first.get().getUUID();
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
            Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
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
            return Minecraft.getInstance().level.players().stream().anyMatch(t->t.getName().toString().equals(name));
        }

        return false;
    }
    public static List<String> getAllPlayerName()
    {
        List<String> result = new ArrayList<>(UsernameCache.getMap().values());
        if(FMLEnvironment.dist.isClient())
        {
            for (AbstractClientPlayer player : Minecraft.getInstance().level.players()) {
                result.add(player.getName().getString());
            }
        }
        return result;
    }
}
