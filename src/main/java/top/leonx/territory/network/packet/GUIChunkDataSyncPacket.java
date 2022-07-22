package top.leonx.territory.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GUIChunkDataSyncPacket {

    public Collection<ChunkPos> selectable;
    public Collection<ChunkPos> removable;

    public Collection<ChunkPos> forbidden;

    public Collection<ChunkPos> occupied;

    public GUIChunkDataSyncPacket(Collection<ChunkPos> selectable, Collection<ChunkPos> removable, Collection<ChunkPos> forbidden, Collection<ChunkPos> occupied) {
        this.selectable = selectable;
        this.removable = removable;
        this.forbidden = forbidden;
        this.occupied = occupied;
    }

    public static void encode(GUIChunkDataSyncPacket msg, PacketByteBuf buffer) {
        buffer.writeInt(msg.selectable.size());
        buffer.writeInt(msg.removable.size());
        buffer.writeInt(msg.forbidden.size());
        buffer.writeInt(msg.occupied.size());
        for (ChunkPos pos : msg.selectable) {
            buffer.writeLong(pos.toLong());
        }
        for (ChunkPos pos : msg.removable) {
            buffer.writeLong(pos.toLong());
        }
        for(ChunkPos pos: msg.forbidden){
            buffer.writeLong(pos.toLong());
        }
        for(ChunkPos pos:msg.occupied){
            buffer.writeLong(pos.toLong());
        }
    }

    public static GUIChunkDataSyncPacket decode(PacketByteBuf buffer) {
        int selectableLen = buffer.readInt();
        int removableLen = buffer.readInt();
        int forbiddenLen = buffer.readInt();
        int occupiedLen = buffer.readInt();

        List<ChunkPos> selectable = new ArrayList<>(selectableLen);
        List<ChunkPos> removable = new ArrayList<>(removableLen);
        List<ChunkPos> forbidden = new ArrayList<>(forbiddenLen);
        List<ChunkPos> occupied = new ArrayList<>(occupiedLen);

        for (int i = 0; i < selectableLen; i++) {
            selectable.set(i,new ChunkPos(buffer.readLong()));
        }
        for (int i = 0; i < removableLen; i++) {
            removable.set(i,new ChunkPos(buffer.readLong()));
        }
        for (int i = 0; i < forbiddenLen; i++) {
            forbidden.set(i,new ChunkPos(buffer.readLong()));
        }
        for (int i = 0; i < occupiedLen; i++) {
            occupied.set(i,new ChunkPos(buffer.readLong()));
        }

        return new GUIChunkDataSyncPacket(selectable,removable,forbidden,occupied);
    }
}
