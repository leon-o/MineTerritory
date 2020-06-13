package top.leonx.territory.data;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;

public class TerritoryOperationMsg {
    public ChunkPos[] readyAdd;
    public ChunkPos[] readyRemove;

    public static void encode(TerritoryOperationMsg msg, PacketBuffer buffer) {
        buffer.writeInt(msg.readyAdd.length);
        buffer.writeInt(msg.readyRemove.length);
        for (ChunkPos pos : msg.readyAdd) {
            buffer.writeInt(pos.x);
            buffer.writeInt(pos.z);
        }
        for (ChunkPos pos : msg.readyRemove) {
            buffer.writeInt(pos.x);
            buffer.writeInt(pos.z);
        }
    }

    public static TerritoryOperationMsg decode(PacketBuffer buffer) {
        int addLength=buffer.readInt();
        int removeLength=buffer.readInt();
        TerritoryOperationMsg msg= new TerritoryOperationMsg();
        msg.readyAdd=new ChunkPos[addLength];
        msg.readyRemove=new ChunkPos[removeLength];

        for (int i=0;i<msg.readyAdd.length;i++)
        {
            msg.readyAdd[i] = new ChunkPos(buffer.readInt(),buffer.readInt());
        }
        for (int i=0;i<msg.readyRemove.length;i++)
        {
            msg.readyRemove[i] = new ChunkPos(buffer.readInt(),buffer.readInt());
        }
        return msg;
    }
}
