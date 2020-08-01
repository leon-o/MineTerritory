package top.leonx.territory.tileentities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import top.leonx.territory.container.TerritoryTableContainer;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static top.leonx.territory.capability.ModCapabilities.TERRITORY_INFO_CAPABILITY;

public class TerritoryTableTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    //For renderer
    public float angle;
    public boolean rise;
    public float scale = 1 / 6f;
    public float height = 0.8f;
    private final TerritoryInfo territoryInfo=new TerritoryInfo();
    private final LazyOptional<TerritoryInfo> territoryInfoLazyOptional = LazyOptional.of(() -> territoryInfo);
    private final HashSet<ChunkPos> lastTerritories = new HashSet<>();

    public TerritoryTableTileEntity() {
        super(ModTileEntityType.TERRITORY_TILE_ENTITY);
        territoryInfo.assignedTo(null,null,"",new PermissionFlag(),new HashSet<>(),new HashMap<>());
    }

    public UUID getOwnerId() {
        return territoryInfo.getOwnerId();
    }

    public void setOwnerId(UUID owner_id) {
        territoryInfo.assignedTo(owner_id);
        if(territoryInfo.territories!=null){
            territoryInfo.territories.forEach(t -> {
                Chunk chunk = world.getChunk(t.x, t.z);
                TerritoryInfo info = chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
                info.assignedTo(owner_id);
            });
        }
        markDirty();
    }

    public String getOwnerName() {
        return territoryInfo.getOwnerName();
    }

    public TerritoryInfo getTerritoryInfo() {
        return territoryInfo;
    }

//    public void addTerritory(ChunkPos pos) {
//        if (territoryInfo.territories.add(pos))
//            updateTerritoryToWorld();
//    }
//
//    public void removeTerritory(ChunkPos pos) {
//        if (territoryInfo.territories.remove(pos))
//            updateTerritoryToWorld();
//    }

    public void updateTerritoryToWorld() {
        if(world==null) return;
        lastTerritories.stream().filter(t -> !territoryInfo.territories.contains(t)).forEach(t -> {
            Chunk chunk = world.getChunk(t.x, t.z);
            TerritoryInfo info = chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
            info.deassign();
        });
        territoryInfo.territories.forEach(t -> {
            Chunk chunk = world.getChunk(t.x, t.z);
            TerritoryInfo info = chunk.getCapability(TERRITORY_INFO_CAPABILITY).orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance());
            info.assignedTo(territoryInfo.getOwnerId(), pos, territoryInfo.territoryName, territoryInfo.defaultPermission, territoryInfo.territories,
                    territoryInfo.permissions);
        });

        lastTerritories.clear();
        lastTerritories.addAll(territoryInfo.territories);
        markDirty();
    }

    @Override
    public void read(@Nonnull CompoundNBT compound) {
        super.read(compound);
        readInternal(compound);
    }

    public void readInternal(@Nonnull CompoundNBT compound) {
        TERRITORY_INFO_CAPABILITY.readNBT(territoryInfo, null, compound);

    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = writeInternal();
        return super.write(compound);
    }

    private CompoundNBT writeInternal() {
        return (CompoundNBT) TERRITORY_INFO_CAPABILITY.writeNBT(territoryInfo, null);
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
        updateTerritoryToWorld();
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
        updateTerritoryToWorld();// Client
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if(world.isRemote) return;

        territoryInfo.centerPos=pos;
        territoryInfo.territories.add(new ChunkPos(pos.getX()>>4,pos.getZ()>>4));
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
        territoryInfo.territories.clear();
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
            double dx = playerentity.posX - this.pos.getX();
            double dz = playerentity.posZ - this.pos.getZ();
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
