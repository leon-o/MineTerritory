package top.leonx.territory.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;

public class GUIBaseDataPushRequestPacket {

    private String teritoryName;


    public static void encode(GUIBaseDataPushRequestPacket msg, PacketByteBuf buffer) {

    }

    public static GUIBaseDataPushRequestPacket decode(PacketByteBuf buffer) {
        return new GUIBaseDataPushRequestPacket();
    }
}
