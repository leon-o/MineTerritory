package top.leonx.territory;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TerritoryPacketHandler {
    public static SimpleChannel CHANNEL;

    public static void Init()
    {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(TerritoryMod.MODID, "main"),
                () -> "1",
                "1"::equals,
                "1"::equals
        );
    }
    public static <T> void registerMessage(int id,Class<T> type,
    BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer,
            T> decoder, BiConsumer<T,Supplier<NetworkEvent.Context>> handler)
    {
       CHANNEL.registerMessage(id,type,encoder,decoder,handler);
    }
}
