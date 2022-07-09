package top.leonx.territory.init.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.apache.commons.lang3.tuple.Pair;
import top.leonx.territory.core.PermissionFlag;
import top.leonx.territory.core.PowerProvider;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public final class TerritoryConfig {

    // Client
    public static  boolean                        displayOwnerName;
    public static  boolean                        displayBoundary;
    // Server
    public static  double                         expNeededPerChunk;
    public static  Map<Item, List<PowerProvider>> powerProvider = new HashMap<>();
    public static  List<PermissionFlag>           usablePermission;
    public static  PermissionFlag                 defaultPermission;
    public static  boolean                        preventFire;
    public static  boolean                        preventExplosion;
    private static List<String>                   powerProviderRaw;

    public final static class ConfigHolder {

        public static final ForgeConfigSpec CLIENT_SPEC;
        public static final ForgeConfigSpec SERVER_SPEC;
        static final        ClientConfig    CLIENT;
        static final        ServerConfig    SERVER;

        static {
            {
                final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
                CLIENT = specPair.getLeft();
                CLIENT_SPEC = specPair.getRight();
            }
            {
                final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
                SERVER = specPair.getLeft();
                SERVER_SPEC = specPair.getRight();
            }
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    public final static class ConfigHelper {


        private static ModConfig clientConfig;
        private static ModConfig serverConfig;

        public static void bakeClient(final ModConfig config) {
            clientConfig = config;

            TerritoryConfig.displayOwnerName = ConfigHolder.CLIENT.displayOwnerName.get();
            TerritoryConfig.displayBoundary = ConfigHolder.CLIENT.displayBoundary.get();
        }

        public static void bakeServer(final ModConfig config) {
            serverConfig = config;

            TerritoryConfig.expNeededPerChunk = ConfigHolder.SERVER.expNeededPerChunk.get();
            powerProviderRaw = ConfigHolder.SERVER.powerProvider.get();
            processPowerProvider();
            List<String> permissionWhiteList = ConfigHolder.SERVER.permissionWhiteList.get();
            List<String> defaultPermissions  = ConfigHolder.SERVER.defaultPermission.get();
            boolean      addedToDefault      = ConfigHolder.SERVER.addToDefault.get();
            usablePermission = new ArrayList<>();
            defaultPermission = new PermissionFlag();
            for (PermissionFlag permissionFlag : PermissionFlag.basicFlag) {
                if (permissionWhiteList.contains(permissionFlag.getNameKey())) usablePermission.add(permissionFlag);
                else if (addedToDefault) defaultPermission.combine(permissionFlag);

                if (defaultPermissions.contains(permissionFlag.getNameKey())) defaultPermission.combine(permissionFlag);
            }
            TerritoryConfig.preventExplosion = ConfigHolder.SERVER.preventExplosion.get();
            TerritoryConfig.preventFire = ConfigHolder.SERVER.preventFire.get();
        }

        public static void setValueAndSave(final ModConfig modConfig, final String path, final Object newValue) {
            modConfig.getConfigData().set(path, newValue);
            modConfig.save();
        }

        public static void processPowerProvider() {
            if (Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).stream().findAny().isEmpty()) return;
            TerritoryConfig.powerProvider = new HashMap<>();

            Pattern typePattern = Pattern.compile("(item)|(tag)(?=:)");
            Pattern idPattern   = Pattern.compile("(?<=:)\\w+:\\w+(?=\\{?)");
            //noinspection RegExpRedundantEscape
            Pattern tagPattern   = Pattern.compile("\\{.*\\}");
            Pattern powerPattern = Pattern.compile("(?<=;)[0-9]+$");
            powerProviderRaw.forEach(t -> {

                Matcher typeMatcher  = typePattern.matcher(t);
                Matcher idMatcher    = idPattern.matcher(t);
                Matcher tagMatcher   = tagPattern.matcher(t);
                Matcher powerMatcher = powerPattern.matcher(t);
                if (!typeMatcher.find() || !idMatcher.find()) return;
                String           type  = typeMatcher.group();
                String           id    = idMatcher.group();
                Collection<Item> items = null;
                if (type.equals("tag")) {
                    ITag<Item> itemTag = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(ItemTags.create(new ResourceLocation(id)));
                            //ItemTags.getCollection().get(new ResourceLocation(id));
                    items = itemTag.stream().collect(Collectors.toList());
                } else if (type.equals("item")) {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                    items = Collections.singleton(item);
                } else {
                    return;
                }
                if (items == null) return;
                double power = 1;

                if (powerMatcher.find()) {
                    power = Double.parseDouble(powerMatcher.group());
                }
                CompoundTag tag = null;
                if (tagMatcher.find()) {
                    try {
                        tag = TagParser.parseTag(tagMatcher.group());
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                for (Item item : items) {
                    PowerProvider provider = new PowerProvider(item, tag, power);
                    if (TerritoryConfig.powerProvider.containsKey(item)) {
                        TerritoryConfig.powerProvider.get(item).add(provider);
                    } else {
                        ArrayList<PowerProvider> providers = new ArrayList<>();
                        providers.add(provider);
                        TerritoryConfig.powerProvider.put(item, providers);
                    }

                }
            });
        }
    }

}
