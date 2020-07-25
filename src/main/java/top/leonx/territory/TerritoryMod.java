package top.leonx.territory;

import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.leonx.territory.data.TerritoryInfo;

import java.util.concurrent.ConcurrentHashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TerritoryMod.MODID)
public class TerritoryMod
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID= "territory";
    public static final ConcurrentHashMap<ChunkPos, TerritoryInfo> TERRITORY_INFO_HASH_MAP =
            new ConcurrentHashMap<>();
    public TerritoryMod() {

    }
}
