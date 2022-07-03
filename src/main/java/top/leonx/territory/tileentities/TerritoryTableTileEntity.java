package top.leonx.territory.tileentities;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.WorldChunk;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.container.TerritoryTableContainer;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;
import top.leonx.territory.util.DataUtil;
import top.leonx.territory.util.MessageUtil;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryTableTileEntity extends BlockEntity implements ExtendedScreenHandlerFactory, NamedScreenHandlerFactory {
    private static final String TERRITORY_POS_KEY = "ter";
    private static final String MAP_COLOR = "map";
    private final TerritoryInfo territoryInfo = new TerritoryInfo();
    //private final        LazyOptional<TerritoryInfo> territoryInfoLazyOptional = LazyOptional.of(() -> territoryInfo);
    private final HashSet<ChunkPos> lastTerritories = new HashSet<>();
    private final List<ChunkPos> territoriesLostDueToPower = new ArrayList<>();
    public final int mapSize = 256;
    public Identifier mapLocation = null;
    public RenderLayer mapRenderType = null;
    public ItemStack mapStack;
    //For renderer
    public float angle;
    public float angleLastTick;
    public boolean rise;
    public float scale = 1 / 6f;
    public float height = 0.8f;
    public HashSet<ChunkPos> territories = new HashSet<>();
    private NativeImageBackedTexture mapTexture = null;
    private byte[] mapColors = new byte[0];

    public TerritoryTableTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.TERRITORY_TILE_ENTITY, pos, state);
        //territoryInfo.assignedTo(null, UUID.randomUUID(), null, "", TerritoryConfig.defaultPermission, new HashMap<>());
    }

    public UUID getOwnerId() {
        return territoryInfo.ownerId;
    }

    public void initTerritoryInfo(UUID owner_id) {
        territoryInfo.assignedTo(owner_id, UUID.randomUUID(), pos, UserUtil.getNameByUUID(owner_id) + "'s",
                                 TerritoryConfig.defaultPermission, new HashMap<>());
        updateTerritoryToWorld();
        markDirty();
    }

    public String getOwnerName() {
        return UserUtil.getNameByUUID(territoryInfo.ownerId);
    }

    public TerritoryInfo getTerritoryInfo() {
        return territoryInfo;
    }

    public void updateTerritoryToWorld() {
        if (world == null || !world.isClient) return;
        lastTerritories.stream().filter(t -> !territories.contains(t)).forEach(
                t -> TerritoryInfoHolder.get(world).deassignToChunk(t));
        territories.forEach(t -> TerritoryInfoHolder.get(world).assignToChunk(t, territoryInfo));
        lastTerritories.clear();
        lastTerritories.addAll(territories);
        markDirty();
    }

    @Override
    public void readNbt(@Nonnull NbtCompound compound) {
        readInternal(compound);
        super.readNbt(compound);
    }

    public void readInternal(@Nonnull NbtCompound compound) {
        //TERRITORY_INFO_CAPABILITY.readNBT(territoryInfo, null, compound);

        territories.clear();
        NbtList list = compound.getList(TERRITORY_POS_KEY, 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound nbt = list.getCompound(i);
            ChunkPos pos = ConvertNbtToPos(nbt);
            territories.add(pos);
        }

        if (world != null && world.isClient && mapColors != null && mapColors.length == mapSize * mapSize)
            updateMapTexture();
    }

    @Nonnull
    @Override
    public void writeNbt(@Nonnull NbtCompound compound) {
        writeInternal(compound);
        super.writeNbt(compound);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("gui.territory_table");
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new TerritoryTableContainer(syncId, inv, ScreenHandlerContext.create(world, this.pos));
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(getPos());
    }

    private void writeInternal(NbtCompound compound) {
        //NbtCompound nbt     = (NbtCompound) TERRITORY_INFO_CAPABILITY.writeNBT(territoryInfo, null);
        NbtList listNBT = new NbtList();
        territories.forEach(t -> listNBT.add(ConvertPosToNbt(t)));
        compound.put(TERRITORY_POS_KEY, listNBT);
        if (mapColors.length == 0) drawMapData();
        compound.putByteArray(MAP_COLOR, mapColors);
    }

    //Call when world loads.
    @Nonnull
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /*@Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap.equals(TERRITORY_INFO_CAPABILITY)) return territoryInfoLazyOptional.cast();
        return null;
    }*/

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        if (world.isClient) {
            mapTexture = new NativeImageBackedTexture(mapSize, mapSize, true);
            mapLocation = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture(
                    String.format("territory_dynamic_map_texture_%d_%d_%d", this.pos.getX() << 4, this.pos.getZ() << 4,
                                  this.mapSize), mapTexture);
            mapRenderType = RenderLayer.getText(mapLocation);
        }
        //drawMapData();
        territoryInfo.centerPos = pos;
        territories.add(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
        lastTerritories.add(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        territories.clear();
        updateTerritoryToWorld();
    }

    public static void tick(World world, BlockPos pos, BlockState state, TerritoryTableTileEntity blockEntity) {
        if (world.isClient) {
            blockEntity.computeAngle();
        }
    }

    @Environment(EnvType.CLIENT)
    private void computeAngle() {
        PlayerEntity player = MinecraftClient.getInstance().player;//this.world.getClosestPlayer((float)this.pos.getX() + 0.5F, (float)this.pos.getY() + 0.5F,

        if (player != null && this.pos.isWithinDistance(player.getPos(), 4)) {
            rise = true;
            double dx = player.getX() - (this.pos.getX() + 0.5);
            double dz = player.getZ() - (this.pos.getZ() + 0.5);
            float angleRadian = (float) MathHelper.atan2(dz, dx);

            while (angleRadian >= (float) Math.PI) {
                angleRadian -= ((float) Math.PI * 2F);
            }

            while (angleRadian < -(float) Math.PI) {
                angleRadian += ((float) Math.PI * 2F);
            }
            angleLastTick = angle;
            angle = MathHelper.lerpAngleDegrees(0.5f, angle, (float) Math.toDegrees(
                    angleRadian));//(float) Math.toDegrees(angleRadian);
            // todo rotated inertia
        } else {
            rise = false;
        }
    }

    public void notifyPowerProviderDestroy() {
        int usedPower = territories.size();
        int protectPower = computeProtectPower();
        int fade = usedPower - protectPower;
        if (fade > 0) {
            ChunkPos centerPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            List<ChunkPos> removable = territories.stream().sorted(Comparator.comparingInt(
                    a -> (a.x - centerPos.x) * (a.x - centerPos.x) + (a.z - centerPos.z) * (a.z - centerPos.z))).collect(
                    Collectors.toList());//.filter

            removable.remove(centerPos);
            for (int i = 0; i < fade; i++) {
                ChunkPos pos = removable.get(removable.size() - 1);
                removable.remove(removable.size() - 1);
                territories.remove(pos);
                territoriesLostDueToPower.add(pos);
            }
            updateTerritoryToWorld();
            world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
            world.getServer().getPlayerManager().getPlayer(getOwnerId()).sendMessage(
                    new TranslatableText("message.territory" + ".insufficient_protect_power").setStyle(MessageUtil.RED),
                    MessageType.GAME_INFO, Util.NIL_UUID);
        }
    }

    public void notifyPowerProviderPlace() {
        int usedPower = territories.size();
        int protectPower = computeProtectPower();
        int count = 0;
        while (territoriesLostDueToPower.size() > 0 && usedPower < protectPower) {
            ChunkPos chunkPos = territoriesLostDueToPower.get(territoriesLostDueToPower.size() - 1);
            if (territories.add(chunkPos)) {
                count++;
            }
            usedPower = territories.size();
            territoriesLostDueToPower.remove(territoriesLostDueToPower.size() - 1);

        }
        if (count == 0) return;
        updateTerritoryToWorld();
        world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
        world.getServer().getPlayerManager().getPlayer(getOwnerId()).sendMessage(
                new TranslatableText("message.territory.territory_restore", Integer.toString(count)).setStyle(
                        MessageUtil.GREEN), MessageType.GAME_INFO, Util.NIL_UUID);
    }

    public double getBlockPower(WorldAccess world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return DataUtil.getBlockStateProtectPower(state, world, pos);
    }

    public int computeProtectPower() {
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && world.isAir(pos.add(l, 0, k)) && world.isAir(pos.add(l, 1, k))) {
                    power += getBlockPower(world, pos.add(l * 2, 0, k * 2));
                    power += getBlockPower(world, pos.add(l * 2, 1, k * 2));

                    if (l != 0 && k != 0) {
                        power += getBlockPower(world, pos.add(l * 2, 0, k));
                        power += getBlockPower(world, pos.add(l * 2, 1, k));
                        power += getBlockPower(world, pos.add(l, 0, k * 2));
                        power += getBlockPower(world, pos.add(l, 1, k * 2));
                    }
                }
            }
        }

        return power + 1;
    }

    /*@Nullable
    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity entity) {
        return new TerritoryTableContainer(id, inventory, this);
    }*/

    @Environment(EnvType.CLIENT)
    private void updateMapTexture() {
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                int index = mapColors[i + j * mapSize] & 255;
                this.mapTexture.getImage().setColor(i, j, MapColor.getRenderColor(index)); // todo there must be a problem
            }
        }
        this.mapTexture.upload();
    }

    /*private void initMapState(){
        ChunkPos centerChunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        BlockPos centerPos = centerChunkPos.getStartPos().add(8, 0, 8);
        state = MapState.of(centerPos.getX(), centerPos.getZ(), (byte) 1, false, false,
                            world.getRegistryKey());
    }*/

    @SuppressWarnings({"UnstableApiUsage"})
    public void drawMapData() {
        mapColors = new byte[mapSize * mapSize];
        /*if(state==null)
            initMapState();*/
        //int i = 1;
        ChunkPos centerChunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        BlockPos centerPos = centerChunkPos.getStartPos().add(8, 0, 8);
        int xCenter = centerPos.getX();
        int yCenter = centerPos.getZ();
        /*int l = MathHelper.floor(pos.getX() - (double) xCenter) / i + mapSize / 2;
        int i1 = MathHelper.floor(pos.getZ() - (double) yCenter) / i + mapSize / 2;
        int j1 = mapSize / i;*/

        int i = 1 << 1;//state.scale;
        int j = xCenter;
        int k = yCenter;
        int l = MathHelper.floor(pos.getX() - (double) j) / i+mapSize/2;
        int m = MathHelper.floor(pos.getZ() - (double) k) / i+mapSize/2;
        int n = mapSize / i;
        if (world.getDimension().hasCeiling()) {
            n /= 2;
        }

        //MapState.PlayerUpdateTracker playerUpdateTracker = state.getPlayerSyncData((PlayerEntity)entity);
        //++playerUpdateTracker.field_131;
        boolean bl = false;

        for (int o = l - n + 1; o < l + n; ++o) {

            double d = 0.0;

            for (int p = m - n - 1; p < m + n; ++p) {
                if (o >= 0 && p >= -1 && o < mapSize && p < mapSize) {
                    int q = o - l;
                    int r = p - m;
                    boolean bl2 = false;// q * q + r * r > (n - 2) * (n - 2);
                    int s = (j / i + o - mapSize/2) * i;
                    int t = (k / i + p - mapSize/2) * i;
                    Multiset<MapColor> multiset = LinkedHashMultiset.create();
                    WorldChunk worldChunk = world.getWorldChunk(new BlockPos(s, 0, t));
                    if (!worldChunk.isEmpty()) {
                        ChunkPos chunkPos = worldChunk.getPos();
                        int u = s & 15;
                        int v = t & 15;
                        int w = 0;
                        double e = 0.0;
                        if (world.getDimension().hasCeiling()) {
                            int x = s + t * 231871;
                            x = x * x * 31287121 + x * 11;
                            if ((x >> 20 & 1) == 0) {
                                multiset.add(Blocks.DIRT.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 10);
                            } else {
                                multiset.add(Blocks.STONE.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 100);
                            }

                            e = 100.0;
                        } else {
                            BlockPos.Mutable mutable = new BlockPos.Mutable();
                            BlockPos.Mutable mutable2 = new BlockPos.Mutable();

                            for (int y = 0; y < i; ++y) {
                                for (int z = 0; z < i; ++z) {
                                    int aa = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, y + u, z + v) + 1;
                                    BlockState blockState;
                                    if (aa <= world.getBottomY() + 1) {
                                        blockState = Blocks.BEDROCK.getDefaultState();
                                    } else {
                                        do {
                                            --aa;
                                            mutable.set(chunkPos.getStartX() + y + u, aa, chunkPos.getStartZ() + z + v);
                                            blockState = worldChunk.getBlockState(mutable);
                                        } while (blockState.getMapColor(world,
                                                                        mutable) == MapColor.CLEAR && aa > world.getBottomY());

                                        if (aa > world.getBottomY() && !blockState.getFluidState().isEmpty()) {
                                            int ab = aa - 1;
                                            mutable2.set(mutable);

                                            BlockState blockState2;
                                            do {
                                                mutable2.setY(ab--);
                                                blockState2 = worldChunk.getBlockState(mutable2);
                                                ++w;
                                            } while (ab > world.getBottomY() && !blockState2.getFluidState().isEmpty());

                                            blockState = this.getFluidStateIfVisible(world, blockState, mutable);
                                        }
                                    }

                                    /*state.removeBanner(world, chunkPos.getStartX() + y + u,
                                                       chunkPos.getStartZ() + z + v);*/
                                    e += (double) aa / (double) (i * i);
                                    multiset.add(blockState.getMapColor(world, mutable));
                                }
                            }
                        }

                        w /= i * i;
                        MapColor mapColor = (MapColor) Iterables.getFirst(Multisets.copyHighestCountFirst(multiset),
                                                                          MapColor.CLEAR);
                        MapColor.Brightness brightness;
                        double f;
                        if (mapColor == MapColor.WATER_BLUE) {
                            f = (double) w * 0.1 + (double) (o + p & 1) * 0.2;
                            if (f < 0.5) {
                                brightness = MapColor.Brightness.HIGH;
                            } else if (f > 0.9) {
                                brightness = MapColor.Brightness.LOW;
                            } else {
                                brightness = MapColor.Brightness.NORMAL;
                            }
                        } else {
                            f = (e - d) * 4.0 / (double) (i + 4) + ((double) (o + p & 1) - 0.5) * 0.4;
                            if (f > 0.6) {
                                brightness = MapColor.Brightness.HIGH;
                            } else if (f < -0.6) {
                                brightness = MapColor.Brightness.LOW;
                            } else {
                                brightness = MapColor.Brightness.NORMAL;
                            }
                        }

                        d = e;
                        if (p >= 0 /*&& q * q + r * r < n * n && (!bl2 || (o + p & 1) != 0)*/) {
                            //byte b1 = (byte) (mapColor.id * 4 + brightness.id);
                            mapColors[o+p*mapSize] = mapColor.getRenderColorByte(brightness);
                            //state.putColor(o, p, mapColor.getRenderColorByte(brightness));
                        }
                    }
                }
            }
        }
    }

    private BlockState getFluidStateIfVisible(World worldIn, BlockState state, BlockPos pos) {
        FluidState ifluidstate = state.getFluidState();
        return !ifluidstate.isEmpty() && !state.isSideSolidFullSquare(worldIn, pos,
                                                                      Direction.UP) ? ifluidstate.getBlockState() : state;
    }
}
