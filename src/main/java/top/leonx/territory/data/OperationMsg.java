package top.leonx.territory.data;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OperationMsg {
    public enum OperationCode{
        DEBUG,
        ADD_CHUNK,
    }

    public OperationMsg(OperationCode code, CompoundNBT nbt)
    {
        this.code=code;
        this.nbt=nbt;
    }
    public OperationCode code;
    public CompoundNBT nbt;
    public static void encode(OperationMsg msg, PacketBuffer buf) {
        buf.writeByte(msg.code.ordinal());
        switch (msg.code)
        {

            case DEBUG:
                break;
            case ADD_CHUNK:
                buf.writeCompoundTag(msg.nbt);
                break;
        }


    }
    public static OperationMsg decode(PacketBuffer buf) {
        OperationCode code=OperationCode.values()[buf.readByte()];
        if(code==OperationCode.ADD_CHUNK)
        {
            return new OperationMsg(code,buf.readCompoundTag());
        }else{
            return new OperationMsg(code,null);
        }

    }
}
