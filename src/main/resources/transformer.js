var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var Label = Java.type('org.objectweb.asm.Label');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var LabelNode=Java.type('org.objectweb.asm.tree.LabelNode');
var JumpInsnNode=Java.type('org.objectweb.asm.tree.JumpInsnNode');
var InsnNode=Java.type('org.objectweb.asm.tree.InsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function transformSendChunkData(method)
{
    var insnList=new InsnList();
    var sendNode=new MethodInsnNode(Opcodes.INVOKESTATIC,"top/leonx/territory/transform/SendChunkDataTransform","onServerChunkLoad","(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/ServerPlayer;)V",false);
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
function transformIsValidPosition(method) {
    var iterator = method.instructions.iterator();
    var codeList=new InsnList();
    codeList.add(new VarInsnNode(Opcodes.ALOAD,1));
    codeList.add(new VarInsnNode(Opcodes.ALOAD,2));
    codeList.add(new VarInsnNode(Opcodes.ALOAD,3));
    codeList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"top/leonx/territory/transform/FireTransform","canBurn","(Lnet/minecraft/world/level/block/state/BlockState;" +
        "Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Z",false));
    var label0_1=new Label();
    var label01Node=new LabelNode(label0_1);
    codeList.add(new JumpInsnNode(Opcodes.IFNE,label01Node));
    codeList.add(new InsnNode(Opcodes.ICONST_0));
    codeList.add(new InsnNode(Opcodes.IRETURN));
    codeList.add(label01Node);

    while (iterator.hasNext())
    {
        var node = iterator.next();
        if(node.getOpcode()===Opcodes.ALOAD) //301 ALOAD 3
        {
            method.instructions.insertBefore(node,codeList);
            break;
        }
    }
    return method;
}
function initializeCoreMod() {
    return {
        'sendToClientSrg': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerPlayer',
                'methodName': 'm_184135_',//sendChunkLoad
                'methodDesc': '(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/network/protocol/Packet;)V'
            },
            'transformer': transformSendChunkData
        },
        'fireIsValidPositionSrg': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.FireBlock',
                'methodName': 'm_60710_',//canSurvive
                'methodDesc': '(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z'
            },
            'transformer': transformIsValidPosition
        }
    }
}
