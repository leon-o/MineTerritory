package top.leonx.territory.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.container.TerritoryTableContainer;
import top.leonx.territory.network.packet.GUIBaseDataPushRequestPacket;
import top.leonx.territory.network.packet.GUIChunkDataSyncPacket;
import top.leonx.territory.network.packet.GUIChunkOperateRequestPacket;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TerritoryNetworkHandler {
    public static Identifier GUI_CHUNK_DATA_SYNC = new Identifier(TerritoryMod.MOD_ID, "gui_chunk_data_sync");
    public static Identifier GUI_CHUNK_OPERATE_REQUEST = new Identifier(TerritoryMod.MOD_ID, "gui_chunk_operate_request");
    public static Identifier GUI_BASE_DATA_PUSH_REQUEST = new Identifier(TerritoryMod.MOD_ID, "gui_chunk_data_push_request");

    public static ServerChannel<GUIChunkDataSyncPacket> guiChunkDataSyncChannel;

    public static ServerChannel<GUIChunkOperateRequestPacket> guiChunkOperateChannel;

    public static ServerChannel<GUIBaseDataPushRequestPacket> guiBaseDataPushChannel;


    @Environment(EnvType.CLIENT)
    public static ClientChannel<GUIChunkDataSyncPacket> clientGuiChunkDataSyncChannel;

    @Environment(EnvType.CLIENT)
    public static ClientChannel<GUIChunkOperateRequestPacket> clientGuiChunkOperateChannel;

    @Environment(EnvType.CLIENT)
    public static ClientChannel<GUIBaseDataPushRequestPacket> clientGuiBaseDataPushChannel;

    public static void register() {
        guiChunkDataSyncChannel = registerServerMessage(GUI_BASE_DATA_PUSH_REQUEST, GUIChunkDataSyncPacket::encode);
        guiChunkOperateChannel = registerServerMessage(GUI_CHUNK_OPERATE_REQUEST, GUIChunkOperateRequestPacket::encode, GUIChunkOperateRequestPacket::decode,
                TerritoryTableContainer::ServerHandleChunkOperateRequest);
        guiBaseDataPushChannel = registerServerMessage(GUI_BASE_DATA_PUSH_REQUEST, GUIBaseDataPushRequestPacket::encode, GUIBaseDataPushRequestPacket::decode,
                TerritoryTableContainer::ServerHandleBaseDataPushRequest);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        clientGuiChunkDataSyncChannel = registerClientMessage(GUI_BASE_DATA_PUSH_REQUEST, GUIChunkDataSyncPacket::encode, GUIChunkDataSyncPacket::decode,
                TerritoryTableContainer::ClientHandleChunkDataSync);
        clientGuiChunkOperateChannel = registerClientMessage(GUI_CHUNK_OPERATE_REQUEST, GUIChunkOperateRequestPacket::encode);
        clientGuiBaseDataPushChannel = registerClientMessage(GUI_BASE_DATA_PUSH_REQUEST, GUIBaseDataPushRequestPacket::encode);
    }

    public static <T> ServerChannel<T> registerServerMessage(Identifier id, BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder,
                                                             ServerChannel.ServerHandler<T> serverHandler) {
        var channel = new FabricServerChannel<>(id, encoder, decoder, serverHandler);
        ServerPlayNetworking.registerGlobalReceiver(id, channel);
        return channel;
    }

    public static <T> ServerChannel<T> registerServerMessage(Identifier id, BiConsumer<T, PacketByteBuf> encoder) {
        var channel = new FabricServerChannel<>(id, encoder);
        ServerPlayNetworking.registerGlobalReceiver(id, channel);
        return channel;
    }

    @Environment(EnvType.CLIENT)
    public static <T> ClientChannel<T> registerClientMessage(Identifier id, BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder,
                                                             ClientChannel.ClientHandler<T> handler) {
        var channel = new FabricClientChannel<>(id, encoder, decoder, handler);
        ClientPlayNetworking.registerGlobalReceiver(id, channel);
        return channel;
    }

    @Environment(EnvType.CLIENT)
    public static <T> ClientChannel<T> registerClientMessage(Identifier id, BiConsumer<T, PacketByteBuf> encoder) {
        var channel = new FabricClientChannel<>(id, encoder);
        ClientPlayNetworking.registerGlobalReceiver(id, channel);
        return channel;
    }

}
