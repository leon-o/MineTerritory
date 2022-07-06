package top.leonx.territory.container;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
import top.leonx.territory.network.PacketContext;
import top.leonx.territory.network.TerritoryNetworkHandler;
import top.leonx.territory.client.gui.WMapWidget;
import top.leonx.territory.data.ComponentTypes;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.network.packet.GUIBaseDataPushRequestPacket;
import top.leonx.territory.network.packet.GUIChunkDataSyncPacket;
import top.leonx.territory.network.packet.GUIChunkOperateRequestPacket;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.util.MessageUtil;
import top.leonx.territory.util.TerritoryUtil;

import java.util.*;

public class TerritoryTableContainer extends SyncedGuiDescription {

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

    public boolean updateTileEntityServerSide(ServerPlayerEntity player, TerritoryOperationMsg msg) {
        if (originalTerritories.size() + msg.add.length - msg.remove.length > protectPower)
            return false;

        TerritoryTableTileEntity tileEntity = (TerritoryTableTileEntity) player.world.getBlockEntity(tileEntityPos);
        if (!player.isCreative()) {
            int experienceNeed = (int) Math.round((msg.add.length-msg.remove.length)* TerritoryConfig.expNeededPerChunk);
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
        for (ChunkPos pos : msg.remove) {
            tileEntity.territories.remove(pos);
        }

        Collections.addAll(tileEntity.territories, msg.add);

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
        ClientPlayNetworking.send(TerritoryNetworkHandler.GUI_BASE_DATA_PUSH_REQUEST,buf);
    }

    public void initChunkInfo() {
        TerritoryUtil.computeSelectableBtn(selectableChunkPos,territories,forbiddenChunkPos);
        TerritoryUtil.computeRemovableBtn(removableChunkPos,tileEntityChunkPos,territories);
    }

    public void onFetchChunkInfoFromServer(){

    }

    public void

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


    /*public static void onServerReceived(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, GUIChunkDataSyncPacket msg, PacketContext<GUIChunkDataSyncPacket> context) {
        if (player.currentScreenHandler instanceof TerritoryTableContainer container) {
            if (!container.updateTileEntityServerSide(player, msg)) return;

            World world = container.player.world;
            BlockState state = world.getBlockState(container.tileEntityPos);
            world.updateListeners(container.tileEntityPos, state, state, 2); //notify all clients to update.
            context.sendPacket(msg); // todo successful message
        }
    }

    public static void onClientReceived(MinecraftClient client, ClientPlayNetworkHandler handler, GUIChunkDataSyncPacket msg, PacketContext<GUIChunkDataSyncPacket> context) {
        if (client.player.currentScreenHandler instanceof TerritoryTableContainer screenHandler) {
            // todo
        }
    }*/

    @Environment(EnvType.CLIENT)
    public static <T> void ClientHandleChunkDataSync(MinecraftClient client, ClientPlayNetworkHandler handler, GUIChunkDataSyncPacket msg, PacketContext<T> context) {

    }

    public static <T> void ServerHandleBaseDataPushRequest(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, GUIBaseDataPushRequestPacket msg, PacketContext<T> context) {

    }

    public static <T> void ServerHandleChunkOperateRequest(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, GUIChunkOperateRequestPacket msg, PacketContext<T> context) {

    }
}
