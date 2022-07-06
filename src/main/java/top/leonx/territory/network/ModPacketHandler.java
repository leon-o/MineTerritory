package top.leonx.territory.network;

import com.google.common.collect.Iterables;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Util;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import top.leonx.territory.TerritoryMod;

import java.util.List;
import java.util.function.Function;

public class ModPacketHandler<T extends PacketListener> {
    final Object2IntMap<Class<? extends Packet<T>>> packetIds = Util.make(new Object2IntOpenHashMap<>(), map -> map.defaultReturnValue(-1));
    private final List<Function<PacketByteBuf, ? extends Packet<T>>> packetFactories = Lists.newArrayList();

    public <P extends Packet<T>> ModPacketHandler<T> register(Class<P> type, Function<PacketByteBuf, P> packetFactory) {
        int i = this.packetFactories.size();
        int j = this.packetIds.put(type, i);
        if (j != -1) {
            String string = "Packet " + type + " is already registered to ID " + j;
            TerritoryMod.LOGGER.error(LogUtils.FATAL_MARKER, string);
            throw new IllegalArgumentException(string);
        }
        this.packetFactories.add(packetFactory);
        return this;
    }

    @Nullable
    public Integer getId(Class<?> packet) {
        int i = this.packetIds.getInt(packet);
        return i == -1 ? null : i;
    }

    @Nullable
    public Packet<?> createPacket(int id, PacketByteBuf buf) {
        Function<PacketByteBuf, ? extends Packet<T>> function = this.packetFactories.get(id);
        return function != null ? function.apply(buf) : null;
    }

    public Iterable<Class<? extends Packet<? extends PacketListener>>> getPacketTypes() {
        return Iterables.unmodifiableIterable(this.packetIds.keySet());
    }
}
