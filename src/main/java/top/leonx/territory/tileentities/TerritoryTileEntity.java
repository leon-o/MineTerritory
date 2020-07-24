package top.leonx.territory.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
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
import top.leonx.territory.container.TerritoryContainer;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static top.leonx.territory.TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP;
import static top.leonx.territory.util.DataUtil.*;

public class TerritoryTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    public TerritoryTileEntity() {
        super(ModTileEntityType.TERRITORY_TILE_ENTITY);
    }
    private static final String OWNER_ID_KEY="owner_id";
    //private static final String OWNER_NAME_KEY="owner_name";
    private static final String TERRITORY_POS_KEY ="territories";
    private static final String PERMISSION_KEY="permission";
    private static final String DEFAULT_PERMISSION_KEY="def_permission";
    public float angle;
    public boolean rise;

    public UUID getOwnerId(){return territoryInfo.getOwnerId();}
    public String getOwnerName(){return territoryInfo.getOwnerName();}
    private TerritoryInfo territoryInfo;
    public TerritoryInfo getTerritoryInfo()
    {
        return territoryInfo;
    }
    public void setOwnerId(UUID owner_id)
    {
        territoryInfo.setOwnerId(owner_id);
        markDirty();
    }

    public void addTerritory(ChunkPos pos)
    {
        territoryInfo.territories.add(pos);

        if(world.isRemote) return;

        if(!TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
        {
            TERRITORY_TILE_ENTITY_HASH_MAP.put(pos, territoryInfo);
        }
    }

    public void removeJurisdiction(ChunkPos pos)
    {

        if(territoryInfo.territories.contains(pos))
        {
            territoryInfo.territories.remove(pos);
            TERRITORY_TILE_ENTITY_HASH_MAP.remove(pos);
        }
    }
    public void updateTerritoryToHashMap()
    {
        territoryInfo.territories.forEach(t->{
            if(!TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(t))
            {
                TERRITORY_TILE_ENTITY_HASH_MAP.put(t, territoryInfo);
            }
        });
    }
    public void setPermissionAll(Map<UUID,PermissionFlag> permissionAll)
    {
        territoryInfo.permissions.clear();
        territoryInfo.permissions.putAll(permissionAll);
    }
    @SuppressWarnings("unused")
    public void setPermission(UUID player, PermissionFlag flag)
    {
        // Ensure that the TerritoryData in TERRITORY_TILE_ENTITY_HASH_MAP and the TileEntity point to the same object
        // The TerritoryData in TERRITORY_TILE_ENTITY_HASH_MAP and here are synchronized
        if (territoryInfo.permissions.containsKey(player))
        {
            territoryInfo.permissions.replace(player,flag);
        }else {
            territoryInfo.permissions.put(player,flag);
        }
    }

    @SuppressWarnings("unused")
    public void removePermission(UUID player)
    {
        territoryInfo.permissions.remove(player);
    }

    @Override
    public void read(@Nonnull CompoundNBT compound) {
        readInternal(compound);
        super.read(compound);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound=writeInternal(compound);
        return super.write(compound);
    }

    private CompoundNBT writeInternal(CompoundNBT compound)
    {
        ListNBT territoryListNBT=new ListNBT();
        territoryInfo.territories.forEach(t->{
            CompoundNBT nbt=ConvertPosToNbt(t);
            territoryListNBT.add(nbt);
        });
        compound.put(TERRITORY_POS_KEY, territoryListNBT);

        ListNBT permissionListNBT=new ListNBT();
        territoryInfo.permissions.forEach((k, v)-> permissionListNBT.add(ConvertUUIDPermissionToNbt(k,v)));
        compound.put(PERMISSION_KEY,permissionListNBT);
        compound.putUniqueId(OWNER_ID_KEY, territoryInfo.getOwnerId());
        compound.putInt(DEFAULT_PERMISSION_KEY,territoryInfo.defaultPermission.getCode());
        return compound;
    }

    private void readInternal(CompoundNBT compound)
    {
        UUID ownerId = compound.getUniqueId(OWNER_ID_KEY);
        ListNBT territoryList = compound.getList(TERRITORY_POS_KEY, 10);
        ListNBT permissionList = compound.getList(PERMISSION_KEY, 10);

        HashMap<UUID,PermissionFlag> permissionFlagHashMap=new HashMap<>();
        permissionList.forEach(t->{
            Map.Entry<UUID, PermissionFlag> entry = ConvertNbtToUUIDPermission((CompoundNBT) t);
            permissionFlagHashMap.put(entry.getKey(),entry.getValue());
        });
        PermissionFlag defPermissionFlag=new PermissionFlag(compound.getInt(DEFAULT_PERMISSION_KEY));
        HashSet<ChunkPos> territoriesTmp=new HashSet<>();
        territoryList.forEach(t-> territoriesTmp.add(ConvertNbtToPos((CompoundNBT) t)));
        if(territoryInfo==null){
            territoryInfo=new TerritoryInfo(ownerId,territoriesTmp,permissionFlagHashMap,defPermissionFlag);
        }else{
            territoryInfo.setOwnerId(ownerId);
            territoryInfo.territories.clear();
            territoryInfo.territories.addAll(territoriesTmp);
            setPermissionAll(permissionFlagHashMap);
            territoryInfo.defaultPermission=defPermissionFlag;
        }

        updateTerritoryToHashMap();
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
        return new SUpdateTileEntityPacket(this.pos,3,writeInternal(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readInternal(pkt.getNbtCompound());
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if(territoryInfo==null)
        {
            territoryInfo=new TerritoryInfo(null,new HashSet<>());
            addTerritory(new ChunkPos(this.pos.getX()>>4,this.pos.getZ()>>4));
        }
    }

    public ItemStack getItem(BlockState state){
        ItemStack itemStack=new ItemStack(state.getBlock().asItem());
        CompoundNBT tag = itemStack.getOrCreateChildTag("territory");
        ListNBT territoryList=new ListNBT();
        territoryInfo.territories.forEach(t-> territoryList.add(ConvertPosToNbt(t)));
        tag.put(TERRITORY_POS_KEY, territoryList);

        return itemStack;
    }
    @Override
    public void remove() {
        super.remove();

        ChunkPos pos=this.world.getChunkAt(this.pos).getPos();
        TERRITORY_TILE_ENTITY_HASH_MAP.remove(pos);
    }

    @Override
    public void tick() {
        if(!this.world.isRemote) return;
        PlayerEntity playerentity = this.world.getClosestPlayer((float)this.pos.getX() + 0.5F, (float)this.pos.getY() + 0.5F, (float)this.pos.getZ() + 0.5F, 3.0D, false);
        if (playerentity != null) {
            rise=true;
            double dx=playerentity.posX-this.pos.getX();
            double dz=playerentity.posZ-this.pos.getZ();
            angle= (float) MathHelper.atan2(dz,dx);

            while(this.angle >= (float)Math.PI) {
                this.angle -= ((float)Math.PI * 2F);
            }

            while(this.angle < -(float)Math.PI) {
                this.angle += ((float)Math.PI * 2F);
            }

            angle= (float) Math.toDegrees(angle);
        }else {
            rise=false;
        }
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Territory") ;
    }

    @Nullable
    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity entity) {
        return new TerritoryContainer(id,inventory,this);
    }
}
