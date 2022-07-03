package top.leonx.territory.client.screen;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import top.leonx.territory.container.TerritoryTableContainer;

public class TerritoryTableScreen extends CottonInventoryScreen<TerritoryTableContainer> {

    public TerritoryTableScreen(TerritoryTableContainer description, PlayerInventory inventory, Text title) {
        super(description, inventory, title);
    }
}
