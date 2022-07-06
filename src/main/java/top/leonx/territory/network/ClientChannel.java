package top.leonx.territory.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import top.leonx.territory.TerritoryMod;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public abstract class ClientChannel<T> {
    protected BiConsumer<T, PacketByteBuf> encoder;

    @Nullable
    protected Function<PacketByteBuf, T> decoder;

    @Nullable
    protected ClientHandler<T> clientHandler;

    public ClientChannel(BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder,ClientHandler<T> clientHandler) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.clientHandler = clientHandler;
    }

    public ClientChannel(BiConsumer<T, PacketByteBuf> encoder) {
        this.encoder = encoder;
    }

    public abstract void sendToServer(T msg);

    public interface ClientHandler<T>{
        void handle(MinecraftClient client, ClientPlayNetworkHandler networkHandler, T msg, PacketContext<T> packetSender);
    }
}
