package top.leonx.territory.init.registry;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import top.leonx.territory.core.TerritoryInfo;

public class ModCaps {
    public static Capability<TerritoryInfo> TERRITORY_INFO_CAPABILITY = CapabilityManager.get(new CapabilityToken<TerritoryInfo>() {
    });;
}
