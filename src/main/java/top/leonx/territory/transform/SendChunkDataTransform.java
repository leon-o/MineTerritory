package top.leonx.territory.transform;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import top.leonx.territory.TerritoryPacketHandler;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;
import top.leonx.territory.data.TerritoryInfoSynchronizer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;

// ChunkDataEvent.Load never fired.
public class SendChunkDataTransform implements ITransformer<MethodNode> {
    @Nonnull
    @Override
    public MethodNode transform(MethodNode input, @Nonnull ITransformerVotingContext context) {

        InsnList insnList=new InsnList();
        MethodInsnNode sendNode=new MethodInsnNode(Opcodes.INVOKESTATIC,"top/leonx/territory/events/ChunkEvent","onServerChunkLoad","(Lnet/minecraft" +
                "/util/math/ChunkPos;Lnet/minecraft/entity/player/ServerPlayerEntity;)V",false);
        insnList.add(new VarInsnNode(Opcodes.ALOAD,1));
        insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
        insnList.add(sendNode);
        ListIterator<AbstractInsnNode> iterator = input.instructions.iterator();
        while (iterator.hasNext())
        {
            AbstractInsnNode insnNode=iterator.next();
            if(insnNode.getOpcode()==Opcodes.RETURN)
            {
                input.instructions.insertBefore(insnNode,insnList);
                break;
            }
        }


        return input;
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(@Nonnull ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
        return Collections.singleton(Target.targetMethod("net/minecraft/entity/player/ServerPlayerEntity","sendChunkLoad","(Lnet/minecraft/util/math" +
                "/ChunkPos;Lnet/minecraft/network/IPacket;Lnet/minecraft/network/IPacket;)V"));
    }
}