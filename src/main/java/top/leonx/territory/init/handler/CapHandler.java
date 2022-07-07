package top.leonx.territory.init.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.leonx.territory.Static;
import top.leonx.territory.common.capability.ChunkCapabilityProvider;
import top.leonx.territory.core.TerritoryInfo;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/28 11:03
 * Version: 1.0
 */
@Mod.EventBusSubscriber
public class CapHandler {

    @SubscribeEvent
    public static void registerCap(RegisterCapabilitiesEvent event) {
        event.register(TerritoryInfo.class);
    }

    @SubscribeEvent
    public static void attachCapabilitiesToChunk(final AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(new ResourceLocation(Static.MOD_ID, "territory_info_cap"), new ChunkCapabilityProvider());
    }


}
