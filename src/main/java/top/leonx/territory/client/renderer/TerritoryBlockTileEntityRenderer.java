package top.leonx.territory.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.model.BannerModel;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.math.MathHelper;
import top.leonx.territory.tileentities.TerritoryTileEntity;

import java.util.ArrayList;
import java.util.List;

public class TerritoryBlockTileEntityRenderer extends TileEntityRenderer<TerritoryTileEntity> {

    float wave=0;
    float scale=1/6f;
    float height=0.8f;
    BannerModel bannerModel=new BannerModel();
    List<BannerPattern> patterns=new ArrayList<>();
    List<DyeColor> colors=new ArrayList<>();
    public TerritoryBlockTileEntityRenderer()
    {
        patterns.add(BannerPattern.BASE);
        colors.add(DyeColor.ORANGE);
    }
    @Override
    public void render(TerritoryTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        wave+=0.07*partialTicks;
        wave%=Math.PI*2;

        GlStateManager.pushMatrix();
        if(tileEntityIn.rise)
        {
            if(height<1.1f)
            {
                height+=0.1*partialTicks;
            }
            if(scale<1/2f)
            {
                scale+=0.08f*partialTicks;
            }
        }else{
            if(height>0.8f)
            {
                height-=0.1*partialTicks;
            }
            if(scale>1/4f)
            {
                scale-=0.06f*partialTicks;
            }
        }
        GlStateManager.translated(x+0.5,y+height,z+0.5);
        GlStateManager.rotatef( 90-tileEntityIn.angle,0,1,0);

        GlStateManager.pushMatrix();

        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(scale, -scale, -scale);


        bannerModel.func_205056_c().rotateAngleX=
                (-0.0125F + 0.01F * MathHelper.cos(wave) * (float)Math.PI);

        this.bindTexture(BannerTextures.BANNER_DESIGNS.getResourceLocation("b"+ colors.get(0).getId(),
                patterns,colors));
//        this.bindTexture(new ResourceLocation("textures/entity/banner_base.png"));
        bannerModel.renderBanner();

        GlStateManager.popMatrix();

        float fontSize=1/96f;

        GlStateManager.translated(0,0,1/8f);
        GlStateManager.scaled(fontSize,-fontSize,fontSize);
        GlStateManager.normal3f(0,0,-fontSize);
        Minecraft.getInstance().fontRenderer.drawString("Owner:",
                -Minecraft.getInstance().fontRenderer.getStringWidth("Owner:")/2f,-70,
                0XFFFFFFFF);

        Minecraft.getInstance().fontRenderer.drawString(tileEntityIn.getOwnerName(),
                -Minecraft.getInstance().fontRenderer.getStringWidth(tileEntityIn.getOwnerName())/2f,-60+height*10,
                0XFFFFFFFF);

        GlStateManager.popMatrix();
    }
}
