package top.leonx.territory.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import top.leonx.territory.client.gui.PermissionList;
import top.leonx.territory.client.gui.PlayerList;
import top.leonx.territory.client.gui.PermissionToggleButton;
import top.leonx.territory.container.TerritoryTableContainer;
import top.leonx.territory.data.PermissionFlag;
import top.leonx.territory.util.UserUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class TerritoryPermissionScreen extends AbstractScreenPage<TerritoryTableContainer> {

    private PlayerList playerList;
    private PermissionList permissionList;
    private TextFieldWidget search;
    private TextFieldWidget addTextField;

    private final Map<PermissionToggleButton,PermissionFlag> permissionCheckbox=new HashMap<>();
    private GuiButtonExt addPlayerBtn;
    private GuiButtonExt removePlayerBtn;
    PlayerList.PlayerEntry defaultPlayerEntry;
    public TerritoryPermissionScreen(TerritoryTableContainer container,
                                     ContainerScreen<TerritoryTableContainer> parent, Consumer<Integer> changePage) {
        super(container,parent,changePage);
    }
    @Override
    public void init() {
        final int halfW = width / 2;
        //noinspection unused
        final int halfH = height / 2;

        playerList=new PlayerList(minecraft,100,200,parent.getGuiTop()+24,parent.getGuiTop()+parent.getYSize()-24,16);
        playerList.setLeftPos(parent.getGuiLeft()+10);
        defaultPlayerEntry = new PlayerList.PlayerEntry(UserUtil.DEFAULT_UUID, UserUtil.DEFAULT_NAME, playerList,this::onPlayerEntrySelected);
        defaultPlayerEntry.canDelete=false;
        search=new TextFieldWidget(font, parent.getGuiLeft()+10, parent.getGuiTop()+8, 100,16,I18n.format("gui.territory.search"));
        addTextField=new TextFieldWidget(font, parent.getGuiLeft()+10, parent.getGuiTop()+parent.getYSize()-24, 80,16,I18n.format("gui.territory.add_player"));
        addPlayerBtn=new GuiButtonExt(parent.getGuiLeft()+90,parent.getGuiTop()+parent.getYSize()-24,20,16,"+",
                t-> addNewPlayer());
        removePlayerBtn=new GuiButtonExt(parent.getGuiLeft()+120,parent.getGuiTop()+8,24,16,I18n.format("gui.territory.remove_player_btn"),
                t-> removePlayer());
        permissionList=new PermissionList(parent.getGuiLeft()+120,parent.getGuiTop()+30,110,100,"permission_list");

        playerList.children().add(defaultPlayerEntry);
        container.territoryInfo.permissions.forEach((k,v)-> playerList.children().add(new PlayerList.PlayerEntry(k,playerList,this::onPlayerEntrySelected)));
        playerList.setSelected(defaultPlayerEntry);

        int checkboxWidth=50;
        int checkboxHeight=20;
        for (PermissionFlag flag : PermissionFlag.basicFlag) {
            PermissionToggleButton btn=new PermissionToggleButton(0,0,checkboxWidth, checkboxHeight,
                    I18n.format(flag.getTranslationKey()),container.territoryInfo.defaultPermission.contain(flag));

            btn.onTriggered=t->{
                PermissionFlag permissionFlag;
                if(playerList.getSelected().equals(defaultPlayerEntry))
                    permissionFlag=container.territoryInfo.defaultPermission;
                else
                    permissionFlag=container.territoryInfo.permissions.get(playerList.getSelected().getUUID());

                if(t.isStateTriggered())
                    permissionFlag.combine(flag);
                else
                    permissionFlag.remove(flag);
            };
            permissionList.children.add(btn);
            permissionCheckbox.put(btn,flag);
        }

        addTextField.setSuggestion(I18n.format("gui.territory.add_player"));
        search.setSuggestion(I18n.format("gui.territory.search"));
        addPlayerBtn.active=false;

        this.addButton(addPlayerBtn);
        this.addButton(removePlayerBtn);
        this.addButton(new GuiButtonExt(halfW+40, parent.getGuiTop()+parent.getYSize()-30, 70, 20, I18n.format("gui.territory.back"),
                $ -> NavigateTo(0)
        ));
        this.children.add(playerList);
        this.children.add(addTextField);
        this.children.add(search);
        this.children.add(permissionList);

        //permissionCheckbox.keySet().forEach(this::addButton);
    }


    @Override
    public void renderInternal(int mouseX, int mouseY, float partialTicks) {
        playerList.render(mouseX,mouseY,partialTicks);
        permissionList.render(mouseX,mouseY,partialTicks);

        search.render(mouseX,mouseY,partialTicks);
        search.renderButton(mouseX,mouseY,partialTicks);

        addTextField.render(mouseX,mouseY,partialTicks);
        addTextField.renderButton(mouseX,mouseY,partialTicks);

        String title=I18n.format("gui.territory.all_player");
        if(playerList.getSelected()!=null) title=playerList.getSelected().getName();
        GlStateManager.pushMatrix();
        GlStateManager.scaled(1.2,1.2,1.2);
        font.drawString(title, (int)((this.parent.getGuiLeft()+150)/1.2),parent.getGuiTop()+4,0xFFFFF0);
        GlStateManager.popMatrix();
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

    }

    String lastTickSearch=null;

    @Override
    public void tick() {
        if(!search.getText().equals(lastTickSearch))
        {
            playerList.children().clear();
            playerList.children().add(defaultPlayerEntry);
            container.territoryInfo.permissions.entrySet().stream().filter(t-> UsernameCache.getLastKnownUsername(t.getKey()).contains(search.getText())).forEach(
                    t-> playerList.children().add(new PlayerList.PlayerEntry(t.getKey(),playerList,this::onPlayerEntrySelected))
            );
            lastTickSearch=search.getText();
        }
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
            playerList.children().add(new PlayerList.PlayerEntry(uuid,name,playerList,this::onPlayerEntrySelected));
        }
    }
    private void removePlayer() {
        PlayerList.PlayerEntry selected = playerList.getSelected();
        if(!selected.canDelete) return;
        container.territoryInfo.permissions.remove(selected.getUUID());
        playerList.children().remove(selected);
    }
    private void onPlayerEntrySelected(PlayerList.PlayerEntry entry)
    {
        PermissionFlag editingPermission;

        if(UserUtil.isDefaultUser(entry.getUUID()))
            editingPermission=container.territoryInfo.defaultPermission;
        else
            editingPermission = container.territoryInfo.permissions.get(entry.getUUID());

        permissionCheckbox.forEach((box,flag)-> box.setStateTriggered(editingPermission.contain(flag)));

        removePlayerBtn.active=entry.canDelete;
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
            addTextFieldSuggestion=I18n.format("gui.territory.add_player");
        }
        addTextField.setSuggestion(addTextFieldSuggestion);

        if(search.getText().length()>0)
            search.setSuggestion("");
        else
            search.setSuggestion(I18n.format("gui.territory.search"));
    }
}
