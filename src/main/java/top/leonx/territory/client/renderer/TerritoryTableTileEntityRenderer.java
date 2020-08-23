package top.leonx.territory.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.MapData;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;

import java.util.ArrayList;
import java.util.List;

public class TerritoryTableTileEntityRenderer extends TileEntityRenderer<TerritoryTableTileEntity> {

    private static final RenderType MAP_BACKGROUND = RenderType.getText(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType COMPASS_ADDITION = RenderType.getText(new ResourceLocation("territory","textures/block/compass.png"));

    public TerritoryTableTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    public static void renderFlag(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn, int packedOverlayIn, ModelRenderer modelRenderer, Material material, boolean isBanner, List<Pair<BannerPattern, DyeColor>> pairs) {
        modelRenderer.render(matrixStack, material.getBuffer(buffer, RenderType::getEntitySolid), packedLightIn, packedOverlayIn);

        for (int i = 0; i < 17 && i < pairs.size(); ++i) {
            Pair<BannerPattern, DyeColor> pair   = pairs.get(i);
            float[]                       afloat = pair.getSecond().getColorComponentValues();
            Material material2 = new Material(isBanner ? Atlases.BANNER_ATLAS : Atlases.SHIELD_ATLAS, pair.getFirst().func_226957_a_(isBanner));
            modelRenderer.render(matrixStack, material2.getBuffer(buffer, RenderType::getEntityNoOutline), packedLightIn, packedOverlayIn, afloat[0], afloat[1],
                                 afloat[2], 1.0F);
        }

    }

    private float EasingLerp(float min, float max, float x) {
        return (float) ((-Math.pow(x - 1, 4) + 1) * (max - min) + min);//POWER 3
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(TerritoryTableTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

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

        IVertexBuilder compassBuffer = bufferIn.getBuffer(COMPASS_ADDITION);
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
        compassBuffer.pos(matrix4f, -0.5F, 0.752F,0.5F).color(255, 255, 255, 255).tex(0.0F, 1.0F).lightmap(combinedLightIn).endVertex();
        compassBuffer.pos(matrix4f, 0.5F,0.752F, 0.5F).color(255, 255, 255, 255).tex(1.0F, 1.0F).lightmap(combinedLightIn).endVertex();
        compassBuffer.pos(matrix4f, 0.5F, 0.752F,-0.5F).color(255, 255, 255, 255).tex(1.0F, 0.0F).lightmap(combinedLightIn).endVertex();
        compassBuffer.pos(matrix4f, -0.5F, 0.752F,-0.5F).color(255, 255, 255, 255).tex(0.0F, 0.0F).lightmap(combinedLightIn).endVertex();

        matrixStackIn.translate(0, height, 0);
        float angleLerp=MathHelper.interpolateAngle(partialTicks,tileEntityIn.angleLastTick,tileEntityIn.angle);
        matrixStackIn.rotate(new Quaternion(new Vector3f(0, 1, 0), 90 - angleLerp, true));

        matrixStackIn.push();
        RenderSystem.enableRescaleNormal();
        matrixStackIn.scale(scale,scale,scale);
        matrixStackIn.rotate(new Quaternion(new Quaternion(new Vector3f(1, 0, 0),30*(3*scale-0.5f),true)));

        IVertexBuilder mapBackBuffer = bufferIn.getBuffer(MAP_BACKGROUND);
        matrix4f = matrixStackIn.getLast().getMatrix();
        mapBackBuffer.pos(matrix4f, -0.6F, 0.0F,0.6F).color(255, 255, 255, 255).tex(0.0F, 1.0F).lightmap(combinedLightIn).endVertex();
        mapBackBuffer.pos(matrix4f, 0.6F,0.0F, 0.6F).color(255, 255, 255, 255).tex(1.0F, 1.0F).lightmap(combinedLightIn).endVertex();
        mapBackBuffer.pos(matrix4f, 0.6F, 0.0F,-0.6F).color(255, 255, 255, 255).tex(1.0F, 0.0F).lightmap(combinedLightIn).endVertex();
        mapBackBuffer.pos(matrix4f, -0.6F, 0.0F,-0.6F).color(255, 255, 255, 255).tex(0.0F, 0.0F).lightmap(combinedLightIn).endVertex();

        IVertexBuilder mapBuffer=bufferIn.getBuffer(tileEntityIn.mapRenderType);
        mapBuffer.pos(matrix4f, -0.55F, 0.002F,0.55F).color(255, 255, 255, 255).tex(0.0F, 1.0F).lightmap(combinedLightIn).endVertex();
        mapBuffer.pos(matrix4f, 0.55F,0.002F, 0.55F).color(255, 255, 255, 255).tex(1.0F, 1.0F).lightmap(combinedLightIn).endVertex();
        mapBuffer.pos(matrix4f, 0.55F, 0.002F,-0.55F).color(255, 255, 255, 255).tex(1.0F, 0.0F).lightmap(combinedLightIn).endVertex();
        mapBuffer.pos(matrix4f, -0.55F, 0.002F,-0.55F).color(255, 255, 255, 255).tex(0.0F, 0.0F).lightmap(combinedLightIn).endVertex();

        matrixStackIn.pop();

        float fontSize = 1 / 96f;


        matrixStackIn.translate(0, height-0.5, 1 / 12f);

        matrixStackIn.scale(fontSize, -fontSize, fontSize);
        String owner_string = I18n.format("gui.territory.owner");


        renderDispatcher.fontRenderer.renderString(owner_string, -renderDispatcher.fontRenderer.getStringWidth(owner_string) / 2f, 0, 0XFFFFFFFF, false,
                                                   matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);

        renderDispatcher.fontRenderer.renderString(tileEntityIn.getOwnerName(), -renderDispatcher.fontRenderer.getStringWidth(tileEntityIn.getOwnerName()) / 2f,
                                                   15f, 0XFFFFFFFF, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);

        matrixStackIn.pop();
    }
}
