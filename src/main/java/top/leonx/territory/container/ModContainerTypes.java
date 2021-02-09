package top.leonx.territory.container;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import top.leonx.territory.TerritoryMod;

public class ModContainerTypes{
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, TerritoryMod.MODID);


    public static final RegistryObject<ContainerType<TerritoryTableContainer>> TERRITORY_CONTAINER =
            CONTAINER_TYPES.register("territory_table",()->IForgeContainerType.create(TerritoryTableContainer::new)) ;
}
