package top.leonx.territory.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.model.BannerModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.math.MathHelper;
import top.leonx.territory.tileentities.TerritoryTableTileEntity;

import java.util.ArrayList;
import java.util.List;

public class TerritoryTableTileEntityRenderer extends TileEntityRenderer<TerritoryTableTileEntity> {

    float wave=0;

    BannerModel bannerModel=new BannerModel();
    List<BannerPattern> patterns=new ArrayList<>();
    List<DyeColor> colors=new ArrayList<>();
    public TerritoryTableTileEntityRenderer()
    {
        patterns.add(BannerPattern.BASE);
        patterns.add(BannerPattern.GRADIENT_UP);
        patterns.add(BannerPattern.GRADIENT);
        colors.add(DyeColor.WHITE);
        colors.add(DyeColor.ORANGE);
        colors.add(DyeColor.PINK);
    }
    @Override
    public void render(TerritoryTableTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        wave+=0.07*partialTicks;
        wave%=Math.PI*2;
        if(tileEntityIn.rise){
            tileEntityIn.height+=0.1*partialTicks;
            tileEntityIn.scale+=0.08f*partialTicks;
        }else{
            tileEntityIn.height-=0.1*partialTicks;
            tileEntityIn.scale-=0.06f*partialTicks;
        }
        tileEntityIn.height=MathHelper.clamp(tileEntityIn.height,0.8f,1.1f);
        tileEntityIn.scale=MathHelper.clamp(tileEntityIn.scale,1/4f,1/2f);
        GlStateManager.pushMatrix();
        GlStateManager.translated(x+0.5,y+tileEntityIn.height,z+0.5);
        GlStateManager.rotatef( 90-tileEntityIn.angle,0,1,0);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(tileEntityIn.scale, -tileEntityIn.scale, -tileEntityIn.scale);

        bannerModel.func_205056_c().rotateAngleX=
                (-0.0125F + 0.01F * MathHelper.cos(wave) * (float)Math.PI);

        this.bindTexture(BannerTextures.BANNER_DESIGNS.getResourceLocation("b"+ "233",
                patterns,colors));

        bannerModel.renderBanner();

        GlStateManager.popMatrix();

        float fontSize=1/96f;

        GlStateManager.translated(0,1,1/12f);
        GlStateManager.rotated(180*(bannerModel.func_205056_c().rotateAngleX)/Math.PI,1,0,0);
        GlStateManager.translated(0,-1,0);
        GlStateManager.scaled(fontSize,-fontSize,fontSize);
        GlStateManager.normal3f(0,0,-fontSize);
        String owner_string= I18n.format("gui.territory.owner");
        Minecraft.getInstance().fontRenderer.drawString(owner_string,
                -Minecraft.getInstance().fontRenderer.getStringWidth(owner_string)/2f,-70,
                0XFFFFFFFF);

        Minecraft.getInstance().fontRenderer.drawString(tileEntityIn.getOwnerName(),
                -Minecraft.getInstance().fontRenderer.getStringWidth(tileEntityIn.getOwnerName())/2f,-60+tileEntityIn.height*10,
                0XFFFFFFFF);

        GlStateManager.popMatrix();
    }
}
