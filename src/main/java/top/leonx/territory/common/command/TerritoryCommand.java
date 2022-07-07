package top.leonx.territory.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraftforge.common.util.LazyOptional;
import top.leonx.territory.init.registry.ModCaps;
import top.leonx.territory.core.PermissionFlag;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.core.TerritoryInfoHolder;
import top.leonx.territory.util.UserUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
public class TerritoryCommand {
    public static void Register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder       = Commands.literal("territory").requires(s -> s.hasPermission(4));
        LiteralArgumentBuilder<CommandSourceStack> resBuilder    = Commands.literal("res");
        LiteralArgumentBuilder<CommandSourceStack> resAddBuilder = Commands.literal("add");
//        resAddBuilder.then(Commands.argument("name",
//                StringArgumentType.string()).then(Commands.argument("permission", IntegerArgumentType.integer()).executes(TerritoryCommand::addSingleResTerritory)));
        resAddBuilder.then(Commands.argument("name",
                StringArgumentType.string()).then(Commands.argument("fromPos", BlockPosArgument.blockPos()).then(Commands.argument("toPos",
                BlockPosArgument.blockPos()).then(Commands.argument("permission", IntegerArgumentType.integer()).executes(TerritoryCommand::addAreaResTerritory)))));
        resBuilder.then(Commands.literal("remove").then(Commands.argument("name",
                StringArgumentType.string()).executes(TerritoryCommand::removeResTerritory)));
        resBuilder.then(Commands.literal("list").executes(TerritoryCommand::listResTerritory));
        LiteralArgumentBuilder<CommandSourceStack> resSetBuilder = Commands.literal("set");
        resSetBuilder.then(Commands.literal("addChunk").then(Commands.argument("name",
                StringArgumentType.string()).executes(TerritoryCommand::setResAddChunk)));
        resSetBuilder.then(Commands.literal("removeChunk").then(Commands.argument("name",
                StringArgumentType.string()).executes(TerritoryCommand::setResRemoveChunk)));
        resSetBuilder.then(Commands.literal("permission").then(Commands.argument("name",
                StringArgumentType.string()).then(Commands.argument("permission", IntegerArgumentType.integer()).executes(TerritoryCommand::setResPermission))));
        resBuilder.then(resAddBuilder);
        resBuilder.then(resSetBuilder);
        resBuilder.then(Commands.literal("tp").then(Commands.argument("name", StringArgumentType.string()).executes(TerritoryCommand::tpTo)));
        builder.then(resBuilder);
        builder.then(Commands.literal("list").executes(TerritoryCommand::listPlayerTerritory));
        builder.then(Commands.literal("check").executes(TerritoryCommand::checkPosTerritory));
        LiteralArgumentBuilder<CommandSourceStack> debug = Commands.literal("debug");
        debug.then(Commands.literal("regen").executes(TerritoryCommand::regen));
        builder.then(debug);
        dispatcher.register(builder);
    }

    private static int tpTo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player     = context.getSource().getPlayerOrException();
        String                  targetName = StringArgumentType.getString(context, "name");
        Set<TerritoryInfo> matched      =
                TerritoryInfoHolder.get(player.getCommandSenderWorld()).TERRITORY_CHUNKS.keySet().stream().filter(t -> t.territoryName.equals(targetName)).collect(Collectors.toSet());
        ChunkPos chunkPos=null;
        if(matched.size()>=1)
        {
            Iterator<TerritoryInfo> first = matched.iterator();
            if(first.hasNext())
            {
                Set<ChunkPos> chunkPosSet = TerritoryInfoHolder.get(player.getCommandSenderWorld()).getAssociatedTerritory(first.next());
                if(chunkPosSet.iterator().hasNext())
                {
                    chunkPos = chunkPosSet.iterator().next();
                }
            }
        }else{
            player.sendMessage(new TextComponent("No such territory"), player.getUUID ());
            return 0;
        }
        BlockPos pos  = chunkPos.getWorldPosition();
        player.connection.teleport(pos.getX(),120,pos.getZ(),0,0);
        return 0;
    }

    private static int capDebug(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer          player     = context.getSource().getPlayerOrException();
        var chunk      = (LevelChunk) player.getCommandSenderWorld().getChunk(player.getOnPos());
        LazyOptional<TerritoryInfo> capability = chunk.getCapability(ModCaps.TERRITORY_INFO_CAPABILITY);
        TerritoryInfo               info       = capability.orElse(new TerritoryInfo());

        player.sendMessage(new TextComponent(Integer.toString(info.defaultPermission.getCode())), Util.NIL_UUID);
        info.defaultPermission = new PermissionFlag(IntegerArgumentType.getInteger(context, "num"));

        return 0;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnstableApiUsage"})
    private static int regen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

//        ServerPlayer player = context.getSource().getPlayerOrException();
//        //ServerChunkProvider chunkProvider = (ServerChunkProvider)world.getChunkProvider();
//
//        ServerLevel originalLevel = (ServerLevel) player.getCommandSenderWorld();
//        ChunkPos    chunkPos      = new ChunkPos(player.func_241140_K_().getX() >> 4, player.func_241140_K_().getZ() >> 4); //func_241140_K_() -> getOnPos
//        File        saveFolder    = Files.createTempDir();
//
//        saveFolder.deleteOnExit();
//
//        MinecraftServer server      = originalWorld.getServer();
//        SaveHandler     saveHandler = new SaveHandler(saveFolder, originalWorld.getSaveHandler().getWorldDirectory().getName(), server, server.getDataFixer());
//        Level freshLevel = new ServerLevel(server, server.getBackgroundExecutor(), saveHandler, originalWorld.getWorldInfo(),
//                originalWorld.dimension.getType(), originalWorld.getProfiler(), new NoopChunkStatusListener());

        //freshWorld.getChunk(chunkPos.x, chunkPos.z);

//        //ForgeLevel from = new ForgeWorld(freshWorld);
//        for (int x = chunkPos.x << 4; x < (chunkPos.x + 1) << 4; x++) {
//            for (int z = chunkPos.z << 4; z < (chunkPos.z + 1) << 4; z++) {
//                for (int y = 0; y < 255; y++) {
//                    BlockPos   pos        = new BlockPos(x, y, z);
//                    TileEntity tileEntity = freshWorld.getTileEntity(pos);
//                    originalWorld.setBlockState(pos, freshWorld.getBlockState(pos));
//                    originalWorld.setTileEntity(pos, tileEntity);
//                }
//            }
//        }
//        saveFolder.delete();
        return 0;
    }

    private static int checkPosTerritory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer playerEntity = context.getSource().getPlayerOrException();
        BlockPos           pos          = playerEntity.getOnPos();
        ChunkPos chunkPos     = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);

        TerritoryInfo info =
                playerEntity.level.getChunk(chunkPos.x, chunkPos.z).getCapability(ModCaps.TERRITORY_INFO_CAPABILITY).orElse(new TerritoryInfo());

        if (!info.IsProtected()) {
            playerEntity.sendMessage(new TextComponent("no territory at " + pos.toString()),Util.NIL_UUID);
        } else {
            playerEntity.sendMessage(new TextComponent(info.toString()),Util.NIL_UUID);
        }

        return 0;
    }

    @SuppressWarnings("DuplicatedCode")
    private static int setResPermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer playerEntity = context.getSource().getPlayerOrException();
        Level world        = playerEntity.getCommandSenderWorld();

        if (world.isClientSide) return 1;
        ServerLevel serverLevel         = (ServerLevel) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverLevel).TERRITORY_CHUNKS.keySet().stream().filter(t->Objects.equals(t.territoryName,targetTerritoryName)).findFirst();

        if (first.isPresent()) {

            TerritoryInfo oldInfo = first.get();
            TerritoryInfo newInfo = oldInfo.copy();
            newInfo.defaultPermission = new PermissionFlag(IntegerArgumentType.getInteger(context, "permission"));

            TerritoryInfoHolder.get(serverLevel).updateReserveTerritory(oldInfo, newInfo);
            playerEntity.sendMessage(new TextComponent("Success"),Util.NIL_UUID);
        } else {
            playerEntity.sendMessage(new TextComponent("No Such Territory"),Util.NIL_UUID);
        }
        return 0;
    }

    private static int listPlayerTerritory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        HashSet<TerritoryInfo> infos        = new HashSet<>();
        ServerPlayer     playerEntity = context.getSource().getPlayerOrException();

        TerritoryInfoHolder.get(playerEntity.level).TERRITORY_CHUNKS.keySet().forEach(t -> {
            if (t.ownerId != UserUtil.DEFAULT_UUID)
                infos.add(t);
        });

        StringBuilder stringBuilder = new StringBuilder();
        infos.forEach(t -> stringBuilder.append(t.toString()).append("\n"));
        playerEntity.sendMessage(new TextComponent(stringBuilder.toString()),Util.NIL_UUID);
        return 0;
    }

    @SuppressWarnings("DuplicatedCode")
    private static int setResAddChunk(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer playerEntity = context.getSource().getPlayerOrException();
        Level              world        = playerEntity.getCommandSenderWorld();

        if (world.isClientSide) return 1;

        BlockPos    blockPos            = playerEntity.getOnPos();
        ChunkPos    chunkPos            = new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        ServerLevel serverLevel         = (ServerLevel) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverLevel).TERRITORY_CHUNKS.keySet().stream().filter(t -> Objects.equals(t.territoryName,targetTerritoryName)).findFirst();

        if (first.isPresent()) {
            TerritoryInfo oldInfo = first.get();
            TerritoryInfoHolder.get(serverLevel).addReservedTerritory(oldInfo, chunkPos);
            playerEntity.sendMessage(new TextComponent("Success"),Util.NIL_UUID);
        } else {
            playerEntity.sendMessage(new TextComponent("No Such Territory"),Util.NIL_UUID);
        }

        return 0;
    }

    @SuppressWarnings("DuplicatedCode")
    private static int setResRemoveChunk(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer playerEntity = context.getSource().getPlayerOrException();
        Level              world        = playerEntity.getCommandSenderWorld();
        if (world.isClientSide) return 1;
        BlockPos    blockPos            = playerEntity.getOnPos();
        ChunkPos    chunkPos            = new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        ServerLevel serverLevel         = (ServerLevel) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverLevel).TERRITORY_CHUNKS.keySet().stream().filter(t->Objects.equals(t.territoryName,targetTerritoryName)).findFirst();
        if (first.isPresent()) {
            TerritoryInfo oldInfo = first.get();
            TerritoryInfoHolder.get(serverLevel).removeReservedTerritory(oldInfo, Collections.singleton(chunkPos));
            playerEntity.sendMessage(new TextComponent("Success"),Util.NIL_UUID);
        } else {
            playerEntity.sendMessage(new TextComponent("No Such Territory"),Util.NIL_UUID);
        }
        return 0;
    }

    private static int listResTerritory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer playerEntity = context.getSource().getPlayerOrException();
        Level              world        = playerEntity.getCommandSenderWorld();
        if (world.isClientSide) return 1;
        ServerLevel            serverLevel   = (ServerLevel) world;
        StringBuilder          stringBuilder = new StringBuilder();
        HashSet<TerritoryInfo> infos         = new HashSet<>(TerritoryInfoHolder.get(serverLevel).TERRITORY_CHUNKS.keySet());
        infos.forEach(t -> {
            if (Objects.equals(t.ownerId, UserUtil.DEFAULT_UUID))
                stringBuilder.append(t.toString()).append("\n");
        });
        playerEntity.sendMessage(new TextComponent(stringBuilder.toString()),Util.NIL_UUID);
        return 0;
    }

    private static int removeResTerritory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer playerEntity = context.getSource().getPlayerOrException();
        Level              world        = playerEntity.getCommandSenderWorld();
        if (world.isClientSide) return 1;
        ServerLevel serverLevel         = (ServerLevel) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverLevel).TERRITORY_CHUNKS.keySet().stream().filter(t->Objects.equals(t.territoryName,targetTerritoryName)).findFirst();
        if (first.isPresent()) {
            TerritoryInfoHolder.get(serverLevel).removeReservedTerritory(first.get());
            playerEntity.sendMessage(new TextComponent("Success"),Util.NIL_UUID);
        } else {
            playerEntity.sendMessage(new TextComponent("No Such Territory"),Util.NIL_UUID);
        }
        return 0;
    }

    private static int addAreaResTerritory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer playerEntity = context.getSource().getPlayerOrException();
        Level              world        = playerEntity.getCommandSenderWorld();

        if (world.isClientSide) return 1;

        String            targetTerritoryName = StringArgumentType.getString(context, "name");
        BlockPos          fromPos             = BlockPosArgument.getLoadedBlockPos(context, "fromPos");
        BlockPos          toPos               = BlockPosArgument.getLoadedBlockPos(context, "toPos");
        PermissionFlag    permission          = new PermissionFlag(IntegerArgumentType.getInteger(context, "permission"));
        int               fromChunkX          = fromPos.getX() >> 4;
        int               fromChunkZ          = fromPos.getZ() >> 4;
        int               toChunkX            = toPos.getX() >> 4;
        int               toChunkZ            = toPos.getZ() >> 4;
        HashSet<ChunkPos> territories         = new HashSet<>();
        Stream<TerritoryInfo> nameMatched = TerritoryInfoHolder.get(world).TERRITORY_CHUNKS.keySet().stream().filter(t -> targetTerritoryName.equals(t.territoryName));
        if(nameMatched.count()>0)
        {
            playerEntity.sendMessage(new TextComponent("There has been territory called "+targetTerritoryName),Util.NIL_UUID);
            return 0;
        }
        for (int x = Math.min(fromChunkX, toChunkX); x <= Math.max(fromChunkX, toChunkX); x++) {
            for (int z = Math.min(fromChunkZ, toChunkZ); z <= Math.max(fromChunkZ, toChunkZ); z++) {
                territories.add(new ChunkPos(x, z));
            }
        }

        TerritoryInfo territoryInfo = new TerritoryInfo();
        ServerLevel   serverLevel   = (ServerLevel) world;
        TerritoryInfoHolder.get(serverLevel).addReservedTerritory(targetTerritoryName, permission, territories);

        playerEntity.sendMessage(new TextComponent("Success"),Util.NIL_UUID);

        return 0;
    }

    private static class NoopChunkStatusListener implements ChunkStatusUpdateListener {

        @Override
        public void onChunkStatusChange(ChunkPos pPos, ChunkHolder.FullChunkStatus pStatus) {

        }
    }
}
