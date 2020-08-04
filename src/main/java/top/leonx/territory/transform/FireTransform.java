package top.leonx.territory.transform;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import top.leonx.territory.data.TerritoryInfo;
import top.leonx.territory.data.TerritoryInfoHolder;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class FireTransform implements ITransformer<MethodNode> {

    @SuppressWarnings("unused")
    public static boolean canBurn(BlockState state, IWorldReader worldIn, BlockPos pos)
    {
        if(worldIn instanceof World)
        {
            TerritoryInfo info = TerritoryInfoHolder.get((World) worldIn).getChunkTerritoryInfo(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
            return !info.IsProtected();
        }
        return true;
    }
    @Nonnull
    @Override
    public MethodNode transform(MethodNode input, @Nonnull ITransformerVotingContext context) {
        input.instructions.clear();
        input.localVariables.clear();

        Label label0 = new Label();
        input.visitLineNumber(84, label0);
        input.visitLabel(label0);
        input.visitVarInsn(ALOAD,1);
        input.visitVarInsn(ALOAD,2);
        input.visitVarInsn(ALOAD,3);
        input.visitMethodInsn(INVOKESTATIC,"top/leonx/territory/transform/FireTransform","canBurn","(Lnet/minecraft/block/BlockState;" +
                "Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)Z",false);
        Label label0_1=new Label();
        input.visitJumpInsn(IFNE,label0_1);
        input.visitInsn(ICONST_0);
        input.visitInsn(IRETURN);

        input.visitLabel(label0_1);
        input.visitVarInsn(ALOAD, 3);
        input.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "down", "()Lnet/minecraft/util/math/BlockPos;", false);
        input.visitVarInsn(ASTORE, 4);
        Label label1 = new Label();
        input.visitLabel(label1);
        input.visitLineNumber(85, label1);
        input.visitVarInsn(ALOAD, 2);
        input.visitVarInsn(ALOAD, 4);
        input.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/world/IWorldReader", "getBlockState", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", true);
        input.visitVarInsn(ALOAD, 2);
        input.visitVarInsn(ALOAD, 4);
        input.visitFieldInsn(GETSTATIC, "net/minecraft/util/Direction", "UP", "Lnet/minecraft/util/Direction;");
        input.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/block/BlockState", "func_224755_d", "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Z", false);
        Label label2 = new Label();
        input.visitJumpInsn(IFNE, label2);
        input.visitVarInsn(ALOAD, 0);
        input.visitVarInsn(ALOAD, 2);
        input.visitVarInsn(ALOAD, 3);
        input.visitMethodInsn(INVOKESPECIAL, "net/minecraft/block/FireBlock", "areNeighborsFlammable", "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z", false);
        Label label3 = new Label();
        input.visitJumpInsn(IFEQ, label3);
        input.visitLabel(label2);
        input.visitFrame(Opcodes.F_APPEND,1, new Object[] {"net/minecraft/util/math/BlockPos"}, 0, null);
        input.visitInsn(ICONST_1);
        Label label4 = new Label();
        input.visitJumpInsn(GOTO, label4);
        input.visitLabel(label3);
        input.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        input.visitInsn(ICONST_0);
        input.visitLabel(label4);
        input.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
        input.visitInsn(IRETURN);
        Label label5 = new Label();
        input.visitLabel(label5);
        input.visitLocalVariable("this", "Lnet/minecraft/block/FireBlock;", null, label0, label5, 0);
        input.visitLocalVariable("state", "Lnet/minecraft/block/BlockState;", null, label0, label5, 1);
        input.visitLocalVariable("worldIn", "Lnet/minecraft/world/IWorldReader;", null, label0, label5, 2);
        input.visitLocalVariable("pos", "Lnet/minecraft/util/math/BlockPos;", null, label0, label5, 3);
        input.visitLocalVariable("blockpos", "Lnet/minecraft/util/math/BlockPos;", null, label1, label5, 4);
        input.visitMaxs(4, 5);
        input.visitEnd();

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
        return Collections.singleton(Target.targetMethod("net/minecraft/block/FireBlock", "isValidPosition", "(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)Z"));
    }
}
