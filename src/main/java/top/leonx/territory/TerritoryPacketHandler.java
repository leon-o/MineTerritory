package top.leonx.territory;

import net.minecraft.util.Identifier;

public class TerritoryPacketHandler {
    public static Identifier CHANNEL;

    public static void Init()
    {
        CHANNEL = new Identifier(TerritoryMod.MOD_ID, "territory_channel");
        /*CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(TerritoryMod.MODID, "main"),
                () -> "1",
                "1"::equals,
                "1"::equals
        );*/
    }

    /*public static <T> void registerMessage(int id, Class<T> type,
                                           BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf,
            T> decoder, BiConsumer<T,Supplier<NetworkEvent.Context>> handler)
    {
        CHANNEL.registerMessage(id,type,encoder,decoder,handler);
    }*/

    /*public static <T> void registerMessage(int id,Class<T> type,
    BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer,
            T> decoder, BiConsumer<T,Supplier<NetworkEvent.Context>> handler)
    {
       CHANNEL.registerMessage(id,type,encoder,decoder,handler);
    }*/
}
