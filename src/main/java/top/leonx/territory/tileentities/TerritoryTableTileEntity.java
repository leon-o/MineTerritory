package top.leonx.territory.tileentities;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
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

import static top.leonx.territory.capability.ModCapabilities.TERRITORY_INFO_CAPABILITY;
import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryTableTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private static final String                      TERRITORY_POS_KEY         = "ter";
    private static final String                      MAP_COLOR                 = "map";
    private final        TerritoryInfo               territoryInfo             = new TerritoryInfo();
    private final        LazyOptional<TerritoryInfo> territoryInfoLazyOptional = LazyOptional.of(() -> territoryInfo);
    private final        HashSet<ChunkPos>           lastTerritories           = new HashSet<>();
    private final        List<ChunkPos>              territoriesLostDueToPower = new ArrayList<>();
    private final        int                         mapSize                   = 144;
    public               ResourceLocation            mapLocation               = null;
    public               RenderType                  mapRenderType             = null;
    public               ItemStack                   mapStack;
    //For renderer
    public               float                       angle;
    public               float                       angleLastTick;
    public               boolean                     rise;
    public               float                       scale                     = 1 / 6f;
    public               float                       height                    = 0.8f;
    public               HashSet<ChunkPos>           territories               = new HashSet<>();
    private              DynamicTexture              mapTexture                = null;
    private              byte[]                      mapColor                  = new byte[0];

    public TerritoryTableTileEntity() {
        super(ModTileEntityTypes.TERRITORY_TILE_ENTITY.get());
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
        if (world == null || world.isRemote) return;
        lastTerritories.stream().filter(t -> !territories.contains(t)).forEach(t -> TerritoryInfoHolder.get(world).deassignToChunk(t));
        territories.forEach(t -> TerritoryInfoHolder.get(world).assignToChunk(t, territoryInfo));
        lastTerritories.clear();
        lastTerritories.addAll(territories);
        markDirty();
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT compound) {
        super.read(state,compound);
        readInternal(compound);
    }

    public void readInternal(@Nonnull CompoundNBT compound) {
        TERRITORY_INFO_CAPABILITY.readNBT(territoryInfo, null, compound);

        territories.clear();
        ListNBT list = compound.getList(TERRITORY_POS_KEY, 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT nbt = list.getCompound(i);
            ChunkPos    pos = ConvertNbtToPos(nbt);
            territories.add(pos);
        }
        mapColor = compound.getByteArray(MAP_COLOR);

        if (world != null && world.isRemote && mapColor != null && mapColor.length == mapSize * mapSize) updateMapTexture();
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = writeInternal();
        return super.write(compound);
    }

    private CompoundNBT writeInternal() {
        CompoundNBT nbt     = (CompoundNBT) TERRITORY_INFO_CAPABILITY.writeNBT(territoryInfo, null);
        ListNBT     listNBT = new ListNBT();
        territories.forEach(t -> listNBT.add(ConvertPosToNbt(t)));
        nbt.put(TERRITORY_POS_KEY, listNBT);
        if (mapColor.length == 0) drawMapData();
        nbt.putByteArray(MAP_COLOR, mapColor);
        return nbt;
    }

    //Call when world loads.
    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        read(state,tag);
    }

    //Call when invoke world::notifyBlockChange
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 3, writeInternal());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readInternal(pkt.getNbtCompound());
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (world.isRemote) {
            mapTexture = new DynamicTexture(mapSize, mapSize, true);
            mapLocation = Minecraft.getInstance().getTextureManager().getDynamicTextureLocation("map_dynamic" + MathHelper.nextInt(new Random(), 0, mapSize),
                                                                                                mapTexture);
            mapRenderType = RenderType.getText(mapLocation);
        }
        //drawMapData();
        territoryInfo.centerPos = pos;
        territories.add(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
        lastTerritories.add(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap.equals(TERRITORY_INFO_CAPABILITY)) return territoryInfoLazyOptional.cast();
        return null;
    }

    @Override
    public void remove() {
        super.remove();
        territories.clear();
        updateTerritoryToWorld();
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            computeAngle();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void computeAngle() {
        PlayerEntity player = Minecraft.getInstance().player;//this.world.getClosestPlayer((float)this.pos.getX() + 0.5F, (float)this.pos.getY() + 0.5F,

        if (player != null && this.pos.withinDistance(player.getPositionVec(), 4)) {
            rise = true;
            double dx          = player.chasingPosX - (this.pos.getX() + 0.5);
            double dz          = player.chasingPosZ - (this.pos.getZ() + 0.5);
            float  angleRadian = (float) MathHelper.atan2(dz, dx);

            while (angleRadian >= (float) Math.PI) {
                angleRadian -= ((float) Math.PI * 2F);
            }

            while (angleRadian < -(float) Math.PI) {
                angleRadian += ((float) Math.PI * 2F);
            }
            angleLastTick = angle;
            angle = (float) Math.toDegrees(angleRadian);
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
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
            world.getServer().getPlayerList().getPlayerByUUID(getOwnerId()).sendMessage(
                    new TranslationTextComponent("message.territory" + ".insufficient_protect_power").setStyle(MessageUtil.RED),Util.DUMMY_UUID);
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
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
        world.getServer().getPlayerList().getPlayerByUUID(getOwnerId()).sendMessage(
                new TranslationTextComponent("message.territory.territory_restore", Integer.toString(count)).setStyle(MessageUtil.GREEN), Util.DUMMY_UUID);
    }

    public double getBlockPower(IWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return DataUtil.getBlockStateProtectPower(state, world, pos);
    }

    public int computeProtectPower() {
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && world.isAirBlock(pos.add(l, 0, k)) && world.isAirBlock(pos.add(l, 1, k))) {
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

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Territory");
    }

    @Nullable
    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity entity) {
        return new TerritoryTableContainer(id, inventory, this);
    }

    @OnlyIn(Dist.CLIENT)
    private void updateMapTexture() {
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                int index = mapColor[i + j * mapSize] & 255;
                if (index / 4 == 0) {
                    this.mapTexture.getTextureData().setPixelRGBA(i, j, 0);
                } else {
                    this.mapTexture.getTextureData().setPixelRGBA(i, j, MaterialColor.COLORS[index / 4].getMapColor(index & 3));
                }
            }
        }
        this.mapTexture.updateDynamicTexture();
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public void drawMapData() {
        mapColor = new byte[mapSize * mapSize];
        ChunkPos chunkPos  = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        BlockPos centerPos = chunkPos.asBlockPos().add(8, 0, 8);
        int      i         = 1;
        int      xCenter   = centerPos.getX();
        int      yCenter   = centerPos.getZ();
        int      l         = MathHelper.floor(pos.getX() - (double) xCenter) / i + mapSize / 2;
        int      i1        = MathHelper.floor(pos.getZ() - (double) yCenter) / i + mapSize / 2;
        int      j1        = mapSize / i;
        if (world!=null && world.getDimensionKey() == World.THE_NETHER) { // world.dimension.isNether()
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
                    Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
                    Chunk                   chunk    = world.getChunkAt(new BlockPos(k2, 0, l2));
                    if (!chunk.isEmpty()) {
                        ChunkPos chunkpos = chunk.getPos();
                        int      i3       = k2 & 15;
                        int      j3       = l2 & 15;
                        int      k3       = 0;
                        double   d1       = 0.0D;
                        if (world!=null && world.getDimensionKey() == World.THE_NETHER) { //world.dimension.isNether()
                            int l3 = k2 + l2 * 231871;
                            l3 = l3 * l3 * 0x1dfd851 + l3 * 11;
                            if ((l3 >> 20 & 1) == 0) {
                                multiset.add(Blocks.DIRT.getDefaultState().getMaterialColor(world, BlockPos.ZERO), 10);
                            } else {
                                multiset.add(Blocks.STONE.getDefaultState().getMaterialColor(world, BlockPos.ZERO), 100);
                            }

                            d1 = 100.0D;
                        } else {
                            BlockPos.Mutable blockpos$mutable1 = new BlockPos.Mutable();
                            BlockPos.Mutable blockpos$mutable  = new BlockPos.Mutable();

                            for (int i4 = 0; i4 < i; ++i4) {
                                for (int j4 = 0; j4 < i; ++j4) {
                                    int        k4 = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, i4 + i3, j4 + j3) + 1;
                                    BlockState blockstate;
                                    if (k4 <= 1) {
                                        blockstate = Blocks.BEDROCK.getDefaultState();
                                    } else {
                                        do {
                                            --k4;
                                            blockpos$mutable1.setPos(chunkpos.getXStart() + i4 + i3, k4, chunkpos.getZStart() + j4 + j3);
                                            blockstate = chunk.getBlockState(blockpos$mutable1);
                                        } while (blockstate.getMaterialColor(world, blockpos$mutable1) == MaterialColor.AIR && k4 > 0);

                                        if (k4 > 0 && !blockstate.getFluidState().isEmpty()) {
                                            int l4 = k4 - 1;
                                            blockpos$mutable.setPos(blockpos$mutable1);

                                            while (true) {
                                                blockpos$mutable.setY(l4--);
                                                BlockState blockstate1 = chunk.getBlockState(blockpos$mutable);
                                                ++k3;
                                                if (l4 <= 0 || blockstate1.getFluidState().isEmpty()) {
                                                    break;
                                                }
                                            }

                                            blockstate = this.getFluidState(world, blockstate, blockpos$mutable1);
                                        }
                                    }

                                    //data.removeStaleBanners(world, chunkpos.getXStart() + i4 + i3, chunkpos.getZStart() + j4 + j3);
                                    d1 += (double) k4 / (double) (i * i);
                                    multiset.add(blockstate.getMaterialColor(world, blockpos$mutable1));
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

                        MaterialColor materialcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.AIR);
                        if (materialcolor == MaterialColor.WATER) {
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

                            byte b1 = (byte) (materialcolor.colorIndex * 4 + i5);

                            mapColor[k1 + l1 * mapSize] = b1;
                        }
                    }
                }
            }
        }
    }

    private BlockState getFluidState(World worldIn, BlockState state, BlockPos pos) {
        FluidState ifluidstate = state.getFluidState();
        return !ifluidstate.isEmpty() && !state.isSolidSide(worldIn, pos, Direction.UP) ? ifluidstate.getBlockState() : state;
    }
}
