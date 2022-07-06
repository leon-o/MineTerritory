package top.leonx.territory.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricServerChannel<T> extends ServerChannel<T> implements ServerPlayNetworking.PlayChannelHandler {
    Identifier id;
    private final FabricPacketContext<T> packetSender;
    public FabricServerChannel(Identifier id, BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder,
                               ServerHandler<T> serverHandler) {
        super(encoder, decoder,serverHandler);
        this.id = id;
        packetSender = new FabricPacketContext<>(encoder,decoder,id);
    }

    public FabricServerChannel(Identifier id, BiConsumer<T, PacketByteBuf> encoder) {
        super(encoder);
        this.id = id;
        packetSender = new FabricPacketContext<>(encoder,decoder,id);
    }

    @Override
    public void sendToClient(ServerPlayerEntity player, T msg) {
        PacketByteBuf buf = PacketByteBufs.create();
        this.encoder.accept(msg,buf);
        ServerPlayNetworking.send(player,id,buf);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if(serverHandler!=null){
            T t = decoder.apply(buf);
            packetSender.setSender(responseSender);
            serverHandler.handle(server,player,handler,t,packetSender);
        }
    }
}
