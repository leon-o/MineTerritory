package top.leonx.territory.client.screen;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import top.leonx.territory.client.gui.PlayerList;
import top.leonx.territory.container.TerritoryContainer;
import top.leonx.territory.data.PermissionFlag;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TerritoryPermissionScreen extends AbstractScreenPage<TerritoryContainer> {

    private PlayerList playerList;
    private TextFieldWidget search;
    private TextFieldWidget addTextField;
    @SuppressWarnings("unused")
    private CheckboxButton enterCheckbox;
    private CheckboxButton breakCheckbox;
    private CheckboxButton interactCheckbox;
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

        this.addButton(new GuiButtonExt(halfW-110+80,parent.getGuiTop()+parent.getYSize()-24,20,16,"+",t-> addNewPlayer()));
        breakCheckbox=new CheckboxButton(halfW,parent.getGuiTop()+8,50,20,"Break",true);
        this.addButton(breakCheckbox);
        interactCheckbox =new CheckboxButton(halfW,parent.getGuiTop()+36,50,20,"Right Click",true);
        this.addButton(interactCheckbox);

        this.children.add(playerList);
        this.children.add(addTextField);
        this.children.add(playerList);
        this.children.add(search);

    }

    private void addNewPlayer() {
        String name=addTextField.getText();
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
        if(container.territoryInfo.permissions.containsKey(uuid)){
            UUID finalUuid = uuid;
            //noinspection OptionalGetWithoutIsPresent
            playerList.setSelected(playerList.children().stream().filter(t->t.getUUID().equals(finalUuid)).findFirst().get());
        }
        else{
            container.territoryInfo.permissions.put(uuid,new PermissionFlag());
            playerList.children().add(playerList.new PlayerEntry(uuid,name,playerList));
        }
        //container.detectAndSendChanges();
    }

    @Override
    public void renderInternal(int mouseX, int mouseY, float partialTicks) {
        playerList.render(mouseX,mouseY,partialTicks);
        search.render(mouseX,mouseY,partialTicks);
        if(!search.isFocused() && search.getText().length()>0)
        {
            font.drawString(search.getMessage(),search.x+4,search.y+4,0xCCCCCCCC);
        }
        addTextField.render(mouseX,mouseY,partialTicks);

        if(!addTextField.isFocused() && addTextField.getText().length()>0)
        {
            font.drawString(addTextField.getMessage(),addTextField.x+4,addTextField.y+4,0xCCCCCCCC);
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

    }

    String lastTickSearch=null;
    PlayerList.PlayerEntry lastPlayerEntry;
    boolean lastTickBreakChecked=false;
    boolean lastTickInteracteChecked=false;
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
            PermissionFlag flag = container.territoryInfo.permissions.get(lastPlayerEntry.getUUID());
            if(breakCheckbox.isChecked()^flag.contain(PermissionFlag.BREAK))
                breakCheckbox.onPress();
            if(interactCheckbox.isChecked()^flag.contain(PermissionFlag.INTERACTE))
                interactCheckbox.onPress();

            lastTickBreakChecked=flag.contain(PermissionFlag.BREAK);
            lastTickInteracteChecked=flag.contain(PermissionFlag.INTERACTE);
        }
        if(breakCheckbox.isChecked()^lastTickBreakChecked|| interactCheckbox.isChecked()^lastTickInteracteChecked)
        {
            PermissionFlag flag=new PermissionFlag();
            if(breakCheckbox.isChecked())
                flag.combine(PermissionFlag.BREAK);
            if(interactCheckbox.isChecked())
                flag.combine(PermissionFlag.INTERACTE);

            container.territoryInfo.permissions.replace(playerList.getSelected().getUUID(),flag);

            lastTickBreakChecked=breakCheckbox.isChecked();
            lastTickInteracteChecked= interactCheckbox.isChecked();
        }
    }
}
