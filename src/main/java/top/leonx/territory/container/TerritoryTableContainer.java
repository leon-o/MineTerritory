package top.leonx.territory.container;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WTabPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import top.leonx.territory.client.gui.WMapWidget;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.data.TerritoryArea;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;
import top.leonx.territory.network.PacketContext;
import top.leonx.territory.network.TerritoryNetworkHandler;
import top.leonx.territory.network.packet.GUIBaseDataPushRequestPacket;
import top.leonx.territory.network.packet.GUIChunkDataSyncPacket;
import top.leonx.territory.network.packet.GUIChunkOperateRequestPacket;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.util.TerritoryUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class TerritoryTableContainer extends SyncedGuiDescription {

    public final BlockPos tileEntityPos;
    public final ChunkPos tileEntityChunkPos;
    public final TerritoryInfo territoryInfo;
    private final TerritoryTableTileEntity tileEntity;
    private final TerritoryArea area;

    public final Set<ChunkPos> occupiedChunks = new HashSet<>();
    public final Set<ChunkPos> selectableChunks = new HashSet<>();
    public final Set<ChunkPos> removableChunks = new HashSet<>();
    public final Set<ChunkPos> forbiddenChunks = new HashSet<>();

    private final PlayerEntity player;
    private final Set<ChunkPos> originalOccupied = new HashSet<>();
    public Identifier mapLocation;
    public ChunkPos mapLeftTopChunkPos;
    public int protectPower;

    public TerritoryTableContainer(int id, PlayerInventory inventory, PacketByteBuf buffer) {
        this(id, inventory, getTileEntity(inventory, buffer));
    }

    public TerritoryTableContainer(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModContainerTypes.TERRITORY_CONTAINER, syncId, inventory, getBlockInventory(context, 32), getBlockPropertyDelegate(context));
        this.player = inventory.player;
        Optional<BlockEntity> blockEntity = context.get(World::getBlockEntity);
        if(blockEntity.isEmpty()){
            throw new IllegalArgumentException("Can not get BlockEntity from context");
        }
        tileEntity = (TerritoryTableTileEntity) blockEntity.get();
        this.territoryInfo = tileEntity.getTerritoryInfo();
        this.area = tileEntity.getArea();
        tileEntityPos = tileEntity.getPos();

        // todo 可以控制的尺寸
        tileEntityChunkPos = new ChunkPos(tileEntityPos.getX() >> 4, tileEntityPos.getZ() >> 4);
        mapLeftTopChunkPos = new ChunkPos((tileEntityPos.getX() >> 4) - 4, (tileEntityPos.getZ() >> 4) - 4);
        occupiedChunks.addAll(tileEntity.territories);
        originalOccupied.addAll(tileEntity.territories);

        if (!Objects.requireNonNull(tileEntity.getWorld()).isClient) {
            computeChunkSelectionData();
        }
        protectPower = tileEntity.computeProtectPower();
        if(FabricLoader.getInstance().getEnvironmentType()== EnvType.CLIENT)
            mapLocation=tileEntity.mapLocation;


        WTabPanel tabPanel = new WTabPanel();
        setRootPanel(tabPanel);
        tabPanel.setSize(336, 292);
        //tabPanel.setInsets(Insets.ROOT_PANEL);

        tabPanel.add(buildPanelWithDoneBtn(buildMapTab()),tab->tab.title(new TranslatableText("gui.territory.tab_map")));
        tabPanel.add(buildPanelWithDoneBtn(buildPermGroupTab()),tab->tab.title(new TranslatableText("gui.territory.tab_perm_group")));
        tabPanel.add(buildPanelWithDoneBtn(buildPermTab()),tab->tab.title(new TranslatableText("gui.territory.tab_perm_map")));

        tabPanel.validate(this);
    }

    private WPanel buildPanelWithDoneBtn(WPanel child){
        WGridPanel root = new WGridPanel();
        root.setSize(360, 252);
        root.setInsets(Insets.ROOT_PANEL);
        root.add(child,0,0,20,13);

        WButton doneBtn = new WButton(new TranslatableText("gui.territory.done_btn"));
        doneBtn.setOnClick(this::done);
        root.add(new WButton(new TranslatableText("gui.territory.permission_btn")),11,8,6,1);
        root.add(doneBtn,11,10,15,1);

        return root;
    }

    private WPanel buildMapTab(){
        WGridPanel root = new WGridPanel();

        root.setInsets(Insets.ROOT_PANEL);
        WMapWidget map = new WMapWidget(tileEntity.mapLocation,180);
        root.add(map,0,1);
        map.SetChunkInfo(occupiedChunks,
                removableChunks,
                selectableChunks, forbiddenChunks, this.tileEntityChunkPos,tileEntity.mapSize);
        return root;
    }

    private WPanel buildPermGroupTab(){
        WGridPanel root = new WGridPanel();
        return root;
    }

    private WPanel buildPermTab(){
        WGridPanel root = new WGridPanel();
        return root;
    }

    private static ScreenHandlerContext getTileEntity(PlayerInventory inventory, PacketByteBuf buffer) {
        //final BlockEntity tileAtPos = inventory.player.world.getBlockEntity(buffer.readBlockPos());
        return ScreenHandlerContext.create(inventory.player.world,buffer.readBlockPos());//tileAtPos;
    }

    public int getXpRequired()
    {
        if (!player.isCreative()) {
            return  (int) Math.round((occupiedChunks.size()- originalOccupied.size())* TerritoryConfig.expNeededPerChunk);
        }
        return 0;
    }
    public int getPlayerLevel(){return player.experienceLevel;}

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
    }

    public void doneServerSide(){
        TerritoryInfoHolder.get(this.world).setAreaChunks(area,this.occupiedChunks);
    }

    @Environment(EnvType.CLIENT)
    public void done() {

        TerritoryNetworkHandler.clientGuiChunkOperateChannel.sendToServer(
                new GUIChunkOperateRequestPacket(ChunkPos.ORIGIN, GUIChunkOperateRequestPacket.Operation.Submit)
        );
    }

    public void computeChunkSelectionData() {
        for (int x = mapLeftTopChunkPos.x; x < mapLeftTopChunkPos.x + 9; x++) {
            for (int z = mapLeftTopChunkPos.z; z < mapLeftTopChunkPos.z + 9; z++) {
                var chunkPos = new ChunkPos(x,z);
                if(occupiedChunks.contains(chunkPos)) continue;
                // todo TerritoryInfoHolder.get(world)
                var infoHolder = TerritoryInfoHolder.get(world);
                if(infoHolder.isChunkOccupied(chunkPos)){
                    forbiddenChunks.add(chunkPos);
                }
            }
        }

        var parent = area.getParent();
        while (parent!=null){
            for (ChunkPos pos : parent.getChunks()) {
                forbiddenChunks.remove(pos);
            }

            for (TerritoryArea child : parent.getChildren()) {
                if(child==area) continue;
                forbiddenChunks.addAll(child.getChunks());
            }
            parent = parent.getParent();
        }
        TerritoryUtil.computeSelectableBtn(selectableChunks, occupiedChunks, forbiddenChunks);
        TerritoryUtil.computeRemovableBtn(removableChunks,tileEntityChunkPos, occupiedChunks);
    }


    public int getTotalProtectPower() {
        return protectPower;
    }

    public int getUsedProtectPower() {
        return occupiedChunks.size();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    private void onClientOperationRequest(GUIChunkOperateRequestPacket msg){
        if(msg.getOperation()== GUIChunkOperateRequestPacket.Operation.Submit){
            // todo Finish logic when client submit.
            this.doneServerSide();
            if (this.player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.closeHandledScreen();
            }
            return;
        }
        if(msg.getOperation()== GUIChunkOperateRequestPacket.Operation.Add){
            occupiedChunks.add(msg.getChunkPos());
        }else if(msg.getOperation() == GUIChunkOperateRequestPacket.Operation.Remove){
            occupiedChunks.remove(msg.getChunkPos());
        }
        computeChunkSelectionData();
        syncSelectionDataToClient();
    }

    private void syncSelectionDataToClient(){
        if(player instanceof ServerPlayerEntity serverPlayerEntity){
            TerritoryNetworkHandler.guiChunkDataSyncChannel.sendToClient(
                    serverPlayerEntity,new GUIChunkDataSyncPacket(selectableChunks,removableChunks,forbiddenChunks,occupiedChunks)
            );
        }
    }

    @Environment(EnvType.CLIENT)
    public static <T> void ClientHandleChunkDataSync(MinecraftClient client, ClientPlayNetworkHandler handler, GUIChunkDataSyncPacket msg, PacketContext<T> context) {
        assert client.player != null;
        if (client.player.currentScreenHandler instanceof TerritoryTableContainer tableContainer) {
            tableContainer.forbiddenChunks.clear();
            tableContainer.forbiddenChunks.addAll(msg.forbidden);
            tableContainer.occupiedChunks.clear();
            tableContainer.occupiedChunks.addAll(msg.occupied);
            tableContainer.selectableChunks.clear();
            tableContainer.selectableChunks.addAll(msg.selectable);
            tableContainer.removableChunks.clear();
            tableContainer.removableChunks.addAll(msg.removable);
        }
    }

    public static <T> void ServerHandleBaseDataPushRequest(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, GUIBaseDataPushRequestPacket msg, PacketContext<T> context) {

    }

    public static <T> void ServerHandleChunkOperateRequest(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, GUIChunkOperateRequestPacket msg, PacketContext<T> context) {
        if (player.currentScreenHandler instanceof TerritoryTableContainer tableContainer) {
            tableContainer.onClientOperationRequest(msg);
        }
    }
}
