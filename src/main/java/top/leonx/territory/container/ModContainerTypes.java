package top.leonx.territory.container;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.leonx.territory.TerritoryMod;

public class ModContainerTypes{
    public static ExtendedScreenHandlerType<TerritoryTableContainer> TERRITORY_CONTAINER =
            new ExtendedScreenHandlerType<>((TerritoryTableContainer::new));

    public static void register(){
        TERRITORY_CONTAINER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(TerritoryMod.MOD_ID, "table_screen_handler"), TERRITORY_CONTAINER);
    }
}
