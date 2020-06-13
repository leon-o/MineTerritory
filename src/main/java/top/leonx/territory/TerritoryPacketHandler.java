package top.leonx.territory;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import top.leonx.territory.container.TerritoryContainer;
import top.leonx.territory.data.OperationMsg;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TerritoryPacketHandler {
    public static SimpleChannel CHANNEL;

    //The pos relative to mapLeftTopChunkPos
    Set<ChunkPos> availableChunkPos = new HashSet<>();
    static int id=0;
    public static void Init()
    {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(TerritoryMod.MODID, "main"),
                () -> "1",
                "1"::equals,
                "1"::equals
        );


        CHANNEL.registerMessage(id++, OperationMsg.class,OperationMsg::encode,OperationMsg::decode,
                TerritoryPacketHandler::handle);
    }
    public static <T> void registerMessage(Class<T> type,
    BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer,
            T> decoder, BiConsumer<T,Supplier<NetworkEvent.Context>> handler)
    {
       CHANNEL.registerMessage(id++,type,encoder,decoder,handler);
    }

    public static void handle(OperationMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {

            ServerPlayerEntity sender = contextSupplier.get().getSender(); // the client that sent this packet


            switch (msg.code){
                case DEBUG:

                case ADD_CHUNK:
//
//                    CompoundNBT nbt=msg.nbt;
//                    ChunkPos chunkPos=new ChunkPos(nbt.getInt("chunk_x"),nbt.getInt("chunk_z"));
//                    if(sender.openContainer instanceof TerritoryContainer)
//                    {
//                        TerritoryContainer territoryContainer=(TerritoryContainer)sender.openContainer;
//                        territoryContainer.addToTileEntityServerSide(sender,chunkPos);
//                    }
//                    sender.openContainer.detectAndSendChanges();
//                    break;
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public static void SendAddJurisdictionToServer(ChunkPos chunkPos)
    {
        CompoundNBT nbt=new CompoundNBT();

        nbt.putInt("chunk_x",chunkPos.x);
        nbt.putInt("chunk_z",chunkPos.z);

        CHANNEL.sendToServer(new OperationMsg(OperationMsg.OperationCode.ADD_CHUNK,nbt));
    }

}
