package top.leonx.territory.container;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;

public class ModContainerTypes{
    public static ContainerType<TerritoryContainer> TERRITORY_CONTAINER=
            IForgeContainerType.create(TerritoryContainer::new);
}
