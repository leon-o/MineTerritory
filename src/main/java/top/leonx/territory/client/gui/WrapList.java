package top.leonx.territory.client.gui;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ChangePageButton;

import java.util.ArrayList;
import java.util.List;

public class WrapList extends Widget {

    public WrapList(int xIn, int yIn, int widthIn, int heightIn, String msg) {
        super(xIn, yIn, widthIn, heightIn, msg);
        prevPageButton = new ChangePageButton(xIn, yIn + heightIn - 13, false, $ -> {
            pageNumber=Math.max(0,--pageNumber);
            if(pageNumber==0)
                prevPageButton.active=false;
            nextPageButton.active=true;
        }, true);
        nextPageButton = new ChangePageButton(xIn+widthIn-23, yIn + heightIn - 13, true, $ -> {
            int maxPage=children.size()/entryCountEachPage;
            pageNumber=Math.min(maxPage,++pageNumber);
            if(pageNumber>=maxPage)
                nextPageButton.active=false;
            prevPageButton.active=true;
        }, true);
    }
    ChangePageButton prevPageButton,nextPageButton;
    public final List<Widget> children=new ArrayList<>();
    public int entryCountEachPage=4;
    public int pageNumber=0;
    public int marginRight=8;
    public int marginLeft=8;
    public int marginTop=4;

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        //super.render(mouseX, mouseY, partialTick);
        int left=0,top=0,maxHeightThisLine=0;
        for (int i=pageNumber*entryCountEachPage;i<Math.min(children.size(),(pageNumber+1)*entryCountEachPage);i++) {
            Widget widget=children.get(i);
            if(left+widget.getWidth()>width)
            {
                left=0;
                top+=maxHeightThisLine+marginTop;
                maxHeightThisLine=0;
            }
            maxHeightThisLine=Math.max(widget.getHeight(),maxHeightThisLine);
            widget.x=super.x+left+marginLeft;
            widget.y=super.y+top;
            widget.render(mouseX, mouseY, partialTick);
            left+=widget.getWidth()+marginRight;
        }
        prevPageButton.render(mouseX, mouseY, partialTick);
        nextPageButton.render(mouseX, mouseY, partialTick);
        prevPageButton.renderButton(mouseX, mouseY, partialTick);
        nextPageButton.renderButton(mouseX, mouseY, partialTick);
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
}
