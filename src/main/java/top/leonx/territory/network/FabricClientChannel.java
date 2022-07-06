package top.leonx.territory.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import top.leonx.territory.TerritoryMod;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricClientChannel<T> extends ClientChannel<T> implements ClientPlayNetworking.PlayChannelHandler {
    Identifier id;
    private final FabricPacketContext<T> packetSender;
    public FabricClientChannel(Identifier id, BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder,
                               ClientHandler<T> clientHandler) {
        super(encoder, decoder,clientHandler);
        this.id = id;
        packetSender = new FabricPacketContext<>(encoder,decoder,id);
    }

    public FabricClientChannel(Identifier id, BiConsumer<T, PacketByteBuf> encoder) {
        super(encoder);
        this.id = id;
        packetSender = new FabricPacketContext<>(encoder,decoder,id);
    }

    @Override
    public void sendToServer(T msg) {
        if (FabricLoader.getInstance().getEnvironmentType()==EnvType.CLIENT){
            PacketByteBuf buf = PacketByteBufs.create();
            this.encoder.accept(msg,buf);
            ClientPlayNetworking.send(id,buf);
        }
    }

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (decoder == null || clientHandler == null) {
            TerritoryMod.LOGGER.error("Decoder and clientHandler can't be null, or you may should not send this packet to client from server");
            return;
        }
        T t = decoder.apply(buf);
        packetSender.setSender(responseSender);
        clientHandler.handle(client,handler,t,packetSender);
    }
}
