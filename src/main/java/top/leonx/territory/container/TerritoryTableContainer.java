package top.leonx.territory.container;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.client.gui.WMapWidget;
import top.leonx.territory.data.ComponentTypes;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.util.MessageUtil;
import top.leonx.territory.util.TerritoryUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class TerritoryTableContainer extends SyncedGuiDescription {

    /*static {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            *//*TerritoryPacketHandler.registerMessage(1,TerritoryOperationMsg.class, TerritoryOperationMsg::encode,
                                                   TerritoryOperationMsg::decode,
                                                   TerritoryTableContainer::handler);*//*

            ClientPlayNetworking.registerGlobalReceiver(TerritoryPacketHandler.CHANNEL, new TerritoryMsgClientReceiver());
        }
        ServerPlayNetworking.registerGlobalReceiver(TerritoryPacketHandler.CHANNEL, new TerritoryMsgServerReceiver());
    }*/

    public final BlockPos tileEntityPos;
    public final ChunkPos tileEntityChunkPos;
    public final TerritoryInfo territoryInfo;
    public final Set<ChunkPos> territories = new HashSet<>();
    public final Set<ChunkPos> selectableChunkPos = new HashSet<>();
    public final Set<ChunkPos> removableChunkPos = new HashSet<>();
    public final Set<ChunkPos> forbiddenChunkPos = new HashSet<>();
    private final PlayerEntity player;
    private final Set<ChunkPos> originalTerritories = new HashSet<>();
    public Identifier mapLocation;
    public ChunkPos mapLeftTopChunkPos;
    public int protectPower;

    public TerritoryTableContainer(int id, PlayerInventory inventory, PacketByteBuf buffer) {
        this(id, inventory, getTileEntity(inventory, buffer));
    }

    public TerritoryTableContainer(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModContainerTypes.TERRITORY_CONTAINER, syncId, inventory, getBlockInventory(context, 32), getBlockPropertyDelegate(context));
        this.player = inventory.player;
        var tileEntity = (TerritoryTableTileEntity) context.get(World::getBlockEntity).get();
        this.territoryInfo = tileEntity.getTerritoryInfo().copy();

        tileEntityPos = tileEntity.getPos();
        tileEntityChunkPos = new ChunkPos(tileEntityPos.getX() >> 4, tileEntityPos.getZ() >> 4);
        mapLeftTopChunkPos = new ChunkPos((tileEntityPos.getX() >> 4) - 4, (tileEntityPos.getZ() >> 4) - 4);
        territories.addAll(tileEntity.territories);
        originalTerritories.addAll(tileEntity.territories);

        if (Objects.requireNonNull(tileEntity.getWorld()).isClient) {

            for (int x = mapLeftTopChunkPos.x; x < mapLeftTopChunkPos.x + 9; x++) {
                for (int z = mapLeftTopChunkPos.z; z < mapLeftTopChunkPos.z + 9; z++) {
                    if(territories.contains(new ChunkPos(x,z))) continue;
                    Chunk chunk = tileEntity.getWorld().getChunk(x, z, ChunkStatus.EMPTY,false);
                    if(chunk!=null){
                        TerritoryInfo info = ComponentTypes.WORLD_TERRITORY_INFO.get(chunk);//chunk.getCapability(ModCapabilities.TERRITORY_INFO_CAPABILITY).orElse(ModCapabilities.TERRITORY_INFO_CAPABILITY.getDefaultInstance());
                        if (info.IsProtected() && !info.equals(territoryInfo))
                            forbiddenChunkPos.add(new ChunkPos(x, z));
                    }
                }
            }

            initChunkInfo();
        }
        protectPower = tileEntity.computeProtectPower();
        if(FabricLoader.getInstance().getEnvironmentType()== EnvType.CLIENT)
            mapLocation=tileEntity.mapLocation;


        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(336, 200);
        root.setInsets(Insets.ROOT_PANEL);
        WMapWidget map = new WMapWidget(tileEntity.mapLocation,180);
        root.add(map,0,1);
        var center= this.tileEntityChunkPos;
        map.SetChunkInfo(List.of(new ChunkPos(center.x+1,center.z)),
                         List.of(new ChunkPos(center.x,center.z+1)),
                         List.of(new ChunkPos(center.x+1,center.z+1)),
                         List.of(new ChunkPos(center.x,center.z)),
                         center,tileEntity.mapSize);
        var listPanel = new WListPanel<String,WText>(List.of("a","b","c"),
                                                     ()->new WText(new LiteralText("a")),(d,s)->{
            s.setText(new LiteralText(d));
        });
        /*var cardPanel = new WScrollPanel();
        cardPanel.add(new WText(new LiteralText("a")));
        cardPanel.add(new WText(new LiteralText("b")));
        cardPanel.add(new WText(new LiteralText("3")));
        cardPanel.layout();*/
        root.add(listPanel, 11, 1, 6, 3);
        root.add(new WButton(new TranslatableText("gui.territory.permission_btn")),11,8,6,1);
        root.add(new WButton(new TranslatableText("gui.territory.done_btn")),11,10,6,1);

        root.validate(this);
    }

    private static ScreenHandlerContext getTileEntity(PlayerInventory inventory, PacketByteBuf buffer) {
        //final BlockEntity tileAtPos = inventory.player.world.getBlockEntity(buffer.readBlockPos());
        return ScreenHandlerContext.create(inventory.player.world,buffer.readBlockPos());//tileAtPos;
    }

    /*private static void handler(TerritoryOperationMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayerEntity sender = contextSupplier.get().getSender();
            if (sender == null)//client side ,when success
            {
                MinecraftClient.getInstance().player.closeScreen();
            } else {
                TerritoryTableContainer container = (TerritoryTableContainer) sender.openContainer;
                if (!container.updateTileEntityServerSide(sender, msg)) return;

                World world = container.player.world;
                BlockState state = world.getBlockState(container.tileEntityPos);
                world.notifyBlockUpdate(container.tileEntityPos, state, state, 2); //notify all clients to update.

                TerritoryPacketHandler.CHANNEL.sendTo(msg, sender.connection.netManager,
                        NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }*/


    //private final TerritoryTileEntity tileEntity; Should avoid directly operating the Tile Entity directly on the client side


    public boolean updateTileEntityServerSide(ServerPlayerEntity player, TerritoryOperationMsg msg) {
        if (originalTerritories.size() + msg.readyAdd.length - msg.readyRemove.length > protectPower)
            return false;

        TerritoryTableTileEntity tileEntity = (TerritoryTableTileEntity) player.world.getBlockEntity(tileEntityPos);
        if (!player.isCreative()) {
            int experienceNeed = (int) Math.round((msg.readyAdd.length-msg.readyRemove.length)* TerritoryConfig.expNeededPerChunk);
            if (player.experienceLevel >= experienceNeed) {
                player.addExperienceLevels(-experienceNeed);
            } else {
                player.sendMessage(new TranslatableText("message.territory.need_experience", Integer.toString(experienceNeed)).setStyle(MessageUtil.YELLOW),
                                   MessageType.GAME_INFO, Util.NIL_UUID);
                return false;
            }
        }
        player.world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                               SoundCategory.BLOCKS, 2F, 1F);
        for (ChunkPos pos : msg.readyRemove) {
            tileEntity.territories.remove(pos);
        }

        Collections.addAll(tileEntity.territories, msg.readyAdd);

        tileEntity.getTerritoryInfo().permissions = msg.permissions;
        tileEntity.getTerritoryInfo().defaultPermission = msg.defaultPermission;
        tileEntity.getTerritoryInfo().territoryName = msg.territoryName;
        tileEntity.markDirty();
        tileEntity.updateTerritoryToWorld();
        return true;
    }
    public int getXpRequired()
    {
        if (!player.isCreative()) {
            return  (int) Math.round((territories.size()- originalTerritories.size())* TerritoryConfig.expNeededPerChunk);
        }
        return 0;
    }
    public int getPlayerLevel(){return player.experienceLevel;}

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
    }

    public void Done() {

        ChunkPos[] readyToRemove =
                originalTerritories.stream().filter(t -> !territories.contains(t)).toArray(ChunkPos[]::new);
        ChunkPos[] readyToAdd =
                territories.stream().filter(t -> !originalTerritories.contains(t)).toArray(ChunkPos[]::new);

        TerritoryOperationMsg msg = new TerritoryOperationMsg(territoryInfo.territoryName, readyToAdd, readyToRemove,
                territoryInfo.permissions, territoryInfo.defaultPermission);

        PacketByteBuf buf = PacketByteBufs.create();
        TerritoryOperationMsg.encode(msg,buf);
        ClientPlayNetworking.send(TerritoryPacketHandler.CHANNEL,buf);
    }

    public void initChunkInfo() {
        selectableChunkPos.clear();


        for (ChunkPos pos : territories) {
            int chunkX = pos.x;
            int chunkZ = pos.z;

            selectableChunkPos.add(new ChunkPos(chunkX + 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ + 1));
            selectableChunkPos.add(new ChunkPos(chunkX - 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ - 1));
        }
        selectableChunkPos.removeIf(territories::contains);
        selectableChunkPos.removeIf(forbiddenChunkPos::contains);

        removableChunkPos.clear();

        removableChunkPos.addAll(territories);
        removableChunkPos.removeAll(TerritoryUtil.computeCutChunk(tileEntityChunkPos, territories));
        removableChunkPos.remove(tileEntityChunkPos); // Player cant remove the chunkPos where the tileEntity is located.
    }

    public int getTotalProtectPower() {
        return protectPower;
    }

    public int getUsedProtectPower() {
        return territories.size();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Environment(EnvType.CLIENT)
    public static class TerritoryMsgClientReceiver implements ClientPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            client.execute(()->{
                MinecraftClient.getInstance().player.closeScreen();
            });
        }
    }

    public static class TerritoryMsgServerReceiver implements ServerPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            TerritoryTableContainer container = (TerritoryTableContainer) player.currentScreenHandler;
            var msg = TerritoryOperationMsg.decode(buf);
            if (!container.updateTileEntityServerSide(player, msg)) return;

            World world = container.player.world;
            BlockState state = world.getBlockState(container.tileEntityPos);
            world.updateListeners(container.tileEntityPos, state, state, 2); //notify all clients to update.
            PacketByteBuf data = PacketByteBufs.create(); // todo 暂时这样表示成功，以后要改
            data.writeBoolean(true);
            responseSender.sendPacket(TerritoryPacketHandler.CHANNEL,data);
        }
    }

    public static class TerritoryOperationMsg {
        @Nonnull
        public ChunkPos[] readyAdd;
        @Nonnull
        public ChunkPos[] readyRemove;
        @Nonnull
        public Map<UUID, PermissionFlag> permissions;
        public PermissionFlag defaultPermission;
        public String territoryName;

        public TerritoryOperationMsg(String territoryName, @Nonnull ChunkPos[] readyAdd, @Nonnull ChunkPos[] readyRemove,
                                     @Nonnull Map<UUID, PermissionFlag> permissionFlagMap, PermissionFlag defaultPermission) {
            this.readyAdd = readyAdd;
            this.readyRemove = readyRemove;
            permissions = permissionFlagMap;
            this.defaultPermission = defaultPermission;
            this.territoryName = territoryName;
        }

        public static void encode(TerritoryOperationMsg msg, PacketByteBuf buffer) {
            buffer.writeInt(msg.readyAdd.length);
            buffer.writeInt(msg.readyRemove.length);
            buffer.writeInt(msg.permissions.size());
            for (ChunkPos pos : msg.readyAdd) {
                buffer.writeInt(pos.x);
                buffer.writeInt(pos.z);
            }
            for (ChunkPos pos : msg.readyRemove) {
                buffer.writeInt(pos.x);
                buffer.writeInt(pos.z);
            }
            msg.permissions.forEach((k, v) -> {
                buffer.writeUuid(k);
                buffer.writeInt(v.getCode());
            });
            buffer.writeInt(msg.defaultPermission.getCode());
            buffer.writeString(msg.territoryName);
        }

        public static TerritoryOperationMsg decode(PacketByteBuf buffer) {
            int addLength = buffer.readInt();
            int removeLength = buffer.readInt();
            int permissionLength = buffer.readInt();

            HashMap<UUID, PermissionFlag> permissions = new HashMap<>();
            ChunkPos[] readyAdd = new ChunkPos[addLength];
            ChunkPos[] readyRemove = new ChunkPos[removeLength];

            for (int i = 0; i < readyAdd.length; i++) {
                readyAdd[i] = new ChunkPos(buffer.readInt(), buffer.readInt());
            }
            for (int i = 0; i < readyRemove.length; i++) {
                readyRemove[i] = new ChunkPos(buffer.readInt(), buffer.readInt());
            }
            for (int i = 0; i < permissionLength; i++) {
                UUID uuid = buffer.readUuid();
                int code = buffer.readInt();
                PermissionFlag flag = new PermissionFlag(code);
                permissions.put(uuid, flag);
            }
            int defaultPermissionCode = buffer.readInt();
            String territoryName = buffer.readString(16);
            return new TerritoryOperationMsg(territoryName, readyAdd, readyRemove, permissions, new PermissionFlag(defaultPermissionCode));
        }
    }
}
