package top.leonx.territory.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricPacketContext<T> extends PacketContext<T>{
    BiConsumer<T, PacketByteBuf> encoder;
    Function<PacketByteBuf, T> decoder;
    Identifier id;

    public FabricPacketContext(BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder, Identifier id) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.id = id;
    }

    private PacketSender sender;

    public PacketSender getSender() {
        return sender;
    }

    public void setSender(PacketSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendPacket(T msg) {
        PacketByteBuf buf = PacketByteBufs.create();
        encoder.accept(msg,buf);
        sender.sendPacket(id,buf);
    }

    @Override
    public void setHandled(boolean handled) {

    }
}
