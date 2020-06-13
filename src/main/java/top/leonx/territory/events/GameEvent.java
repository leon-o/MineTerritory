package top.leonx.territory.events;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.omg.PortableServer.POA;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.blocks.ModBlocks;
import top.leonx.territory.items.ModItems;
import top.leonx.territory.tileentities.TerritoryTileEntity;

import java.awt.*;
import java.util.Random;

@Mod.EventBusSubscriber(modid = TerritoryMod.MODID)
public class GameEvent {
    static Random random=new Random();

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (!hasPermission(event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
            event.getPlayer().sendMessage(new StringTextComponent("Not your territory"));
        }
    }

    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (!hasPermission(event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
            event.getPlayer().sendMessage(new StringTextComponent("Not your territory"));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasPermission(BlockPos pos, PlayerEntity entity)
    {
        ChunkPos chunkPos = new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
        if(TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.containsKey(chunkPos))
        {
            TerritoryTileEntity tileEntity=TerritoryMod.TERRITORY_TILE_ENTITY_HASH_MAP.get(chunkPos);
            return tileEntity.getOwnerId().equals(entity.getUniqueID());
        }
        return true;
    }

    static int timeDelay=0;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event)
    {
        PlayerEntity entity= Minecraft.getInstance().player;
        if(entity==null)return;
        ItemStack heldItem = entity.getHeldItem(Hand.MAIN_HAND);
        if(heldItem.getItem()== ModItems.TerritoryBlockItem)
        {
            World world=Minecraft.getInstance().world;

            ChunkPos chunkPos=Minecraft.getInstance().world.getChunkAt(entity.getPosition()).getPos();
            timeDelay++;
            if(timeDelay==5)
            {
                timeDelay=0;
                for(float i=0;i<16;i+=0.2)
                {
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),
                            entity.posY,chunkPos.z<<4,0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),
                            entity.posY,(chunkPos.z<<4)+16,0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4),
                            entity.posY,(chunkPos.z<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),0,0.03,0);
                    Minecraft.getInstance().world.addParticle(ParticleTypes.DRAGON_BREATH,(chunkPos.x<<4)+16,
                            entity.posY,(chunkPos.z<<4)+i+ MathHelper.nextFloat(random,-0.1f,0.1f),0,0.03,0);
                }
            }
        }else{
            timeDelay=0;
        }
    }
}
