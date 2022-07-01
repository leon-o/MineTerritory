package top.leonx.territory.tileentities;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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

public class TerritoryTableTileEntity extends BlockEntity {
    private static final String                      TERRITORY_POS_KEY         = "ter";
    private static final String                      MAP_COLOR                 = "map";
    private final        TerritoryInfo               territoryInfo             = new TerritoryInfo();
    //private final        LazyOptional<TerritoryInfo> territoryInfoLazyOptional = LazyOptional.of(() -> territoryInfo);
    private final        HashSet<ChunkPos>           lastTerritories           = new HashSet<>();
    private final        List<ChunkPos>              territoriesLostDueToPower = new ArrayList<>();
    private final        int                         mapSize                   = 144;
    public Identifier mapLocation               = null;
    public               RenderLayer                 mapRenderType             = null;
    public               ItemStack                   mapStack;
    //For renderer
    public               float                       angle;
    public               float                       angleLastTick;
    public               boolean                     rise;
    public               float                       scale                     = 1 / 6f;
    public               float                       height                    = 0.8f;
    public               HashSet<ChunkPos>           territories               = new HashSet<>();
    private NativeImageBackedTexture mapTexture                = null;
    private              byte[]                      mapColor                  = new byte[0];

    public TerritoryTableTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.TERRITORY_TILE_ENTITY,pos,state);
        //territoryInfo.assignedTo(null, UUID.randomUUID(), null, "", TerritoryConfig.defaultPermission, new HashMap<>());
    }

    public UUID getOwnerId() {
        return territoryInfo.ownerId;
    }

    public void initTerritoryInfo(UUID owner_id) {
        territoryInfo.assignedTo(owner_id, UUID.randomUUID(), pos, UserUtil.getNameByUUID(owner_id) + "'s", TerritoryConfig.defaultPermission, new HashMap<>());
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
        lastTerritories.stream().filter(t -> !territories.contains(t)).forEach(t -> TerritoryInfoHolder.get(world).deassignToChunk(t));
        territories.forEach(t -> TerritoryInfoHolder.get(world).assignToChunk(t, territoryInfo));
        lastTerritories.clear();
        lastTerritories.addAll(territories);
        markDirty();
    }

    @Override
    public void readNbt(@Nonnull NbtCompound compound) {
        super.readNbt(compound);
        readInternal(compound);
    }

    public void readInternal(@Nonnull NbtCompound compound) {
        //TERRITORY_INFO_CAPABILITY.readNBT(territoryInfo, null, compound);

        territories.clear();
        NbtList list = compound.getList(TERRITORY_POS_KEY, 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound nbt = list.getCompound(i);
            ChunkPos    pos = ConvertNbtToPos(nbt);
            territories.add(pos);
        }
        mapColor = compound.getByteArray(MAP_COLOR);

        if (world != null && !world.isClient && mapColor != null && mapColor.length == mapSize * mapSize) updateMapTexture();
    }

    @Nonnull
    @Override
    public void writeNbt(@Nonnull NbtCompound compound) {
        writeInternal(compound);
        super.writeNbt(compound);
    }

    private void writeInternal(NbtCompound compound) {
        //NbtCompound nbt     = (NbtCompound) TERRITORY_INFO_CAPABILITY.writeNBT(territoryInfo, null);
        NbtList listNBT = new NbtList();
        territories.forEach(t -> listNBT.add(ConvertPosToNbt(t)));
        compound.put(TERRITORY_POS_KEY, listNBT);
        if (mapColor.length == 0) drawMapData();
        compound.putByteArray(MAP_COLOR, mapColor);
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
            mapLocation = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("map_dynamic" + MathHelper.nextInt(new Random(), 0, mapSize),
                    mapTexture);
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

    public static void tick(World world, BlockPos pos, BlockState state, TerritoryTableTileEntity be) {
        if (world.isClient) {
            be.computeAngle();
        }
    }

    @Environment(EnvType.CLIENT)
    private void computeAngle() {
        PlayerEntity player = MinecraftClient.getInstance().player;//this.world.getClosestPlayer((float)this.pos.getX() + 0.5F, (float)this.pos.getY() + 0.5F,

        if (player != null && this.pos.isWithinDistance(player.getTrackedPosition(), 4)) {
            rise = true;
            double dx          = player.getX() - (this.pos.getX() + 0.5);
            double dz          = player.getZ() - (this.pos.getZ() + 0.5);
            float  angleRadian = (float) MathHelper.atan2(dz, dx);

            while (angleRadian >= (float) Math.PI) {
                angleRadian -= ((float) Math.PI * 2F);
            }

            while (angleRadian < -(float) Math.PI) {
                angleRadian += ((float) Math.PI * 2F);
            }
            angleLastTick = angle;
            angle = (float) Math.toDegrees(angleRadian);

            // todo rotated inertia
        } else {
            rise = false;
        }
    }

    public void notifyPowerProviderDestroy() {
        int usedPower    = territories.size();
        int protectPower = computeProtectPower();
        int fade         = usedPower - protectPower;
        if (fade > 0) {
            ChunkPos centerPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            List<ChunkPos> removable = territories.stream().sorted(
                    Comparator.comparingInt(a -> (a.x - centerPos.x) * (a.x - centerPos.x) + (a.z - centerPos.z) * (a.z - centerPos.z))).collect(Collectors.toList());//.filter

            removable.remove(centerPos);
            for (int i = 0; i < fade; i++) {
                ChunkPos pos         = removable.get(removable.size()-1);
                removable.remove(removable.size()-1);
                territories.remove(pos);
                territoriesLostDueToPower.add(pos);
            }
            updateTerritoryToWorld();
            world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
            world.getServer().getPlayerManager().getPlayer(getOwnerId()).sendMessage(
                    new TranslatableText("message.territory" + ".insufficient_protect_power").setStyle(MessageUtil.RED),
                    MessageType.GAME_INFO,Util.NIL_UUID);
        }
    }

    public void notifyPowerProviderPlace() {
        int usedPower    = territories.size();
        int protectPower = computeProtectPower();
        int count=0;
        while (territoriesLostDueToPower.size()>0 && usedPower < protectPower) {
            ChunkPos chunkPos = territoriesLostDueToPower.get(territoriesLostDueToPower.size() - 1);
            if (territories.add(chunkPos)) {
                count++;
            }
            usedPower = territories.size();
            territoriesLostDueToPower.remove(territoriesLostDueToPower.size() - 1);

        }
        if(count==0) return;
        updateTerritoryToWorld();
        world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
        world.getServer().getPlayerManager().getPlayer(getOwnerId()).sendMessage(
                new TranslatableText("message.territory.territory_restore", Integer.toString(count)).setStyle(MessageUtil.GREEN),
                MessageType.GAME_INFO, Util.NIL_UUID);
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
                int index = mapColor[i + j * mapSize] & 255;
                if (index / 4 == 0) {
                    this.mapTexture.getImage().setColor(i, j, 0);
                } else {
                    this.mapTexture.getImage().setColor(i, j, MapColor.getRenderColor(index & 3)); // todo there must be a problem
                }
            }
        }
        this.mapTexture.upload();
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public void drawMapData() {
        mapColor = new byte[mapSize * mapSize];
        ChunkPos chunkPos  = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        BlockPos centerPos = chunkPos.getStartPos().add(8, 0, 8);
        int      i         = 1;
        int      xCenter   = centerPos.getX();
        int      yCenter   = centerPos.getZ();
        int      l         = MathHelper.floor(pos.getX() - (double) xCenter) / i + mapSize / 2;
        int      i1        = MathHelper.floor(pos.getZ() - (double) yCenter) / i + mapSize / 2;
        int      j1        = mapSize / i;
        if (world!=null && world.getDimension().hasCeiling()) { // world.dimension.isNether()
            j1 /= 2;
        }

        for (int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
            //flag = false;
            double d0 = 0.0D;

            for (int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
                if (k1 >= 0 && l1 >= -1 && k1 < mapSize && l1 < mapSize) {
                    int                     i2       = k1 - l;
                    int                     j2       = l1 - i1;
                    boolean                 flag1    = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
                    int                     k2       = (xCenter / i + k1 - mapSize / 2) * i;
                    int                     l2       = (yCenter / i + l1 - mapSize / 2) * i;
                    Multiset<MapColor> multiset = LinkedHashMultiset.create();
                    WorldChunk chunk    = world.getWorldChunk(new BlockPos(k2, 0, l2));
                    if (!chunk.isEmpty()) {
                        ChunkPos chunkpos = chunk.getPos();
                        int      i3       = k2 & 15;
                        int      j3       = l2 & 15;
                        int      k3       = 0;
                        double   d1       = 0.0D;
                        if (world!=null && world.getDimension().hasCeiling()) { //world.dimension.isNether()
                            int l3 = k2 + l2 * 231871;
                            l3 = l3 * l3 * 0x1dfd851 + l3 * 11;
                            if ((l3 >> 20 & 1) == 0) {
                                multiset.add(Blocks.DIRT.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 10);
                            } else {
                                multiset.add(Blocks.STONE.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 100);
                            }

                            d1 = 100.0D;
                        } else {
                            BlockPos.Mutable mutable = new BlockPos.Mutable();
                            BlockPos.Mutable mutable2  = new BlockPos.Mutable();

                            for (int i4 = 0; i4 < i; ++i4) {
                                for (int j4 = 0; j4 < i; ++j4) {
                                    int        k4 = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, i4 + i3, j4 + j3) + 1;
                                    BlockState blockstate;
                                    if (k4 <= 1) {
                                        blockstate = Blocks.BEDROCK.getDefaultState();
                                    } else {
                                        do {
                                            --k4;
                                            mutable.set(chunkpos.getStartX() + i4 + i3, k4, chunkpos.getStartZ() + j4 + j3);
                                            blockstate = chunk.getBlockState(mutable);
                                        } while ((blockstate = chunk.getBlockState(mutable)).getMapColor(world, mutable) == MapColor.CLEAR && k4 > world.getBottomY());

                                        if (k4 > 0 && !blockstate.getFluidState().isEmpty()) {
                                            int l4 = k4 - 1;
                                            mutable2.set(mutable);

                                            while (true) {
                                                mutable2.setY(l4--);
                                                BlockState blockstate1 = chunk.getBlockState(mutable2);
                                                ++k3;
                                                if (l4 <= 0 || blockstate1.getFluidState().isEmpty()) {
                                                    break;
                                                }
                                            }

                                            blockstate = this.getFluidState(world, blockstate, mutable);
                                        }
                                    }

                                    //data.removeStaleBanners(world, chunkpos.getXStart() + i4 + i3, chunkpos.getZStart() + j4 + j3);
                                    d1 += (double) k4 / (double) (i * i);
                                    multiset.add(blockstate.getMapColor(world, mutable));
                                }
                            }
                        }

                        k3 = k3 / (i * i);
                        double d2 = (d1 - d0) * 4.0D / (double) (i + 4) + ((double) (k1 + l1 & 1) - 0.5D) * 0.4D;
                        int    i5 = 1;
                        if (d2 > 0.6D) {
                            i5 = 2;
                        }

                        if (d2 < -0.6D) {
                            i5 = 0;
                        }

                        MapColor materialcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.CLEAR);
                        if (materialcolor == MapColor.WATER_BLUE) {
                            d2 = (double) k3 * 0.1D + (double) (k1 + l1 & 1) * 0.2D;
                            i5 = 1;
                            if (d2 < 0.5D) {
                                i5 = 2;
                            }

                            if (d2 > 0.9D) {
                                i5 = 0;
                            }
                        }

                        d0 = d1;
                        if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {

                            byte b1 = (byte) (materialcolor.id * 4 + i5);

                            mapColor[k1 + l1 * mapSize] = b1;
                        }
                    }
                }
            }
        }
    }

    private BlockState getFluidState(World worldIn, BlockState state, BlockPos pos) {
        FluidState ifluidstate = state.getFluidState();
        return !ifluidstate.isEmpty() && !state.isSideSolidFullSquare(worldIn, pos, Direction.UP) ? ifluidstate.getBlockState() : state;
    }
}
