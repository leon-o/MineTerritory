package top.leonx.territory.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import top.leonx.territory.core.TerritoryInfo;
import top.leonx.territory.init.registry.ModCaps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkCapabilityProvider extends CapabilityProvider<ChunkCapabilityProvider> implements ICapabilitySerializable<Tag> {

    public static final LazyOptional<TerritoryInfo> instance = LazyOptional.of(TerritoryInfo::new);
    public ChunkCapabilityProvider() {
        super(ChunkCapabilityProvider.class);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap== ModCaps.TERRITORY_INFO_CAPABILITY)
            return instance.cast();
        return LazyOptional.empty();
    }

    @Override
    public Tag serializeNBT() {
        return  instance.resolve().isPresent() ? instance.resolve().get().serializeNBT() : new CompoundTag();
    }


    @Override
    public void deserializeNBT(Tag nbt) {
        instance.ifPresent(territoryInfo -> territoryInfo.deserializeNBT(nbt));
    }


}
