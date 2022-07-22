package top.leonx.territory.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;

public class GUIChunkOperateRequestPacket {
    public enum Operation{
        Add,
        Remove,
        Submit;
        public static Operation fromInt(int value){
            if (value == 1) {
                return Operation.Remove;
            }else if(value == 2){
                return Operation.Submit;
            }
            return Operation.Add;
        }
    }
    private ChunkPos chunkPos;
    private Operation operation;

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public Operation getOperation() {
        return operation;
    }

    public GUIChunkOperateRequestPacket(ChunkPos chunkPos, Operation operation) {
        this.chunkPos = chunkPos;
        this.operation = operation;
    }

    public static void encode(GUIChunkOperateRequestPacket msg, PacketByteBuf buffer) {
        buffer.writeInt(msg.operation.ordinal());
        buffer.writeLong(msg.chunkPos.toLong());
    }

    public static GUIChunkOperateRequestPacket decode(PacketByteBuf buffer) {
        var operation = Operation.fromInt(buffer.readInt());
        var chunkPos = new ChunkPos(buffer.readLong());

        return new GUIChunkOperateRequestPacket(chunkPos,operation);
    }
}
