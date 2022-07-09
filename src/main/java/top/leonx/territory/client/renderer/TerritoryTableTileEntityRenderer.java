package top.leonx.territory.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import top.leonx.territory.common.tileentities.TerritoryTableTileEntity;
import top.leonx.territory.init.config.TerritoryConfig;

public class TerritoryTableTileEntityRenderer implements BlockEntityRenderer<TerritoryTableTileEntity> {

    private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType COMPASS_ADDITION = RenderType.text(new ResourceLocation("territory","textures/block/compass.png"));

    private final Font font;
    public TerritoryTableTileEntityRenderer(BlockEntityRendererProvider.Context pContext) {
        this.font = pContext.getFont();
    }
    private float EasingLerp(float min, float max, float x) {
        return (float) ((-Math.pow(x - 1, 4) + 1) * (max - min) + min);//POWER 3
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(TerritoryTableTileEntity tileEntityIn, float partialTicks, PoseStack PoseStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        if (tileEntityIn.rise) {
            tileEntityIn.height += 0.06 * partialTicks;
            tileEntityIn.scale += 0.04f * partialTicks;
        } else {
            tileEntityIn.height -= 0.06 * partialTicks;
            tileEntityIn.scale -= 0.04f * partialTicks;
        }
        tileEntityIn.height = Mth.clamp(tileEntityIn.height, 0, 1f);
        tileEntityIn.scale = Mth.clamp(tileEntityIn.scale, 0, 1f);
        @SuppressWarnings("SuspiciousNameCombination")
        float height = EasingLerp(0.8f, 1.1f, tileEntityIn.height);
        float scale = EasingLerp(1 / 4f, 1 / 2f, tileEntityIn.scale);

        PoseStackIn.pushPose();
        PoseStackIn.translate(0.5, 0, 0.5);

        var compassBuffer = bufferIn.getBuffer(COMPASS_ADDITION);
        Matrix4f matrix        = PoseStackIn.last().pose();
        compassBuffer.vertex(matrix, -0.5F, 0.752F,0.5F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(combinedLightIn).endVertex();
        compassBuffer.vertex(matrix, 0.5F,0.752F, 0.5F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(combinedLightIn).endVertex();
        compassBuffer.vertex(matrix, 0.5F, 0.752F,-0.5F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(combinedLightIn).endVertex();
        compassBuffer.vertex(matrix, -0.5F, 0.752F,-0.5F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(combinedLightIn).endVertex();

        PoseStackIn.translate(0, height, 0);
        float angleLerp= Mth.rotLerp(partialTicks,tileEntityIn.angleLastTick,tileEntityIn.angle);
        PoseStackIn.mulPose(new Quaternion(new Vector3f(0, 1, 0), 90 - angleLerp, true));

        PoseStackIn.pushPose();
        PoseStackIn.scale(scale,scale,scale);
        PoseStackIn.mulPose(new Quaternion(new Quaternion(new Vector3f(1, 0, 0),30*(3*scale-0.5f),true)));

        var mapBackBuffer = bufferIn.getBuffer(MAP_BACKGROUND);
        matrix = PoseStackIn.last().pose();
        mapBackBuffer.vertex(matrix, -0.6F, 0.0F,0.6F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(combinedLightIn).endVertex();
        mapBackBuffer.vertex(matrix, 0.6F,0.0F, 0.6F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(combinedLightIn).endVertex();
        mapBackBuffer.vertex(matrix, 0.6F, 0.0F,-0.6F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(combinedLightIn).endVertex();
        mapBackBuffer.vertex(matrix, -0.6F, 0.0F,-0.6F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(combinedLightIn).endVertex();

        var mapBuffer=bufferIn.getBuffer(tileEntityIn.mapRenderType);
        mapBuffer.vertex(matrix, -0.55F, 0.002F,0.55F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(combinedLightIn).endVertex();
        mapBuffer.vertex(matrix, 0.55F,0.002F, 0.55F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(combinedLightIn).endVertex();
        mapBuffer.vertex(matrix, 0.55F, 0.002F,-0.55F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(combinedLightIn).endVertex();
        mapBuffer.vertex(matrix, -0.55F, 0.002F,-0.55F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(combinedLightIn).endVertex();

        PoseStackIn.popPose();

        if(TerritoryConfig.displayOwnerName)
        {
            float fontSize = 1 / 96f;
            PoseStackIn.translate(0, height-0.5, 1 / 12f);
            PoseStackIn.scale(fontSize, -fontSize, fontSize);

            String owner_string = I18n.get("gui.territory.owner");

            font.drawInBatch( owner_string, -font.width(owner_string) / 2f, 0, 0XFFFFFFFF, false,
                                                       PoseStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);

            font.drawInBatch(tileEntityIn.getOwnerName(), - font.width(tileEntityIn.getOwnerName()) / 2f,
                                                       15f, 0XFFFFFFFF, false, PoseStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);
        }

        PoseStackIn.popPose();
    }

}
