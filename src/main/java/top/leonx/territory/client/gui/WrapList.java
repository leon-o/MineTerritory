package top.leonx.territory.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class WrapList extends AbstractWidget {

    public WrapList(int xIn, int yIn, int widthIn, int heightIn, Component msg) {
        super(xIn, yIn, widthIn, heightIn, msg);
        prevPageButton = new PageButton(xIn, yIn + heightIn - 13, false, $ -> {
            pageNumber=Math.max(0,--pageNumber);
            if(pageNumber==0)
                prevPageButton.active=false;
            nextPageButton.active=true;
        }, true);
        nextPageButton = new PageButton(xIn+widthIn-23, yIn + heightIn - 13, true, $ -> {
            int maxPage=children.size()/entryCountEachPage;
            pageNumber=Math.min(maxPage,++pageNumber);
            if(pageNumber>=maxPage)
                nextPageButton.active=false;
            prevPageButton.active=true;
        }, true);
    }
    PageButton prevPageButton,nextPageButton;
    public final List<AbstractWidget> children=new ArrayList<>();
    public int entryCountEachPage=4;
    public int pageNumber=0;
    public int marginRight=8;
    public int marginLeft=8;
    public int marginTop=4;

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTick) {
        //super.render(mouseX, mouseY, partialTick);
        int left=0,top=0,maxHeightThisLine=0;
        for (int i=pageNumber*entryCountEachPage;i<Math.min(children.size(),(pageNumber+1)*entryCountEachPage);i++) {
            AbstractWidget widget=children.get(i);
            if(left+widget.getWidth()>width)
            {
                left=0;
                top+=maxHeightThisLine+marginTop;
                maxHeightThisLine=0;
            }
            maxHeightThisLine=Math.max(widget.getHeight(),maxHeightThisLine);
            widget.x=super.x+left+marginLeft;
            widget.y=super.y+top;
            widget.render(matrix,mouseX, mouseY, partialTick);
            left+=widget.getWidth()+marginRight;
        }
        prevPageButton.render(matrix,mouseX, mouseY, partialTick);
        nextPageButton.render(matrix,mouseX, mouseY, partialTick);
        prevPageButton.renderButton(matrix,mouseX, mouseY, partialTick);
        nextPageButton.renderButton(matrix,mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int btn) {
        for (int i=pageNumber*entryCountEachPage;i<Math.min(children.size(),(pageNumber+1)*entryCountEachPage);i++) {
            children.get(i).mouseClicked(mouseX,mouseY,btn);
        }
        prevPageButton.mouseClicked(mouseX,mouseY,btn);
        nextPageButton.mouseClicked(mouseX,mouseY,btn);
        return true;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
