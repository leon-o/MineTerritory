package top.leonx.territory.client.screen;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import top.leonx.territory.client.gui.CheckBoxButtonEx;
import top.leonx.territory.client.gui.PlayerList;
import top.leonx.territory.container.TerritoryContainer;
import top.leonx.territory.data.PermissionFlag;

import java.util.*;
import java.util.function.Consumer;

public class TerritoryPermissionScreen extends AbstractScreenPage<TerritoryContainer> {

    private PlayerList playerList;
    private TextFieldWidget search;
    private TextFieldWidget addTextField;
    @SuppressWarnings("unused")
    private final Map<CheckBoxButtonEx,PermissionFlag> permissionCheckbox=new HashMap<>();
//    private CheckboxButton enterCheckbox;
//    private CheckboxButton breakCheckbox;
//    private CheckboxButton interactCheckbox;
    private GuiButtonExt addPlayerBtn;
    public TerritoryPermissionScreen(TerritoryContainer container,
                                     ContainerScreen<TerritoryContainer> parent, Consumer<Integer> changePage) {
        super(container,parent,changePage);
    }
    @Override
    public void init() {
        final int halfW = width / 2;
        //noinspection unused
        final int halfH = height / 2;

        this.addButton(new GuiButtonExt(halfW+40, parent.getGuiTop()+parent.getYSize()-30, 70, 20, "BACK",
                $ -> NavigateTo(0)
        ));
        playerList=new PlayerList(minecraft,100,200,parent.getGuiTop()+24,parent.getGuiTop()+parent.getYSize()-24,16);
        playerList.setLeftPos(halfW-110);
        container.territoryInfo.permissions.forEach((k,v)->{
//            ServerPlayerEntity player = Minecraft.getInstance().getIntegratedServer().getPlayerList().getPlayerByUUID(k);
            playerList.children().add(playerList.new PlayerEntry(k,playerList));
        });

        search=new TextFieldWidget(font, halfW-110, parent.getGuiTop()+8, 100,16,"search");
        addTextField=new TextFieldWidget(font, halfW-110, parent.getGuiTop()+parent.getYSize()-24, 80,16,"NAME");
        addPlayerBtn=new GuiButtonExt(halfW-110+80,parent.getGuiTop()+parent.getYSize()-24,20,16,"+",
                t-> addNewPlayer());

        int checkboxTop=parent.getGuiTop()+8;
        int checkboxWidth=50;
        int checkboxHeight=20;
        int checkboxIndexTmp=0;
        for (PermissionFlag flag : PermissionFlag.basicFlag) {
            CheckBoxButtonEx btn=new CheckBoxButtonEx(halfW,checkboxTop+(checkboxHeight+4)*checkboxIndexTmp,checkboxWidth,checkboxHeight,flag.getName()
                    ,true);
            btn.onCheckedChange=isChecked->{
                if(playerList.getSelected()== null) return;
                PermissionFlag permission = container.territoryInfo.permissions.get(playerList.getSelected().getUUID());
                if(isChecked)
                    permission.combine(flag);
                else
                    permission.remove(flag);
            };
            permissionCheckbox.put(btn,flag);
            checkboxIndexTmp++;
        }

        addTextField.setSuggestion("ADD PLAYER");
        search.setSuggestion("SEARCH");
        addPlayerBtn.active=false;

        this.children.add(playerList);
        this.children.add(addTextField);
        this.children.add(search);
        this.addButton(addPlayerBtn);
        permissionCheckbox.keySet().forEach(this::addButton);
    }

    @Override
    public void renderInternal(int mouseX, int mouseY, float partialTicks) {
        playerList.render(mouseX,mouseY,partialTicks);

        search.render(mouseX,mouseY,partialTicks);
        search.renderButton(mouseX,mouseY,partialTicks);

        addTextField.render(mouseX,mouseY,partialTicks);
        addTextField.renderButton(mouseX,mouseY,partialTicks);

    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

    }

    String lastTickSearch=null;
    PlayerList.PlayerEntry lastPlayerEntry;
    @Override
    public void tick() {
        if(!search.getText().equals(lastTickSearch))
        {
            playerList.children().clear();
            container.territoryInfo.permissions.entrySet().stream().filter(t-> UsernameCache.getLastKnownUsername(t.getKey()).contains(search.getText())).forEach(
                    t-> playerList.children().add(playerList.new PlayerEntry(t.getKey(),playerList))
            );
            lastTickSearch=search.getText();
        }


        if(playerList.getSelected()== null) return;
        if(lastPlayerEntry!=playerList.getSelected())
        {
            lastPlayerEntry=playerList.getSelected();
            PermissionFlag permission = container.territoryInfo.permissions.get(lastPlayerEntry.getUUID());
            permissionCheckbox.forEach((box,flag)-> box.setIsChecked(permission.contain(flag)));

            //lastTickBreakChecked=flag.contain(PermissionFlag.BREAK);
            //lastTickInteractChecked =flag.contain(PermissionFlag.PLACE);
        }
//        if(breakCheckbox.isChecked()^lastTickBreakChecked|| interactCheckbox.isChecked()^ lastTickInteractChecked)
//        {
//            PermissionFlag flag=new PermissionFlag();
//            if(breakCheckbox.isChecked())
//                flag.combine(PermissionFlag.BREAK);
//            if(interactCheckbox.isChecked())
//                flag.combine(PermissionFlag.PLACE);
//
//            container.territoryInfo.permissions.replace(playerList.getSelected().getUUID(),flag);
//
//            lastTickBreakChecked=breakCheckbox.isChecked();
//            lastTickInteractChecked = interactCheckbox.isChecked();
//        }
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        boolean result= super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        updateSuggestion();
        return result;
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        boolean r=super.charTyped(p_charTyped_1_,p_charTyped_2_);
        updateSuggestion();
        return r;
    }
    private String addTextFieldSuggestion; //why is there no direct way to get;
    private void addNewPlayer() {
        String name=addTextField.getText()+addTextFieldSuggestion;
        UUID uuid=null;
        for (Map.Entry<UUID,String> entry:UsernameCache.getMap().entrySet())
        {
            if(entry.getValue().equals(name))
            {
                uuid=entry.getKey();
                break;
            }
        }

        if(uuid==null)
        {
            return;
        }
        addTextField.setText("");
        updateSuggestion();
        if(container.territoryInfo.permissions.containsKey(uuid)){
            UUID finalUuid = uuid;
            //noinspection OptionalGetWithoutIsPresent
            playerList.setSelected(playerList.children().stream().filter(t->t.getUUID().equals(finalUuid)).findFirst().get());
        }
        else{
            container.territoryInfo.permissions.put(uuid,new PermissionFlag());
            playerList.children().add(playerList.new PlayerEntry(uuid,name,playerList));
        }
    }

    private void updateSuggestion()
    {
        if(addTextField.getText().length()>0)
        {
            Optional<Map.Entry<UUID, String>> first =
                    UsernameCache.getMap().entrySet().stream().filter(t -> t.getValue().indexOf(addTextField.getText())==0).findFirst();
            if(first.isPresent())
            {
                addTextFieldSuggestion=first.get().getValue().replace(addTextField.getText(),"");
                addPlayerBtn.active=true;
            }else{
                addPlayerBtn.active=false;
                addTextFieldSuggestion="";
            }
        }else{
            addPlayerBtn.active=false;
            addTextFieldSuggestion="ADD PLAYER";
        }
        addTextField.setSuggestion(addTextFieldSuggestion);

        if(search.getText().length()>0)
            search.setSuggestion("");
        else
            search.setSuggestion("SEARCH");
    }
}
