package top.leonx.territory;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import top.leonx.territory.data.TerritoryWorldSavedData;

import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;

public class TerritoryCommand {
    public static void Register(CommandDispatcher<CommandSource> dispatcher)
    {
        //LiteralArgumentBuilder<CommandSource>
        // TODO
        dispatcher.register(Commands.literal("territory").requires(s->s.hasPermissionLevel(4)).executes(context->
                execute(context)).then(Commands.literal("reserve").then(Commands.literal("add").executes((context) -> {
                    ServerPlayerEntity playerEntity=context.getSource().asPlayer();
                    World entityWorld = context.getSource().asPlayer().getEntityWorld();
                    if(!entityWorld.isRemote)
                    {
                        ServerWorld serverWorld=(ServerWorld)entityWorld;
                        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
                        BlockPos pos = playerEntity.getPosition();
                        ChunkPos chunkPos=new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
                        data.addReservedTerritory(chunkPos);
                        context.getSource().sendFeedback(new StringTextComponent(String.format("Reserved Territory [%d,%d] " +
                                        "has been added.",chunkPos.x,chunkPos.z)),
                                false);
                    }
                    return 0;
                }
                )).then(Commands.literal("remove").executes(context -> {
                    ServerPlayerEntity playerEntity=context.getSource().asPlayer();
                    World entityWorld = context.getSource().asPlayer().getEntityWorld();
                    if(!entityWorld.isRemote)
                    {
                        ServerWorld serverWorld=(ServerWorld)entityWorld;
                        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
                        BlockPos pos = playerEntity.getPosition();
                        ChunkPos chunkPos=new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
                        data.removeReservedTerritory(chunkPos);
                        context.getSource().sendFeedback(new StringTextComponent(String.format("Reserved Territory [%d,%d] " +
                                        "has been remove.",chunkPos.x,chunkPos.z)),
                                false);
                    }
                    return 0;
                }
            )).then(Commands.literal("list").executes(context -> {
                ServerPlayerEntity playerEntity=context.getSource().asPlayer();
                World entityWorld = context.getSource().asPlayer().getEntityWorld();
                if(!entityWorld.isRemote)
                {
                    ServerWorld serverWorld=(ServerWorld)entityWorld;
                    TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
                    StringBuilder builder=new StringBuilder();
                    for(int i=0;i<data.reservedTerritory.size();i++){
                        CompoundNBT nbt=data.reservedTerritory.getCompound(i);
                        ChunkPos pos=ConvertNbtToPos(nbt);
                        builder.append(String.format("[%d,%d] ",pos.x,pos.z));
                    }
                    playerEntity.sendMessage(new StringTextComponent(builder.toString()));
                }
                return 0;
            }))
        ));
    }

    private static int execute(CommandContext<CommandSource> context) {
        World entityWorld = null;
        try {
            entityWorld = context.getSource().asPlayer().getEntityWorld();
            if(!entityWorld.isRemote)
            {
                ServerWorld serverWorld=(ServerWorld)entityWorld;
                TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
                context.getSource().sendFeedback(new StringTextComponent(data.testStr),false);
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
