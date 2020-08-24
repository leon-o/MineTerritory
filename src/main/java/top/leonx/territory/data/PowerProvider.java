package top.leonx.territory.data;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;

public class PowerProvider {
    private final Item        item;
    private final CompoundNBT tag;
    private final double      power;

    public PowerProvider(Item item, CompoundNBT tag, double power) {
        this.item = item;
        this.tag = tag;
        this.power = power;
    }

    public Item getItem() {
        return item;
    }

    public CompoundNBT getTag() {
        return tag;
    }

    public double getPower() {
        return power;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PowerProvider that = (PowerProvider) o;
        return Double.compare(that.power, power) == 0 && item.equals(that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag, power);
    }
}
