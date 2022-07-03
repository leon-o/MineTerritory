package top.leonx.territory.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import top.leonx.territory.config.TerritoryConfig;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;

public class TerritoryTableTileEntityRenderer implements BlockEntityRenderer<TerritoryTableTileEntity> {

    private static final RenderLayer MAP_BACKGROUND = RenderLayer.getText(new Identifier("textures/map/map_background.png"));
    private static final RenderLayer COMPASS_ADDITION = RenderLayer.getText(new Identifier("territory","textures/block/compass.png"));


    @Override
    public void render(TerritoryTableTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider vertexConsumers, int combinedLightIn, int overlay) {
        if (tileEntityIn.rise) {
            tileEntityIn.height += 0.06 * partialTicks;
            tileEntityIn.scale += 0.04f * partialTicks;
        } else {
            tileEntityIn.height -= 0.06 * partialTicks;
            tileEntityIn.scale -= 0.04f * partialTicks;
        }
        tileEntityIn.height = MathHelper.clamp(tileEntityIn.height, 0, 1f);
        tileEntityIn.scale = MathHelper.clamp(tileEntityIn.scale, 0, 1f);
        @SuppressWarnings("SuspiciousNameCombination")
        float height = EasingLerp(0.8f, 1.1f, tileEntityIn.height);
        float scale = EasingLerp(1 / 4f, 1 / 2f, tileEntityIn.scale);
        matrixStackIn.push();
        matrixStackIn.translate(0.5, 0, 0.5);

        var compassBuffer = vertexConsumers.getBuffer(COMPASS_ADDITION);
        var       matrix        = matrixStackIn.peek().getPositionMatrix();
        compassBuffer.vertex(matrix, -0.5F, 0.752F,0.5F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(combinedLightIn).next();
        compassBuffer.vertex(matrix, 0.5F,0.752F, 0.5F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(combinedLightIn).next();
        compassBuffer.vertex(matrix, 0.5F, 0.752F,-0.5F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(combinedLightIn).next();
        compassBuffer.vertex(matrix, -0.5F, 0.752F,-0.5F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(combinedLightIn).next();

        matrixStackIn.translate(0, height, 0);
        float angleLerp=MathHelper.lerpAngleDegrees(partialTicks,tileEntityIn.angleLastTick,tileEntityIn.angle);
        matrixStackIn.multiply(Quaternion.fromEulerXyz(0, (float) Math.toRadians(90 - angleLerp), 0));

        matrixStackIn.push();
        //RenderSystem._enableRescaleNormal();
        matrixStackIn.scale(scale,scale,scale);
        matrixStackIn.multiply(new Quaternion(new Vec3f(1, 0, 0), 30*(3*scale-0.5f), true));

        var mapBackBuffer = vertexConsumers.getBuffer(MAP_BACKGROUND);
        matrix        = matrixStackIn.peek().getPositionMatrix();
        mapBackBuffer.vertex(matrix, -0.6F, 0.0F,0.6F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(combinedLightIn).next();
        mapBackBuffer.vertex(matrix, 0.6F,0.0F, 0.6F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(combinedLightIn).next();
        mapBackBuffer.vertex(matrix, 0.6F, 0.0F,-0.6F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(combinedLightIn).next();
        mapBackBuffer.vertex(matrix, -0.6F, 0.0F,-0.6F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(combinedLightIn).next();

        var mapBuffer=vertexConsumers.getBuffer(tileEntityIn.mapRenderType);
        //MinecraftClient.getInstance().getTextureManager().bindTexture(tileEntityIn.mapLocation);
        mapBuffer.vertex(matrix, -0.55F, 0.002F,0.55F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(combinedLightIn).next();
        mapBuffer.vertex(matrix, 0.55F,0.002F, 0.55F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(combinedLightIn).next();
        mapBuffer.vertex(matrix, 0.55F, 0.002F,-0.55F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(combinedLightIn).next();
        mapBuffer.vertex(matrix, -0.55F, 0.002F,-0.55F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(combinedLightIn).next();

        matrixStackIn.pop();

        if(TerritoryConfig.displayOwnerName)
        {
            float fontSize = 1 / 96f;
            matrixStackIn.translate(0, height-0.5, 1 / 12f);
            matrixStackIn.scale(fontSize, -fontSize, fontSize);

            String owner_string = Language.getInstance().get("gui.territory.owner");

            var fontRenderer = MinecraftClient.getInstance().textRenderer;

            fontRenderer.draw(owner_string, -fontRenderer.getWidth(owner_string) / 2f, 0, 0XFFFFFFFF, false,
                                                       matrixStackIn.peek().getPositionMatrix(), vertexConsumers, false, 0, combinedLightIn);

            fontRenderer.draw(tileEntityIn.getOwnerName(), -fontRenderer.getWidth(tileEntityIn.getOwnerName()) / 2f,
                                                       15f, 0XFFFFFFFF, false, matrixStackIn.peek().getPositionMatrix(), vertexConsumers, false, 0, combinedLightIn);
        }

        matrixStackIn.pop();
    }


    private float EasingLerp(float min, float max, float x) {
        return (float) ((-Math.pow(x - 1, 4) + 1) * (max - min) + min);//POWER 3
    }

}
