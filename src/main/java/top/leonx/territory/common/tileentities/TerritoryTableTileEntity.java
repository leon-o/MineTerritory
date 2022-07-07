package top.leonx.territory.common.tileentities;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import top.leonx.territory.common.capability.ChunkCapabilityProvider;
import top.leonx.territory.common.container.TerritoryTableContainer;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.core.TerritoryInfoHolder;
import top.leonx.territory.init.config.TerritoryConfig;
import top.leonx.territory.init.registry.ModTiles;
import top.leonx.territory.util.DataUtil;
import top.leonx.territory.util.MessageUtil;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static top.leonx.territory.init.registry.ModCaps.TERRITORY_INFO_CAPABILITY;
import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryTableTileEntity extends BlockEntity implements BlockEntityTicker, MenuProvider {
    private static final String                      TERRITORY_POS_KEY         = "ter";
    private static final String                      MAP_COLOR                 = "map";
    private final        TerritoryInfo               territoryInfo             = new TerritoryInfo();
    private final        LazyOptional<TerritoryInfo> territoryInfoLazyOptional = LazyOptional.of(() -> territoryInfo);
    private final        HashSet<ChunkPos>           lastTerritories           = new HashSet<>();
    private final        List<ChunkPos>              territoriesLostDueToPower = new ArrayList<>();
    private final        int                         mapSize                   = 144;
    public ResourceLocation mapLocation               = null;
    public               RenderType                  mapRenderType             = null;
    public ItemStack mapStack;
    //For renderer
    public               float                       angle;
    public               float                       angleLastTick;
    public               boolean                     rise;
    public               float                       scale                     = 1 / 6f;
    public               float                       height                    = 0.8f;
    public               HashSet<ChunkPos>           territories               = new HashSet<>();
    private              DynamicTexture              mapTexture                = null;
    private              byte[]                      mapColor                  = new byte[0];

    public TerritoryTableTileEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModTiles.TERRITORY_TILE_ENTITY.get(), pWorldPosition, pBlockState);
    }

//    public TerritoryTableTileEntity() {
//        super(ModTiles.TERRITORY_TILE_ENTITY.get());
//        //territoryInfo.assignedTo(null, UUID.randomUUID(), null, "", TerritoryConfig.defaultPermission, new HashMap<>());
//    }

    public UUID getOwnerId() {
        return territoryInfo.ownerId;
    }

    public void initTerritoryInfo(UUID owner_id) {
        territoryInfo.assignedTo(owner_id, UUID.randomUUID(), worldPosition, UserUtil.getNameByUUID(owner_id) + "'s", TerritoryConfig.defaultPermission, new HashMap<>());
        updateTerritoryToWorld();
        setChanged();
    }

    public String getOwnerName() {
        return UserUtil.getNameByUUID(territoryInfo.ownerId);
    }

    public TerritoryInfo getTerritoryInfo() {
        return territoryInfo;
    }

    public void updateTerritoryToWorld() {
        if (level == null || level.isClientSide) return;
        lastTerritories.stream().filter(t -> !territories.contains(t)).forEach(t -> TerritoryInfoHolder.get(level).deassignToLevelChunk(t));
        territories.forEach(t -> TerritoryInfoHolder.get(level).assignToLevelChunk(t, territoryInfo));
        lastTerritories.clear();
        lastTerritories.addAll(territories);
        setChanged();
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        readInternal(pTag);
    }



    public void readInternal(@Nonnull CompoundTag compound) {
        ChunkCapabilityProvider.instance.ifPresent(territoryInfo1 -> territoryInfo1.deserializeNBT(compound));
        //TERRITORY_INFO_CAPABILITY.readNBT(territoryInfo, null, compound);

        territories.clear();
        ListTag list = compound.getList(TERRITORY_POS_KEY, 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag nbt = list.getCompound(i);
            ChunkPos    pos = ConvertNbtToPos(nbt);
            territories.add(pos);
        }
        mapColor = compound.getByteArray(MAP_COLOR);

        if (level != null && level.isClientSide && mapColor != null && mapColor.length == mapSize * mapSize) updateMapTexture();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag = writeInternal();
        super.saveAdditional(pTag);
    }


    private CompoundTag writeInternal() {


        CompoundTag nbt     = (CompoundTag) ChunkCapabilityProvider.instance.resolve().get().serializeNBT();
        ListTag listNBT = new ListTag();
        territories.forEach(t -> listNBT.add(ConvertPosToNbt(t)));
        nbt.put(TERRITORY_POS_KEY, listNBT);
        if (mapColor.length == 0) drawMapData();
        nbt.putByteArray(MAP_COLOR, mapColor);
        return nbt;
    }

    //Call when world loads.
    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }


    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }


    //Call when invoke world::notifyBlockChange

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, writeInternal());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        readInternal(Objects.requireNonNull(pkt.getTag()));
    }


    @Override
    public void onLoad() {
        super.onLoad();

        if (level.isClientSide) {
            mapTexture = new DynamicTexture(mapSize, mapSize, true);
            mapLocation = Minecraft.getInstance().getTextureManager()
                    .register("map_dynamic" + Mth.nextInt(new Random(), 0, mapSize),
                                                                                                mapTexture);
            mapRenderType = RenderType.text(mapLocation);
        }
        //drawMapData();
        territoryInfo.centerPos = worldPosition;
        territories.add(new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4));
        lastTerritories.add(new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap.equals(TERRITORY_INFO_CAPABILITY)) return territoryInfoLazyOptional.cast();
        return null;
    }


    @Override
    public void setRemoved() {
        super.setRemoved();
        territories.clear();
        updateTerritoryToWorld();
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState, BlockEntity pBlockEntity) {
        if (pLevel.isClientSide) {
            computeAngle();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void computeAngle() {
        Player player = Minecraft.getInstance().player;//this.world.getClosestPlayer((float)this.pos.getX() + 0.5F, (float)this.pos.getY() + 0.5F,

        if (player != null && this.worldPosition.closerThan(player.getEyePosition(), 4)) {
            rise = true;
            double dx          = player.xCloak - (this.worldPosition.getX() + 0.5);
            double dz          = player.zCloak - (this.worldPosition.getZ() + 0.5);
            float  angleRadian = (float) Mth.atan2(dz, dx);

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
            ChunkPos centerPos = new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4);
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
            level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
            level.getServer().getPlayerList().getPlayer(getOwnerId()).sendMessage(
                    new TranslatableComponent("message.territory" + ".insufficient_protect_power").setStyle(MessageUtil.RED),Util.NIL_UUID);
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
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
        level.getServer().getPlayerList().getPlayer(getOwnerId()).sendMessage(
                new TranslatableComponent("message.territory.territory_restore", Integer.toString(count)).setStyle(MessageUtil.GREEN), Util.NIL_UUID);
    }

    public double getBlockPower(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return DataUtil.getBlockStateProtectPower(state, level, pos);
    }

    public int computeProtectPower() {
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && level.isEmptyBlock(worldPosition.offset(l, 0, k)) && level.isEmptyBlock(worldPosition.offset(l, 1, k))) {
                    power += getBlockPower(level, worldPosition.offset(l * 2, 0, k * 2));
                    power += getBlockPower(level, worldPosition.offset(l * 2, 1, k * 2));

                    if (l != 0 && k != 0) {
                        power += getBlockPower(level, worldPosition.offset(l * 2, 0, k));
                        power += getBlockPower(level, worldPosition.offset(l * 2, 1, k));
                        power += getBlockPower(level, worldPosition.offset(l, 0, k * 2));
                        power += getBlockPower(level, worldPosition.offset(l, 1, k * 2));
                    }
                }
            }
        }

        return power + 1;
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return new TextComponent("Territory");
    }


    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player entity) {
        return new TerritoryTableContainer(id, inventory, this);
    }

    @OnlyIn(Dist.CLIENT)
    private void updateMapTexture() {
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                int index = mapColor[i + j * mapSize] & 255;
                if (index / 4 == 0) {
                    this.mapTexture.getPixels().setPixelRGBA(i, j, 0);
                } else {
                    this.mapTexture.getPixels().setPixelRGBA(i, j, MaterialColor.MATERIAL_COLORS[index / 4].getMapColor(index & 3));
                }
            }
        }
        this.mapTexture.upload();
    }

    @SuppressWarnings({"UnstableApiUsage"})
    public void drawMapData() {
        mapColor = new byte[mapSize * mapSize];
        ChunkPos chunkPos  = new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4);
        BlockPos centerPos = chunkPos.getWorldPosition().offset(8, 0, 8);
        int      i         = 1;
        int      xCenter   = centerPos.getX();
        int      yCenter   = centerPos.getZ();
        int      l         = Mth.floor(worldPosition.getX() - (double) xCenter) / i + mapSize / 2;
        int      i1        = Mth.floor(worldPosition.getZ() - (double) yCenter) / i + mapSize / 2;
        int      j1        = mapSize / i;
        if (level!=null && level.dimension() == Level.NETHER) { // level.dimension.isNether()
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
                    LevelChunk chunk    = level.getChunkAt(new BlockPos(k2, 0, l2));
                    if (!chunk.isEmpty()) {
                        ChunkPos chunkpos = chunk.getPos();
                        int      i3       = k2 & 15;
                        int      j3       = l2 & 15;
                        int      k3       = 0;
                        double   d1       = 0.0D;
                        if (level!=null && level.dimension() == Level.NETHER) { //world.dimension.isNether()
                            int l3 = k2 + l2 * 231871;
                            l3 = l3 * l3 * 0x1dfd851 + l3 * 11;
                            if ((l3 >> 20 & 1) == 0) {
                                multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                            } else {
                                multiset.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                            }

                            d1 = 100.0D;
                        } else {
                            BlockPos.MutableBlockPos blockpos$mutable1 = new BlockPos.MutableBlockPos();
                            BlockPos.MutableBlockPos blockpos$mutable  = new BlockPos.MutableBlockPos();

                            for (int i4 = 0; i4 < i; ++i4) {
                                for (int j4 = 0; j4 < i; ++j4) {
                                    int        k4 = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, i4 + i3, j4 + j3) + 1;
                                    BlockState blockstate;
                                    if (k4 <= 1) {
                                        blockstate = Blocks.BEDROCK.defaultBlockState();
                                    } else {
                                        do {
                                            --k4;
                                            blockpos$mutable1.set(chunkpos.getMinBlockX() + i4 + i3, k4, chunkpos.getMinBlockZ() + j4 + j3);
                                            blockstate = chunk.getBlockState(blockpos$mutable1);
                                        } while (blockstate.getMapColor(level, blockpos$mutable1) == MaterialColor.NONE && k4 > 0);

                                        if (k4 > 0 && !blockstate.getFluidState().isEmpty()) {
                                            int l4 = k4 - 1;
                                            blockpos$mutable.set(blockpos$mutable1);

                                            while (true) {
                                                blockpos$mutable.setY(l4--);
                                                BlockState blockstate1 = chunk.getBlockState(blockpos$mutable);
                                                ++k3;
                                                if (l4 <= 0 || blockstate1.getFluidState().isEmpty()) {
                                                    break;
                                                }
                                            }

                                            blockstate = this.getFluidState(level, blockstate, blockpos$mutable1);
                                        }
                                    }

                                    //data.removeStaleBanners(level, chunkpos.getXStart() + i4 + i3, chunkpos.getZStart() + j4 + j3);
                                    d1 += (double) k4 / (double) (i * i);
                                    multiset.add(blockstate.getMapColor(level, blockpos$mutable1));
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

                        MaterialColor materialcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.NONE);
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

                            byte b1 = (byte) (materialcolor.id * 4 + i5);

                            mapColor[k1 + l1 * mapSize] = b1;
                        }
                    }
                }
            }
        }
    }

    private BlockState getFluidState(Level worldIn, BlockState state, BlockPos pos) {
        FluidState ifluidstate = state.getFluidState();
        return !ifluidstate.isEmpty() && !state.isSolidRender(worldIn, pos) ?
                ifluidstate.createLegacyBlock() : state;
    }




}
