package top.leonx.territory.tileentities;

import com.mojang.authlib.yggdrasil.response.User;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import top.leonx.territory.container.TerritoryTableContainer;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;
import top.leonx.territory.data.TerritoryInfoSynchronizer;
import top.leonx.territory.util.UserUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static top.leonx.territory.capability.ModCapabilities.TERRITORY_INFO_CAPABILITY;
import static top.leonx.territory.util.DataUtil.ConvertNbtToPos;
import static top.leonx.territory.util.DataUtil.ConvertPosToNbt;

public class TerritoryTableTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    //For renderer
    public float angle;
    public boolean rise;
    public float scale = 1 / 6f;
    public float height = 0.8f;
    public HashSet<ChunkPos> territories=new HashSet<>();
    private final TerritoryInfo territoryInfo=new TerritoryInfo();
    private final LazyOptional<TerritoryInfo> territoryInfoLazyOptional = LazyOptional.of(() -> territoryInfo);
    private final HashSet<ChunkPos> lastTerritories = new HashSet<>();
    private static final String TERRITORY_POS_KEY ="ter";


    public TerritoryTableTileEntity() {
        super(ModTileEntityType.TERRITORY_TILE_ENTITY);
        territoryInfo.assignedTo(null,UUID.randomUUID(),null,"",new PermissionFlag(),new HashMap<>());
    }

    public UUID getOwnerId() {
        return territoryInfo.ownerId;
    }

    public void initTerritoryInfo(UUID owner_id) {
        territoryInfo.assignedTo(owner_id,UUID.randomUUID(),pos, UserUtil.getNameByUUID(owner_id)+"'s",new PermissionFlag(1),new HashMap<>());
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
        if(world==null || world.isRemote) return;
        lastTerritories.stream().filter(t -> !territories.contains(t)).forEach(
                t->TerritoryInfoHolder.get(world).deassignToChunk(t)
        );
        territories.forEach(t -> TerritoryInfoHolder.get(world).assignToChunk(t,territoryInfo));
        lastTerritories.clear();
        lastTerritories.addAll(territories);
        markDirty();
    }

    @Override
    public void read(@Nonnull CompoundNBT compound) {
        super.read(compound);
        readInternal(compound);
    }

    public void readInternal(@Nonnull CompoundNBT compound) {
        TERRITORY_INFO_CAPABILITY.readNBT(territoryInfo, null, compound);

        territories.clear();
        ListNBT list = compound.getList(TERRITORY_POS_KEY, 10);
        for(int i=0;i<list.size();i++)
        {
            CompoundNBT nbt = list.getCompound(i);
            ChunkPos pos=ConvertNbtToPos(nbt);
            territories.add(pos);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = writeInternal();
        return super.write(compound);
    }

    private CompoundNBT writeInternal() {
        CompoundNBT nbt=(CompoundNBT) TERRITORY_INFO_CAPABILITY.writeNBT(territoryInfo, null);
        ListNBT listNBT=new ListNBT();
        territories.forEach(t-> listNBT.add(ConvertPosToNbt(t)));
        nbt.put(TERRITORY_POS_KEY,listNBT);
        return nbt;
    }

    //Call when world loads.
    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT data) {
        read(data);
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

        if(world.isRemote) return;

        territoryInfo.centerPos=pos;
        territories.add(new ChunkPos(pos.getX()>>4,pos.getZ()>>4));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap.equals(TERRITORY_INFO_CAPABILITY))
            return territoryInfoLazyOptional.cast();
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
        if (world.isRemote)
            computeAngle();
    }

    @OnlyIn(Dist.CLIENT)
    private void computeAngle() {
        PlayerEntity playerentity = Minecraft.getInstance().player;//this.world.getClosestPlayer((float)this.pos.getX() + 0.5F, (float)this.pos.getY() + 0.5F,

        if (playerentity != null && this.pos.withinDistance(playerentity.getPositionVec(), 4)) {
            rise = true;
            double dx = playerentity.getPosX() - this.pos.getX();
            double dz = playerentity.getPosZ() - this.pos.getZ();
            angle = (float) MathHelper.atan2(dz, dx);

            while (this.angle >= (float) Math.PI) {
                this.angle -= ((float) Math.PI * 2F);
            }

            while (this.angle < -(float) Math.PI) {
                this.angle += ((float) Math.PI * 2F);
            }

            angle = (float) Math.toDegrees(angle);
        } else {
            rise = false;
        }
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
}
