package top.leonx.territory.container;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;

public class ModContainerTypes{
    public static ContainerType<TerritoryTableContainer> TERRITORY_CONTAINER=
            IForgeContainerType.create(TerritoryTableContainer::new);
}
