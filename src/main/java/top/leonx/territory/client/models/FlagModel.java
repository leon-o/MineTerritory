package top.leonx.territory.client.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class FlagModel extends Model {
    ModelRenderer flag=new ModelRenderer(this);
    ModelRenderer stick_bottom=new ModelRenderer(this);
    ModelRenderer stick_top=new ModelRenderer(this);

    public FlagModel(Function<ResourceLocation, RenderType> renderTypeIn) {
        super(renderTypeIn);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {

    }
}
