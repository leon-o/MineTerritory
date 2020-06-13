package top.leonx.territory.container;

import com.sun.jndi.cosnaming.CNCtx;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.data.TerritoryOperationMsg;
import top.leonx.territory.tileentities.TerritoryTileEntity;

import java.util.*;
import java.util.function.Supplier;

public class TerritoryContainer extends Container {

    //The pos relative to mapLeftTopChunkPos
    public Set<ChunkPos> selectableChunkPos = new HashSet<>();
    public Set<ChunkPos> removableChunkPos =new HashSet<>();
    public TerritoryContainer(int id, PlayerInventory inventory, PacketBuffer buffer) {
        this(id,inventory,getTileEntity(inventory,buffer));
    }

    public TerritoryContainer(int id,PlayerInventory inventory,TerritoryTileEntity tileEntity)
    {
        super(ModContainerTypes.TERRITORY_CONTAINER, id);
        this.tileEntity=tileEntity;
        centerPos=tileEntity.getPos();

        initJurisdictionList();

        if(Objects.requireNonNull(tileEntity.getWorld()).isRemote)
        {
            initSelectableChunkPos();
            initRemovableChunkPos();
        }
        protectPower=computeProtectPower();
    }
    public int protectPower;
    private static TerritoryTileEntity getTileEntity(PlayerInventory inventory,PacketBuffer buffer)
    {
        final TileEntity tileAtPos = inventory.player.world.getTileEntity(buffer.readBlockPos());
        return (TerritoryTileEntity)tileAtPos;
    }
    static {
        TerritoryPacketHandler.registerMessage(TerritoryOperationMsg.class,TerritoryOperationMsg::encode,
                TerritoryOperationMsg::decode,
                TerritoryContainer::handler);
    }

    private static void handler(TerritoryOperationMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(()->{
            ServerPlayerEntity sender = contextSupplier.get().getSender();
            if(sender==null)//client side ,when success
            {
                TerritoryContainer container= (TerritoryContainer)Minecraft.getInstance().player.openContainer;

                container.jurisdictions.removeAll(container.chunkToBeRemoved);
                container.jurisdictions.addAll(container.chunkToBeAdded);
                container.chunkToBeAdded.clear();
                container.chunkToBeRemoved.clear();

                //container.initJurisdictionList();
                container.initRemovableChunkPos();
                container.initSelectableChunkPos();

            }else{
                TerritoryContainer container= (TerritoryContainer) sender.openContainer;
                if(container.updateTileEntityServerSide(sender,msg));
                    TerritoryPacketHandler.CHANNEL.sendTo(msg,sender.connection.netManager,
                        NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }




    private final TerritoryTileEntity tileEntity;
    public final BlockPos centerPos;
    public List<ChunkPos> jurisdictions=new ArrayList<>();
    public final Set<ChunkPos> chunkToBeAdded = new HashSet<>();
    public final Set<ChunkPos> chunkToBeRemoved =new HashSet<>();


    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
    public boolean updateTileEntityServerSide(ServerPlayerEntity playerEntity, TerritoryOperationMsg msg)
    {
        if(jurisdictions.size()+msg.readyAdd.length-msg.readyRemove.length>protectPower)
            return false;

        for(ChunkPos pos:msg.readyRemove)
        {
            tileEntity.removeJurisdiction(pos);
        }

        for (ChunkPos pos:msg.readyAdd) {

            if(!playerEntity.isCreative())
            {
                if(playerEntity.experienceLevel>=30)
                {
                    playerEntity.addExperienceLevel(-30);
                }else{
                    return false;
                }
            }
            playerEntity.world.playSound(playerEntity,playerEntity.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.BLOCKS,1F, 1F);

            tileEntity.addJurisdiction(pos);
        }
        tileEntity.markDirty();
        return true;
    }
    public void Done() {
        TerritoryOperationMsg msg = new TerritoryOperationMsg();
        msg.readyAdd= chunkToBeAdded.toArray(new ChunkPos[0]);
        msg.readyRemove= chunkToBeRemoved.toArray(new ChunkPos[0]);
        TerritoryPacketHandler.CHANNEL.sendToServer(msg);
    }
    private void initJurisdictionList() {
        jurisdictions.clear();
        for (INBT nbt : tileEntity.jurisdictions) {
            CompoundNBT compoundNBT = (CompoundNBT) nbt;
            jurisdictions.add(new ChunkPos(compoundNBT.getInt("x"),compoundNBT.getInt("z")));
        }
    }

    public void initSelectableChunkPos() {
        selectableChunkPos.clear();
        List< ChunkPos> tmp=new ArrayList<>(jurisdictions);

        tmp.addAll(chunkToBeAdded);
        tmp.removeAll(chunkToBeRemoved);

        for (ChunkPos pos : tmp) {
            int chunkX = pos.x;
            int chunkZ = pos.z;
            if (chunkToBeRemoved.contains(pos)) continue;
            selectableChunkPos.add(new ChunkPos(chunkX + 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ + 1));
            selectableChunkPos.add(new ChunkPos(chunkX - 1, chunkZ));
            selectableChunkPos.add(new ChunkPos(chunkX, chunkZ - 1));
        }
        for (ChunkPos jurisdiction : jurisdictions) {
            int chunkX = jurisdiction.x;
            int chunkZ = jurisdiction.z;

            selectableChunkPos.removeIf(t -> t.z == chunkZ && t.x == chunkX);
        }
    }
    public void initRemovableChunkPos()
    {
        removableChunkPos.clear();
        List<ChunkPos> tmp=new ArrayList<>(jurisdictions);
        tmp.addAll(chunkToBeRemoved);

        for (ChunkPos pos :tmp) {
            if(chunkToBeRemoved.contains(pos)) continue;
            int chunkX = pos.x;
            int chunkZ = pos.z;
            ChunkPos right = new ChunkPos(chunkX + 1, chunkZ);
            ChunkPos up =new ChunkPos(chunkX, chunkZ + 1);
            ChunkPos left =new ChunkPos(chunkX - 1, chunkZ);
            ChunkPos down =new ChunkPos(chunkX, chunkZ - 1);

            if(jurisdictions.contains(right) && jurisdictions.contains(up)&&jurisdictions.contains(left)&&jurisdictions.contains(down)
            || jurisdictions.contains(down) && jurisdictions.contains(up) || jurisdictions.contains(left) && jurisdictions.contains(right))
                continue;

            removableChunkPos.add(pos);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public int getBlockPower(IWorld world, BlockPos pos)
    {
        String banner_name= Objects.requireNonNull(world.getBlockState(pos).getBlock().getRegistryName()).getPath();
        return banner_name.contains("banner")?1:0;
    }

    private int computeProtectPower()
    {
        int power=0;
        BlockPos pos=tileEntity.getPos();
        IWorld world=tileEntity.getWorld();
        for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
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

        return power;
    }
    public int getTotalProtectPower()
    {
        return protectPower;
    }
    public int getUsedProtectPower()
    {
        return jurisdictions.size()+chunkToBeAdded.size()-chunkToBeRemoved.size();
    }
}
