package top.leonx.territory.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ServerChannel<T> {
    protected BiConsumer<T, PacketByteBuf> encoder;
    protected Function<PacketByteBuf, T> decoder;
    @Nullable
    protected ServerHandler<T> serverHandler;
    public ServerChannel(BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder,ServerHandler<T> serverHandler) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.serverHandler = serverHandler;
    }

    public ServerChannel(BiConsumer<T, PacketByteBuf> encoder) {
        this.encoder = encoder;
    }

    public abstract void sendToClient(ServerPlayerEntity player, T msg);


    public interface ServerHandler<T>{
        void handle(MinecraftServer server,ServerPlayerEntity player, ServerPlayNetworkHandler networkHandler, T msg, PacketContext<T> packetSender);
    }
}
