package top.leonx.territory.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import top.leonx.territory.data.TerritoryInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static top.leonx.territory.capability.ModCapabilities.TERRITORY_INFO_CAPABILITY;

public class ChunkCapabilityProvider implements ICapabilitySerializable<INBT> {

    private final TerritoryInfo territoryInfo=TERRITORY_INFO_CAPABILITY.getDefaultInstance();
    private final LazyOptional<TerritoryInfo> instance = LazyOptional.of(()->territoryInfo);
    private final Capability.IStorage<TerritoryInfo> storage = TERRITORY_INFO_CAPABILITY.getStorage();
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap== TERRITORY_INFO_CAPABILITY)
            return instance.cast();
        return null;
    }

    @Override
    public INBT serializeNBT() {
        return storage.writeNBT(TERRITORY_INFO_CAPABILITY,instance.orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance()),null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        storage.readNBT(TERRITORY_INFO_CAPABILITY,instance.orElse(TERRITORY_INFO_CAPABILITY.getDefaultInstance()),null,nbt);
    }
}
