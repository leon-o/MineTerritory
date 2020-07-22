package top.leonx.territory.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.client.gui.PlayerList;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.items.ModItems;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = TerritoryMod.MODID)
public class GameEvent {
    static Random random=new Random();

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (!hasPermission(event.getPos(), event.getPlayer(),PermissionFlag.BREAK)) {
            event.setCanceled(true);

            if(!event.getWorld().isRemote)
                event.getPlayer().sendMessage(new StringTextComponent("Not your territory"));
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (!hasPermission(event.getPos(), event.getPlayer(),PermissionFlag.INTERACTE)) {
            event.setCanceled(true);

            if(!event.getWorld().isRemote)
                event.getPlayer().sendMessage(new StringTextComponent("Not your territory"));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasPermission(BlockPos pos, PlayerEntity entity, PermissionFlag flag)
    {
        ChunkPos chunkPos = new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
        if(TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(chunkPos))
        {
            TerritoryInfo data=TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.get(chunkPos);

            return data.getOwnerId()==null&&!entity.hasPermissionLevel(4) ||
                    data.getOwnerId()!=null && (data.getOwnerId().equals(entity.getUniqueID())|| data.permissions.containsKey(entity.getUniqueID()) && data.permissions.get(entity.getUniqueID()).contain(flag));
        }
        return true;
    }

    @Nullable
    private static TerritoryInfo getTerritoryData(ChunkPos pos)
    {
        if(TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(pos))
        {
            return TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.get(pos);
        }
        return null;
    }
    static int timeDelay=0;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase!= TickEvent.Phase.END) return;

        PlayerEntity clientPlayer= Minecraft.getInstance().player;
        if(clientPlayer==null) return;

        ChunkPos lastTickPos=new ChunkPos((int) clientPlayer.lastTickPosX>>4,(int) clientPlayer.lastTickPosZ>>4);
        ChunkPos thisTickPos=new ChunkPos((int) clientPlayer.posX>>4,(int) clientPlayer.posZ>>4);
        TerritoryInfo lastTerritoryInfo =getTerritoryData(lastTickPos);
        TerritoryInfo thisTerritoryInfo =getTerritoryData(thisTickPos);

        if((lastTerritoryInfo ==null && thisTerritoryInfo !=null) || (lastTerritoryInfo !=null && thisTerritoryInfo !=null && lastTerritoryInfo.getOwnerId()!= thisTerritoryInfo.getOwnerId()))
        {
            String ownerName;
            if(thisTerritoryInfo.getOwnerId()==null) // exit
            {
                ownerName= "'public area'";
            }else{
                ownerName= UsernameCache.getLastKnownUsername(thisTerritoryInfo.getOwnerId());
            }
            clientPlayer.sendMessage(new StringTextComponent(String.format("You have entered %s's territory",
                    ownerName)));
        }else if(thisTerritoryInfo ==null && lastTerritoryInfo !=null)
        {
            String ownerName;
            if(lastTerritoryInfo.getOwnerId()==null) // exit
            {
                ownerName= "'public area'";
            }else{
                ownerName= UsernameCache.getLastKnownUsername(lastTerritoryInfo.getOwnerId());
            }

            clientPlayer.sendMessage(new StringTextComponent(String.format("You have exited %s's territory", ownerName)));
        }

        ItemStack heldItem = clientPlayer.getHeldItem(Hand.MAIN_HAND);
        if(heldItem.getItem()== ModItems.TerritoryBlockItem)
        {
            // World world=Minecraft.getInstance().world;
            ChunkPos chunkPos=Minecraft.getInstance().world.getChunkAt(clientPlayer.getPosition()).getPos();
            timeDelay++;
            if(timeDelay==5)
            {
                timeDelay=0;
                for(float i=0;i<16;i+=0.2)
                {
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),
                            clientPlayer.posY,chunkPos.z<<4,0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),
                            clientPlayer.posY,(chunkPos.z<<4)+16,0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4),
                            clientPlayer.posY,(chunkPos.z<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+16,
                            clientPlayer.posY,(chunkPos.z<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),0,0.03,0);
                }
            }
        }else{
            timeDelay=0;
        }
    }
}
