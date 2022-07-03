package top.leonx.territory.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

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
        if(FabricLoader.getInstance().getEnvironmentType()== EnvType.CLIENT)
        {
            PlayerEntity player = MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
            if(player!=null)
                return player.getName().getString();
        }
        String lastKnownUsername = null;// UsernameCache.getLastKnownUsername(uuid);
        return lastKnownUsername==null?DEFAULT_NAME:lastKnownUsername;
    }
    public static UUID getUUIDByName(String name)
    {
        if(FabricLoader.getInstance().getEnvironmentType()== EnvType.CLIENT)
        {
            List<AbstractClientPlayerEntity> players = MinecraftClient.getInstance().world.getPlayers();
            Optional<AbstractClientPlayerEntity> first = players.stream().filter(t -> t.getName().getString().equals(name)).findFirst();
            if(first.isPresent())
                return first.get().getUuid();
        }
        if(name.equals(DEFAULT_NAME))
            return DEFAULT_UUID;
        Optional<Map.Entry<UUID, String>> first = Optional.empty();// todo UsernameCache.getMap().entrySet().stream().filter(entry -> name.equals(entry.getValue())).findFirst();
        if(first.isPresent())
            return first.get().getKey();
        else
            return DEFAULT_UUID;
    }
    public static boolean hasPlayer(UUID uuid)
    {
        return false;
        /*if(UsernameCache.containsUUID(uuid))
            return true;

        if(FMLEnvironment.dist.isClient())
        {
            PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(uuid);
            return player!=null;
        }
        return false;*/
    }
    public static boolean hasPlayer(String name)
    {
        /*if(UsernameCache.getMap().values().stream().anyMatch(name::equals))
            return true;
        if(FMLEnvironment.dist.isClient())
        {
            return Minecraft.getInstance().world.getPlayers().stream().anyMatch(t->t.getName().toString().equals(name));
        }*/

        return false;
    }
    public static List<String> getAllPlayerName()
    {
        /*List<String> result = new ArrayList<>(UsernameCache.getMap().values());
        if(FMLEnvironment.dist.isClient())
        {
            for (AbstractClientPlayerEntity player : Minecraft.getInstance().world.getPlayers()) {
                result.add(player.getName().getString());
            }
        }*/
        return List.of();// result;
    }
}
