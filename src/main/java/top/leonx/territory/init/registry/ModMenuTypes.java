package top.leonx.territory.init.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.leonx.territory.TerritoryMod;
import top.leonx.territory.common.container.TerritoryTableContainer;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, TerritoryMod.MODID);


    public static final RegistryObject<MenuType<TerritoryTableContainer>> TERRITORY_CONTAINER =
            CONTAINER_TYPES.register("territory_table",()-> IForgeMenuType.create(TerritoryTableContainer::new)) ;
}
