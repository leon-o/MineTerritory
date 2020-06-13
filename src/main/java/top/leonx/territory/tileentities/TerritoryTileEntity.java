package top.leonx.territory.tileentities;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import top.leonx.territory.container.TerritoryContainer;

import javax.annotation.Nullable;
import java.util.UUID;

import static top.leonx.territory.TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP;

public class TerritoryTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    public TerritoryTileEntity() {
        super(ModTileEntityType.TERRITORY_TILE_ENTITY);
    }
    private static final String OWNER_ID_KEY="owner_id";
    private static final String OWNER_NAME_KEY="owner_name";
    private static final String JURISDICTION_KEY="jurisdiction";
    private UUID owner_id;
    public float angle;
    public boolean rise;
    public String owner_name;
    public UUID getOwnerId(){return owner_id;}
    public ListNBT jurisdictions;

    public void setOwnerId(UUID owner_id)
    {
        this.owner_id=owner_id;
        owner_name= Minecraft.getInstance().world.getPlayerByUuid(owner_id).getName().getString();
        markDirty();
    }

    public void addJurisdiction(ChunkPos pos)
    {
        CompoundNBT nbt=new CompoundNBT();
        nbt.putInt("x",pos.x);
        nbt.putInt("z",pos.z);
        if(!jurisdictions.contains(nbt))
            jurisdictions.add(nbt);

        if(world.isRemote) return;

        if(!TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
        {
            TERRITORY_TILE_ENTITY_HASH_MAP.put(pos,this);
        }
    }

    public void removeJurisdiction(ChunkPos pos)
    {
        CompoundNBT nbt=new CompoundNBT();
        nbt.putInt("x",pos.x);
        nbt.putInt("z",pos.z);
        if(jurisdictions.contains(nbt))
        {
            jurisdictions.remove(nbt);
            TERRITORY_TILE_ENTITY_HASH_MAP.remove(pos);
        }
    }
    public void updateJurisdictionMap()
    {
        for (int i=0;i<jurisdictions.size();i++)
        {
            CompoundNBT nbt = jurisdictions.getCompound(i);
            ChunkPos pos=new ChunkPos(nbt.getInt("x"),nbt.getInt("z"));
            if(!TERRITORY_TILE_ENTITY_HASH_MAP.contains(pos))
            {
                TERRITORY_TILE_ENTITY_HASH_MAP.put(pos,this);
            }
        }
    }
    @Override
    public void read(CompoundNBT compound) {
        readInternal(compound);
        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound=writeInternal(compound);
        return super.write(compound);
    }

    private CompoundNBT writeInternal(CompoundNBT compound)
    {
        compound.put(JURISDICTION_KEY, jurisdictions);
        compound.putUniqueId(OWNER_ID_KEY,owner_id);
        compound.putString(OWNER_NAME_KEY,owner_name);
        return compound;
    }

    private void readInternal(CompoundNBT compound)
    {
        jurisdictions =compound.getList(JURISDICTION_KEY,10);
        owner_id=compound.getUniqueId(OWNER_ID_KEY);
        owner_name=compound.getString(OWNER_NAME_KEY);
        updateJurisdictionMap();
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }
    @Override
    public void handleUpdateTag(CompoundNBT data) {
        read(data);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if(jurisdictions==null)
        {
            jurisdictions=new ListNBT();
            addJurisdiction(new ChunkPos(this.pos.getX()>>4,this.pos.getZ()>>4));
        }
    }

    public ItemStack getItem(BlockState state){
        ItemStack itemStack=new ItemStack(state.getBlock().asItem());
        CompoundNBT tag = itemStack.getOrCreateChildTag("territory");
        tag.put(JURISDICTION_KEY,jurisdictions);

        return itemStack;
    }
    @Override
    public void remove() {
        super.remove();
        if(world.isRemote) return;
        ChunkPos pos=this.world.getChunkAt(this.pos).getPos();
        if(TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
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

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Territory") ;
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity) {
        return new TerritoryContainer(id,inventory,this);
    }
}
