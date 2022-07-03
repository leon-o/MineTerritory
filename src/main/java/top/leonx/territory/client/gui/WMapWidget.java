package top.leonx.territory.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;

import java.util.Collection;
import java.util.List;

import static net.minecraft.client.gui.DrawableHelper.drawTexture;

public class WMapWidget extends WWidget {
    private final Identifier mapTextureIdentifier;
    private static final Identifier territorySquareLocation = new Identifier("minecraft", "textures/block/blue_stained_glass.png");
    private static final Identifier edgeSquareLocation      = new Identifier("territory", "textures/gui/point_overlay.png");
    private static final Identifier mouseOnSquareLocation   = new Identifier("minecraft", "textures/block/light_blue_stained_glass.png");
    private static final Identifier expandSquareLocation    = new Identifier("minecraft", "textures/block/cyan_stained_glass.png");
    private static final Identifier forbiddenSquareLocation = new Identifier("minecraft", "textures/block/red_stained_glass.png");
    private static final Identifier xpIconLocation          = new Identifier("territory", "textures/gui/xp_icon.png");
    public WMapWidget(Identifier mapTextureIdentifier,int size) {
        this.width=this.height=size;
        this.mapTextureIdentifier = mapTextureIdentifier;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        ScreenDrawing.texturedRect(matrices, x, y, width, height, mapTextureIdentifier, 0xFFFFFFFF);
        drawChunkOverlay(matrices,x+mapLeftTopPos.x(),y+mapLeftTopPos.y(),mouseX,mouseY);
    }

    private Collection<ChunkPos> territories;
    private Collection<ChunkPos> removables;
    private Collection<ChunkPos> addable;
    private Collection<ChunkPos> forbiddings;
    private ChunkPos mapLeftTopChunkPos;
    private Vec2i mapLeftTopPos;
    private int chunkNumX;
    private int chunkNumY;
    private float widthPerChunk;



    public void SetChunkInfo(List<ChunkPos> territories,List<ChunkPos> removeables,
                             List<ChunkPos> addable,List<ChunkPos> forbidens,ChunkPos center,int mapSize){
        this.territories = territories;
        this.removables = removeables;
        this.addable = addable;
        this.forbiddings = forbidens;

        int chunkNum =  mapSize>>4;
        if(chunkNum%2==0){
            chunkNum-=1;
        }
        mapLeftTopChunkPos = new ChunkPos(center.x - (chunkNum/2),
                                          center.z - (chunkNum/2));
        chunkNumX = chunkNumY = chunkNum;

        widthPerChunk = (float) width/mapSize*16;
        float availableAreaSize = (chunkNum*widthPerChunk);
        mapLeftTopPos = new Vec2i((int) (mapSize-availableAreaSize)/2-2, (int)(mapSize-availableAreaSize)/2-2);
    }

    private void drawChunkOverlay(MatrixStack matrix,int mapPosLeft,int mapPosTop, int mouseX, int mouseY) {
        if(territories==null)
            return;

        //GlStateManager._enableBlend();
        //GlStateManager._blendFunc(GlStateManager.SrcFactor.SRC_ALPHA.value, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value);
        //client.getTextureManager().bindTexture(territorySquareLocation);

        territories.forEach(pos -> {
            int posX = pos.x - mapLeftTopChunkPos.x;
            int posZ = pos.z - mapLeftTopChunkPos.z;
            ScreenDrawing.texturedRect(matrix, (int) (mapPosLeft+posX*widthPerChunk),
                                       (int) (mapPosTop+posZ*widthPerChunk), (int) widthPerChunk,
                                       (int) widthPerChunk, territorySquareLocation, 0xFFFFFFFF );
        });

        matrix.push();
        matrix.translate(mapPosLeft,mapPosTop,0);
        drawOverlayByCollection(matrix, expandSquareLocation,addable);
        drawOverlayByCollection(matrix, edgeSquareLocation, removables);
        drawOverlayByCollection(matrix, forbiddenSquareLocation, forbiddings);

        matrix.pop();
        int left = 0 ,top = 0; //todo left and top
        // 绘制鼠标悬浮的区
        int mouseOnChunkX = (int) ((mouseX) / widthPerChunk)+1;
        int mouseOnChunkY = (int) ((mouseY) / widthPerChunk)+1;

        ChunkPos mouseOverPos = new ChunkPos(mapLeftTopChunkPos.x + mouseOnChunkX, mapLeftTopChunkPos.z + mouseOnChunkY);

        ScreenDrawing.texturedRect(matrix, (int) (mapPosLeft+mouseOnChunkX*widthPerChunk),
                                   (int) (mapPosTop+mouseOnChunkY*widthPerChunk), (int) widthPerChunk,
                                   (int) widthPerChunk, mouseOnSquareLocation, 0xFFFFFFFF );
        /*if (removables.contains(mouseOverPos)) {
            ScreenDrawing.texturedRect(matrix, (int) (mapPosLeft+mouseOnChunkX*widthPerChunk),
                                       (int) (mapPosTop+mouseOnChunkY*widthPerChunk), (int) widthPerChunk,
                                       (int) widthPerChunk, mouseOnSquareLocation, 0xFFFFFFFF );
            //drawTexture(matrix,mapPosLeft + (mouseOnChunkX << 4), mapPosTop + (mouseOnChunkY << 4), 0, 0, 16, 16, 16, 16);
        }*/
    }

    private void drawOverlayByCollection(MatrixStack matrix,Identifier texture, Collection<ChunkPos> collection) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f model = matrix.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);


        for (ChunkPos pos : collection) {
            int posX = pos.x - mapLeftTopChunkPos.x;
            int posZ = pos.z - mapLeftTopChunkPos.z;
            if (posX < 0 || posZ < 0 || posX >= chunkNumX || posZ >= chunkNumY) continue;

            float x = posX*widthPerChunk;
            float y = posZ*widthPerChunk;
            buffer.vertex(model, x, y + widthPerChunk, 0.0F).texture(0, 1).next();
            buffer.vertex(model, x + widthPerChunk, y + widthPerChunk, 0.0F).texture(1, 1).next();
            buffer.vertex(model, x + widthPerChunk, y, 0.0F).texture(1, 0).next();
            buffer.vertex(model, x, y, 0.0F).texture(0, 0).next();
            //drawTexture(matrix,mapPosLeft + (posX << 4), mapPosTop + (posZ << 4), 0, 0, 16, 16, 16, 16);
        }

        buffer.end();
        BufferRenderer.draw(buffer);
        RenderSystem.disableBlend();
    }
}
