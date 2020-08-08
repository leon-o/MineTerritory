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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.math.MathHelper;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;

import java.util.ArrayList;
import java.util.List;

public class TerritoryTableTileEntityRenderer extends TileEntityRenderer<TerritoryTableTileEntity> {

    private final ModelRenderer flag        = new ModelRenderer(64, 64, 0, 0);
    private final ModelRenderer stickBottom = new ModelRenderer(64, 64, 44, 0);
    private final ModelRenderer stickTop    = new ModelRenderer(64, 64, 0, 42);
    float wave = 0;

    //BannerModel bannerModel=new BannerModel();
    List<Pair<BannerPattern, DyeColor>> patterns = new ArrayList<>();

    public TerritoryTableTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        patterns.add(new Pair<>(BannerPattern.BASE, DyeColor.WHITE));
        patterns.add(new Pair<>(BannerPattern.GRADIENT_UP, DyeColor.ORANGE));
        patterns.add(new Pair<>(BannerPattern.GRADIENT, DyeColor.PINK));

        this.stickBottom.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
        this.stickTop.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
        this.flag.addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
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
        wave += 0.07 * partialTicks;
        wave %= Math.PI * 2;
        if (tileEntityIn.rise) {
            tileEntityIn.height += 0.09 * partialTicks;
            tileEntityIn.scale += 0.06f * partialTicks;
        } else {
            tileEntityIn.height -= 0.09 * partialTicks;
            tileEntityIn.scale -= 0.06f * partialTicks;
        }
        tileEntityIn.height = MathHelper.clamp(tileEntityIn.height, 0, 1f);
        tileEntityIn.scale = MathHelper.clamp(tileEntityIn.scale, 0, 1f);
        @SuppressWarnings("SuspiciousNameCombination")
        float height = EasingLerp(0.8f, 1.1f, tileEntityIn.height);
        float scale = EasingLerp(1 / 4f, 1 / 2f, tileEntityIn.scale);

        matrixStackIn.push();
        matrixStackIn.translate(0.5, height, 0.5);
        matrixStackIn.rotate(new Quaternion(new Vector3f(0, 1, 0), 90 - tileEntityIn.angle, true));
        matrixStackIn.push();
        RenderSystem.enableRescaleNormal();
        matrixStackIn.scale(scale, -scale, -scale);

        IVertexBuilder ivertexbuilder = ModelBakery.LOCATION_BANNER_BASE.getBuffer(bufferIn, RenderType::getEntitySolid);
        this.stickBottom.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn);
        this.stickTop.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, -2, 0);
        this.flag.rotateAngleX = (-0.0125F + 0.01F * MathHelper.cos(wave) * (float) Math.PI);
        renderFlag(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, this.flag, ModelBakery.LOCATION_BANNER_BASE, true, this.patterns);

        matrixStackIn.pop();

        float fontSize = 1 / 96f;

        matrixStackIn.translate(0, -0.5 + height, 0);
        matrixStackIn.translate(0, 1, 1 / 12f);
        matrixStackIn.rotate(new Quaternion(new Vector3f(1, 0, 0), flag.rotateAngleX, false));
        matrixStackIn.translate(0, -1, 0);

        matrixStackIn.scale(fontSize, -fontSize, fontSize);
        String owner_string = I18n.format("gui.territory.owner");


        renderDispatcher.fontRenderer.renderString(owner_string, -renderDispatcher.fontRenderer.getStringWidth(owner_string) / 2f, 0, 0XFFFFFFFF, false,
                                                   matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);

        renderDispatcher.fontRenderer.renderString(tileEntityIn.getOwnerName(), -renderDispatcher.fontRenderer.getStringWidth(tileEntityIn.getOwnerName()) / 2f,
                                                   15f, 0XFFFFFFFF, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);

        matrixStackIn.pop();
    }
}
