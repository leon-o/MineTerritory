package top.leonx.territory.command;

import com.google.common.io.Files;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.util.LazyOptional;
import top.leonx.territory.capability.ModCapabilities;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
public class TerritoryCommand {
    public static void Register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder       = Commands.literal("territory").requires(s -> s.hasPermissionLevel(4));
        LiteralArgumentBuilder<CommandSource> resBuilder    = Commands.literal("res");
        LiteralArgumentBuilder<CommandSource> resAddBuilder = Commands.literal("add");
//        resAddBuilder.then(Commands.argument("name",
//                StringArgumentType.string()).then(Commands.argument("permission", IntegerArgumentType.integer()).executes(TerritoryCommand::addSingleResTerritory)));
        resAddBuilder.then(Commands.argument("name",
                StringArgumentType.string()).then(Commands.argument("fromPos", BlockPosArgument.blockPos()).then(Commands.argument("toPos",
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
                StringArgumentType.string()).then(Commands.argument("permission", IntegerArgumentType.integer()).executes(TerritoryCommand::setResPermission))));
        resBuilder.then(resAddBuilder);
        resBuilder.then(resSetBuilder);
        resBuilder.then(Commands.literal("tp").then(Commands.argument("name", StringArgumentType.string()).executes(TerritoryCommand::tpTo)));
        builder.then(resBuilder);
        builder.then(Commands.literal("list").executes(TerritoryCommand::listPlayerTerritory));
        builder.then(Commands.literal("check").executes(TerritoryCommand::checkPosTerritory));
        LiteralArgumentBuilder<CommandSource> debug = Commands.literal("debug");
        debug.then(Commands.literal("regen").executes(TerritoryCommand::regen));
        builder.then(debug);
        dispatcher.register(builder);
    }

    private static int tpTo(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity      player     = context.getSource().asPlayer();
        String                  targetName = StringArgumentType.getString(context, "name");
        Set<TerritoryInfo> matched      =
                TerritoryInfoHolder.get(player.getEntityWorld()).TERRITORY_CHUNKS.keySet().stream().filter(t -> t.territoryName.equals(targetName)).collect(Collectors.toSet());
        ChunkPos chunkPos=null;
        if(matched.size()>=1)
        {
            Iterator<TerritoryInfo> first = matched.iterator();
            if(first.hasNext())
            {
                Set<ChunkPos> chunkPosSet = TerritoryInfoHolder.get(player.getEntityWorld()).getAssociatedTerritory(first.next());
                if(chunkPosSet.iterator().hasNext())
                {
                    chunkPos = chunkPosSet.iterator().next();
                }
            }
        }else{
            player.sendMessage(new StringTextComponent("No such territory"));
            return 0;
        }
        BlockPos pos  = chunkPos.asBlockPos();
        player.connection.setPlayerLocation(pos.getX(),120,pos.getZ(),0,0);
        return 0;
    }

    private static int capDebug(CommandContext<CommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity          player     = context.getSource().asPlayer();
        Chunk                       chunk      = (Chunk) player.getEntityWorld().getChunk(player.getPosition());
        LazyOptional<TerritoryInfo> capability = chunk.getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY);
        TerritoryInfo               info       = capability.orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());

        player.sendMessage(new StringTextComponent(Integer.toString(info.defaultPermission.getCode())));
        info.defaultPermission = new PermissionFlag(IntegerArgumentType.getInteger(context, "num"));
        chunk.markDirty();

        return 0;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnstableApiUsage"})
    private static int regen(CommandContext<CommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity player = context.getSource().asPlayer();
        //ServerChunkProvider chunkProvider = (ServerChunkProvider)world.getChunkProvider();

        ServerWorld originalWorld = (ServerWorld) player.getEntityWorld();
        ChunkPos    chunkPos      = new ChunkPos(player.getPosition().getX() >> 4, player.getPosition().getZ() >> 4);
        File        saveFolder    = Files.createTempDir();

        saveFolder.deleteOnExit();

        MinecraftServer server      = originalWorld.getServer();
        SaveHandler     saveHandler = new SaveHandler(saveFolder, originalWorld.getSaveHandler().getWorldDirectory().getName(), server, server.getDataFixer());
        World freshWorld = new ServerWorld(server, server.getBackgroundExecutor(), saveHandler, originalWorld.getWorldInfo(),
                originalWorld.dimension.getType(), originalWorld.getProfiler(), new NoopChunkStatusListener());

        freshWorld.getChunk(chunkPos.x, chunkPos.z);

        //ForgeWorld from = new ForgeWorld(freshWorld);
        for (int x = chunkPos.x << 4; x < (chunkPos.x + 1) << 4; x++) {
            for (int z = chunkPos.z << 4; z < (chunkPos.z + 1) << 4; z++) {
                for (int y = 0; y < 255; y++) {
                    BlockPos   pos        = new BlockPos(x, y, z);
                    TileEntity tileEntity = freshWorld.getTileEntity(pos);
                    originalWorld.setBlockState(pos, freshWorld.getBlockState(pos));
                    originalWorld.setTileEntity(pos, tileEntity);
                }
            }
        }
        saveFolder.delete();
        return 0;
    }

    private static int checkPosTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        BlockPos           pos          = playerEntity.getPosition();
        ChunkPos           chunkPos     = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);

        TerritoryInfo info =
                playerEntity.world.getChunk(chunkPos.x, chunkPos.z).getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY).orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());

        if (!info.IsProtected()) {
            playerEntity.sendMessage(new StringTextComponent("no territory at " + pos.toString()));
        } else {
            playerEntity.sendMessage(new StringTextComponent(info.toString()));
        }

        return 0;
    }

    @SuppressWarnings("DuplicatedCode")
    private static int setResPermission(CommandContext<CommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        World              world        = playerEntity.getEntityWorld();

        if (world.isRemote) return 1;
        ServerWorld serverWorld         = (ServerWorld) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverWorld).TERRITORY_CHUNKS.keySet().stream().filter(t->Objects.equals(t.territoryName,targetTerritoryName)).findFirst();

        if (first.isPresent()) {

            TerritoryInfo oldInfo = first.get();
            TerritoryInfo newInfo = oldInfo.copy();
            newInfo.defaultPermission = new PermissionFlag(IntegerArgumentType.getInteger(context, "permission"));

            TerritoryInfoHolder.get(serverWorld).updateReserveTerritory(oldInfo, newInfo);
            playerEntity.sendMessage(new StringTextComponent("Success"));
        } else {
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }
        return 0;
    }

    private static int listPlayerTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {

        HashSet<TerritoryInfo> infos        = new HashSet<>();
        ServerPlayerEntity     playerEntity = context.getSource().asPlayer();

        TerritoryInfoHolder.get(playerEntity.world).TERRITORY_CHUNKS.keySet().forEach(t -> {
            if (t.ownerId != UserUtil.DEFAULT_UUID)
                infos.add(t);
        });

        StringBuilder stringBuilder = new StringBuilder();
        infos.forEach(t -> stringBuilder.append(t.toString()).append("\n"));
        playerEntity.sendMessage(new StringTextComponent(stringBuilder.toString()));
        return 0;
    }

    @SuppressWarnings("DuplicatedCode")
    private static int setResAddChunk(CommandContext<CommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        World              world        = playerEntity.getEntityWorld();

        if (world.isRemote) return 1;

        BlockPos    blockPos            = playerEntity.getPosition();
        ChunkPos    chunkPos            = new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        ServerWorld serverWorld         = (ServerWorld) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverWorld).TERRITORY_CHUNKS.keySet().stream().filter(t -> Objects.equals(t.territoryName,targetTerritoryName)).findFirst();

        if (first.isPresent()) {
            TerritoryInfo oldInfo = first.get();
            TerritoryInfoHolder.get(serverWorld).addReservedTerritory(oldInfo, chunkPos);
            playerEntity.sendMessage(new StringTextComponent("Success"));
        } else {
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }

        return 0;
    }

    @SuppressWarnings("DuplicatedCode")
    private static int setResRemoveChunk(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        World              world        = playerEntity.getEntityWorld();
        if (world.isRemote) return 1;
        BlockPos    blockPos            = playerEntity.getPosition();
        ChunkPos    chunkPos            = new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        ServerWorld serverWorld         = (ServerWorld) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverWorld).TERRITORY_CHUNKS.keySet().stream().filter(t->Objects.equals(t.territoryName,targetTerritoryName)).findFirst();
        if (first.isPresent()) {
            TerritoryInfo oldInfo = first.get();
            TerritoryInfoHolder.get(serverWorld).removeReservedTerritory(oldInfo, Collections.singleton(chunkPos));
            playerEntity.sendMessage(new StringTextComponent("Success"));
        } else {
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }
        return 0;
    }

    private static int listResTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        World              world        = playerEntity.getEntityWorld();
        if (world.isRemote) return 1;
        ServerWorld            serverWorld   = (ServerWorld) world;
        StringBuilder          stringBuilder = new StringBuilder();
        HashSet<TerritoryInfo> infos         = new HashSet<>(TerritoryInfoHolder.get(serverWorld).TERRITORY_CHUNKS.keySet());
        infos.forEach(t -> {
            if (Objects.equals(t.ownerId, UserUtil.DEFAULT_UUID))
                stringBuilder.append(t.toString()).append("\n");
        });
        playerEntity.sendMessage(new StringTextComponent(stringBuilder.toString()));
        return 0;
    }

    private static int removeResTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        World              world        = playerEntity.getEntityWorld();
        if (world.isRemote) return 1;
        ServerWorld serverWorld         = (ServerWorld) world;
        String      targetTerritoryName = StringArgumentType.getString(context, "name");
        Optional<TerritoryInfo> first =
                TerritoryInfoHolder.get(serverWorld).TERRITORY_CHUNKS.keySet().stream().filter(t->Objects.equals(t.territoryName,targetTerritoryName)).findFirst();
        if (first.isPresent()) {
            TerritoryInfoHolder.get(serverWorld).removeReservedTerritory(first.get());
            playerEntity.sendMessage(new StringTextComponent("Success"));
        } else {
            playerEntity.sendMessage(new StringTextComponent("No Such Territory"));
        }
        return 0;
    }

    private static int addAreaResTerritory(CommandContext<CommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        World              world        = playerEntity.getEntityWorld();

        if (world.isRemote) return 1;

        String            targetTerritoryName = StringArgumentType.getString(context, "name");
        BlockPos          fromPos             = BlockPosArgument.getBlockPos(context, "fromPos");
        BlockPos          toPos               = BlockPosArgument.getBlockPos(context, "toPos");
        PermissionFlag    permission          = new PermissionFlag(IntegerArgumentType.getInteger(context, "permission"));
        int               fromChunkX          = fromPos.getX() >> 4;
        int               fromChunkZ          = fromPos.getZ() >> 4;
        int               toChunkX            = toPos.getX() >> 4;
        int               toChunkZ            = toPos.getZ() >> 4;
        HashSet<ChunkPos> territories         = new HashSet<>();
        Stream<TerritoryInfo> nameMatched = TerritoryInfoHolder.get(world).TERRITORY_CHUNKS.keySet().stream().filter(t -> targetTerritoryName.equals(t.territoryName));
        if(nameMatched.count()>0)
        {
            playerEntity.sendMessage(new StringTextComponent("There has been territory called "+targetTerritoryName));
            return 0;
        }
        for (int x = Math.min(fromChunkX, toChunkX); x <= Math.max(fromChunkX, toChunkX); x++) {
            for (int z = Math.min(fromChunkZ, toChunkZ); z <= Math.max(fromChunkZ, toChunkZ); z++) {
                territories.add(new ChunkPos(x, z));
            }
        }

        TerritoryInfo territoryInfo = new TerritoryInfo();
        ServerWorld   serverWorld   = (ServerWorld) world;
        TerritoryInfoHolder.get(serverWorld).addReservedTerritory(targetTerritoryName, permission, territories);

        playerEntity.sendMessage(new StringTextComponent("Success"));

        return 0;
    }

    private static class NoopChunkStatusListener implements IChunkStatusListener {
        @Override
        public void start(@Nonnull ChunkPos center) {
        }

        @Override
        public void statusChanged(@Nonnull ChunkPos p_219508_1_, ChunkStatus p_219508_2_) {
        }

        @Override
        public void stop() {
        }
    }
}
