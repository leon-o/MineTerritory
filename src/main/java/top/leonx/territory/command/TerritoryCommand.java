package top.leonx.territory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryWorldSavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class TerritoryCommand {
    public static void Register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> builder=Commands.literal("territory").requires(s->s.hasPermissionLevel(4));
        LiteralArgumentBuilder<CommandSource> resBuilder = Commands.literal("res");
        LiteralArgumentBuilder<CommandSource> resAddBuilder=Commands.literal("add");
        resAddBuilder.then(Commands.argument("name",
                StringArgumentType.string()).then(Commands.argument("permission",IntegerArgumentType.integer()).executes(TerritoryCommand::addSingleResTerritory)));
        resAddBuilder.then(Commands.argument("name",
                StringArgumentType.string()).then(Commands.argument("fromPos",BlockPosArgument.blockPos()).then(Commands.argument("toPos",
                BlockPosArgument.blockPos()).then(Commands.argument("permission", IntegerArgumentType.integer()).executes(TerritoryCommand::addAreaResTerritory)))));
        resBuilder.then(Commands.literal("remove").then(Commands.argument("name",
                StringArgumentType.string()).executes(TerritoryCommand::removeResTerritory)));
        resBuilder.then(Commands.literal("list").executes(TerritoryCommand::listResTerritory));
        LiteralArgumentBuilder<CommandSource> resSetBuilder = Commands.literal("set");
        resSetBuilder.then(Commands.literal("addChunk").then(Commands.argument("name",
                StringArgumentType.string()).executes(TerritoryCommand::setResAddChunk)));
        resSetBuilder.then(Commands.literal("removeChunk").then(Commands.argument("name",
                StringArgumentType.string()).executes(TerritoryCommand::setResRemoveChunk)));
        resSetBuilder.then(Commands.literal("permission").then(Commands.argument("name",
                StringArgumentType.string()).then(Commands.argument("permission",IntegerArgumentType.integer()).executes(TerritoryCommand::setResPermission))));
        resBuilder.then(resAddBuilder);
        resBuilder.then(resSetBuilder);
        builder.then(resBuilder);
        dispatcher.register(builder);
    }


    @SuppressWarnings("DuplicatedCode")
    private static int setResPermission(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity=context.getSource().asPlayer();
        World world = playerEntity.getEntityWorld();
        if(world.isRemote) return 1;
        ServerWorld serverWorld=(ServerWorld)world;
        String targetTerritoryName=StringArgumentType.getString(context,"name");
        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
        Optional<TerritoryInfo> first = data.territoryInfos.stream().filter(t -> t.territoryName.equals(targetTerritoryName)).findFirst();
        if(first.isPresent())
        {
            TerritoryInfo oldInfo=first.get();
            TerritoryInfo newInfo=oldInfo.copy();
            newInfo.defaultPermission=new PermissionFlag(IntegerArgumentType.getInteger(context,"permission"));
            data.updateReservedTerritory(oldInfo,newInfo);
            playerEntity.sendMessage(new StringTextComponent("Success"));
        }else{
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }
        return 0;
    }

    @SuppressWarnings("DuplicatedCode")
    private static int setResAddChunk(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity=context.getSource().asPlayer();
        World world = playerEntity.getEntityWorld();
        if(world.isRemote) return 1;
        BlockPos blockPos = playerEntity.getPosition();
        ChunkPos chunkPos=new ChunkPos(blockPos.getX()>>4,blockPos.getZ()>>4);
        ServerWorld serverWorld=(ServerWorld)world;
        String targetTerritoryName=StringArgumentType.getString(context,"name");
        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
        Optional<TerritoryInfo> first = data.territoryInfos.stream().filter(t -> t.territoryName.equals(targetTerritoryName)).findFirst();
        if(first.isPresent())
        {
            TerritoryInfo oldInfo = first.get();
            TerritoryInfo newInfo=oldInfo.copy();
            if(newInfo.territories.add(chunkPos))
            {
                data.updateReservedTerritory(oldInfo,newInfo);
                playerEntity.sendMessage(new StringTextComponent("Success"));
            }
        }else{
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }
        return 0;
    }
    @SuppressWarnings("DuplicatedCode")
    private static int setResRemoveChunk(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity=context.getSource().asPlayer();
        World world = playerEntity.getEntityWorld();
        if(world.isRemote) return 1;
        BlockPos blockPos = playerEntity.getPosition();
        ChunkPos chunkPos=new ChunkPos(blockPos.getX()>>4,blockPos.getZ()>>4);
        ServerWorld serverWorld=(ServerWorld)world;
        String targetTerritoryName=StringArgumentType.getString(context,"name");
        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
        Optional<TerritoryInfo> first = data.territoryInfos.stream().filter(t -> t.territoryName.equals(targetTerritoryName)).findFirst();
        if(first.isPresent())
        {
            TerritoryInfo oldInfo = first.get();
            TerritoryInfo newInfo=oldInfo.copy();
            if(newInfo.territories.remove(chunkPos))
            {
                data.updateReservedTerritory(oldInfo,newInfo);
                playerEntity.sendMessage(new StringTextComponent("Success"));
            }
        }else{
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }
        return 0;
    }

    private static int listResTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity=context.getSource().asPlayer();
        World world = playerEntity.getEntityWorld();
        if(world.isRemote) return 1;
        ServerWorld serverWorld=(ServerWorld)world;
        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
        StringBuilder stringBuilder=new StringBuilder();
        data.territoryInfos.forEach(t->{
            ChunkPos pos = t.territories.iterator().next();
            stringBuilder.append(String.format("[%s,(%d,%d),%d] ",t.territoryName,pos.x,pos.z,t.defaultPermission.getCode()));
        });
        playerEntity.sendMessage(new StringTextComponent(stringBuilder.toString()));
        return 0;
    }

    private static int removeResTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity=context.getSource().asPlayer();
        World world = playerEntity.getEntityWorld();
        if(world.isRemote) return 1;
        ServerWorld serverWorld=(ServerWorld)world;
        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
        String targetTerritoryName=StringArgumentType.getString(context,"name");
        Optional<TerritoryInfo> first = data.territoryInfos.stream().filter(t -> t.territoryName.equals(targetTerritoryName)).findFirst();
        if(first.isPresent())
        {
            data.removeReservedTerritory(first.get());
        }else{
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }
        return 0;
    }

    private static int addAreaResTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity=context.getSource().asPlayer();
        World world = playerEntity.getEntityWorld();
        if(world.isRemote) return 1;
        String targetTerritoryName=StringArgumentType.getString(context,"name");
        BlockPos fromPos = BlockPosArgument.getBlockPos(context, "fromPos");
        BlockPos toPos = BlockPosArgument.getBlockPos(context, "toPos");
        PermissionFlag permission=new PermissionFlag(IntegerArgumentType.getInteger(context,"permission"));
        int fromChunkX=fromPos.getX()>>4;
        int fromChunkZ=fromPos.getZ()>>4;
        int toChunkX=toPos.getX()>>4;
        int toChunkZ=toPos.getZ()>>4;
        HashSet<ChunkPos> territories=new HashSet<>();
        for (int x=fromChunkX;x<=toChunkX;x++)
        {
            for (int z=fromChunkZ;z<=toChunkZ;z++)
            {
                territories.add(new ChunkPos(x,z));
            }
        }
        TerritoryInfo territoryInfo=new TerritoryInfo(targetTerritoryName,null,territories,new HashMap<>(),permission);
        ServerWorld serverWorld=(ServerWorld)world;
        TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
        data.addReservedTerritory(territoryInfo);
        return 0;
    }

    private static int addSingleResTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity=context.getSource().asPlayer();
        World entityWorld = context.getSource().asPlayer().getEntityWorld();
        if(!entityWorld.isRemote)
        {
            ServerWorld serverWorld=(ServerWorld)entityWorld;
            TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
            BlockPos pos = playerEntity.getPosition();
            ChunkPos chunkPos=new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
            PermissionFlag permission=new PermissionFlag(IntegerArgumentType.getInteger(context,"permission"));
            data.addReservedTerritory(new TerritoryInfo("",null,Collections.singleton(chunkPos),Collections.emptyMap(),permission));
            context.getSource().sendFeedback(new StringTextComponent(String.format("Reserved Territory [%d,%d] " +
                            "has been added.",chunkPos.x,chunkPos.z)),
                    false);
        }
        return 0;
    }
//    private static int execute(CommandContext<CommandSource> context) {
//        World entityWorld = null;
//        try {
//            entityWorld = context.getSource().asPlayer().getEntityWorld();
//            if(!entityWorld.isRemote)
//            {
//                ServerWorld serverWorld=(ServerWorld)entityWorld;
//                TerritoryWorldSavedData data = TerritoryWorldSavedData.get(serverWorld);
//                context.getSource().sendFeedback(new StringTextComponent(data.testStr),false);
//            }
//        } catch (CommandSyntaxException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }
}
