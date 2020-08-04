var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var Label = Java.type('org.objectweb.asm.Label');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function initializeCoreMod() {
    return {
        'sendToClient': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.player.ServerPlayerEntity',
                'methodName': 'sendChunkLoad',
                'methodDesc': '(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/network/IPacket;Lnet/minecraft/network/IPacket;)V'
            },
            'transformer': function(method) {

                var insnList=new InsnList();
                var sendNode=new MethodInsnNode(Opcodes.INVOKESTATIC,"top/leonx/territory/transform/SendChunkDataTransform","onServerChunkLoad","(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/entity/player/ServerPlayerEntity;)V",false);
                insnList.add(new VarInsnNode(Opcodes.ALOAD,1));
                insnList.add(new VarInsnNode(Opcodes.ALOAD,0));
                insnList.add(sendNode);
                var iterator = method.instructions.iterator();
                while (iterator.hasNext())
                {
                    var insnNode=iterator.next();
                    if(insnNode.getOpcode()===Opcodes.RETURN)
                    {
                        method.instructions.insertBefore(insnNode,insnList);
                        break;
                    }
                }
                return method;
            }
        },
        'fireIsValidPosition': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.block.FireBlock',
                'methodName': 'isValidPosition',
                'methodDesc': '(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)Z'
            },
            'transformer': function(method) {
                method.instructions.clear();
                method.localVariables.clear();

                var label0 = new Label();
                method.visitLineNumber(84, label0);
                method.visitLabel(label0);
                method.visitVarInsn(Opcodes.ALOAD,1);
                method.visitVarInsn(Opcodes.ALOAD,2);
                method.visitVarInsn(Opcodes.ALOAD,3);
                method.visitMethodInsn(Opcodes.INVOKESTATIC,"top/leonx/territory/transform/FireTransform","canBurn","(Lnet/minecraft/block/BlockState;" +
                    "Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)Z",false);
                var label0_1=new Label();
                method.visitJumpInsn(Opcodes.IFNE,label0_1);
                method.visitInsn(Opcodes.ICONST_0);
                method.visitInsn(Opcodes.IRETURN);

                method.visitLabel(label0_1);
                method.visitVarInsn(Opcodes.ALOAD, 3);
                method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "down", "()Lnet/minecraft/util/math/BlockPos;", false);
                method.visitVarInsn(Opcodes.ASTORE, 4);
                var label1 = new Label();
                method.visitLabel(label1);
                method.visitLineNumber(85, label1);
                method.visitVarInsn(Opcodes.ALOAD, 2);
                method.visitVarInsn(Opcodes.ALOAD, 4);
                method.visitMethodInsn(Opcodes.INVOKEINTERFACE, "net/minecraft/world/IWorldReader", "getBlockState", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", true);
                method.visitVarInsn(Opcodes.ALOAD, 2);
                method.visitVarInsn(Opcodes.ALOAD, 4);
                method.visitFieldInsn(Opcodes.GETSTATIC, "net/minecraft/util/Direction", "UP", "Lnet/minecraft/util/Direction;");
                method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/block/BlockState", "func_224755_d", "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Z", false);
                var label2 = new Label();
                method.visitJumpInsn(Opcodes.IFNE, label2);
                method.visitVarInsn(Opcodes.ALOAD, 0);
                method.visitVarInsn(Opcodes.ALOAD, 2);
                method.visitVarInsn(Opcodes.ALOAD, 3);
                method.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/block/FireBlock", "areNeighborsFlammable", "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z", false);
                var label3 = new Label();
                method.visitJumpInsn(Opcodes.IFEQ, label3);
                method.visitLabel(label2);
                method.visitFrame(Opcodes.F_APPEND,1, ["net/minecraft/util/math/BlockPos"], 0, null);
                method.visitInsn(Opcodes.ICONST_1);
                var label4 = new Label();
                method.visitJumpInsn(Opcodes.GOTO, label4);
                method.visitLabel(label3);
                method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                method.visitInsn(Opcodes.ICONST_0);
                method.visitLabel(label4);
                method.visitFrame(Opcodes.F_SAME1, 0, null, 1, [Opcodes.INTEGER]);
                method.visitInsn(Opcodes.IRETURN);
                var label5 = new Label();
                method.visitLabel(label5);
                method.visitLocalVariable("this", "Lnet/minecraft/block/FireBlock;", null, label0, label5, 0);
                method.visitLocalVariable("state", "Lnet/minecraft/block/BlockState;", null, label0, label5, 1);
                method.visitLocalVariable("worldIn", "Lnet/minecraft/world/IWorldReader;", null, label0, label5, 2);
                method.visitLocalVariable("pos", "Lnet/minecraft/util/math/BlockPos;", null, label0, label5, 3);
                method.visitLocalVariable("blockpos", "Lnet/minecraft/util/math/BlockPos;", null, label1, label5, 4);
                method.visitMaxs(4, 5);
                method.visitEnd();
                return method;
            }
        }
    }
}