package top.leonx.territory.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import top.leonx.territory.data.TerritoryInfo;

public class ModCapabilities {
    @CapabilityInject(TerritoryInfo.class)
    public static Capability<TerritoryInfo> TERRITORY_INFO_CAPABILITY;
    public static void register()
    {
        CapabilityManager.INSTANCE.register(TerritoryInfo.class, new TerritoryInfoCapability.Storage(),new TerritoryInfoCapability.TerritoryInfoFactory());
    }
}
