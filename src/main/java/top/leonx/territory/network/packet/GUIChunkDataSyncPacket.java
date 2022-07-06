package top.leonx.territory.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;

public class GUIChunkDataSyncPacket {

    public ChunkPos[] selectable;
    public ChunkPos[] removable;

    public ChunkPos[] forbidden;

    public ChunkPos[] occupied;

    public GUIChunkDataSyncPacket(ChunkPos[] selectable, ChunkPos[] removable, ChunkPos[] forbidden, ChunkPos[] occupied) {
        this.selectable = selectable;
        this.removable = removable;
        this.forbidden = forbidden;
        this.occupied = occupied;
    }

    public static void encode(GUIChunkDataSyncPacket msg, PacketByteBuf buffer) {
        buffer.writeInt(msg.selectable.length);
        buffer.writeInt(msg.removable.length);
        buffer.writeInt(msg.forbidden.length);
        buffer.writeInt(msg.occupied.length);
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

        ChunkPos[] selectable = new ChunkPos[selectableLen];
        ChunkPos[] removable = new ChunkPos[removableLen];
        ChunkPos[] forbidden = new ChunkPos[forbiddenLen];
        ChunkPos[] occupied = new ChunkPos[occupiedLen];

        for (int i = 0; i < selectableLen; i++) {
            selectable[i]=new ChunkPos(buffer.readLong());
        }
        for (int i = 0; i < removableLen; i++) {
            removable[i]=new ChunkPos(buffer.readLong());
        }
        for (int i = 0; i < forbiddenLen; i++) {
            forbidden[i]=new ChunkPos(buffer.readLong());
        }
        for (int i = 0; i < occupiedLen; i++) {
            occupied[i]=new ChunkPos(buffer.readLong());
        }

        return new GUIChunkDataSyncPacket(selectable,removable,forbidden,occupied);
    }
}
