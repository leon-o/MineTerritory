package top.leonx.territory.component;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;
import top.leonx.territory.TerritoryMod;

public class ComponentContainer implements ChunkComponentInitializer {
    public static final ComponentKey<ChunkTerritoryInfoComponent> TERRITORY_INFO =
            ComponentRegistry.getOrCreate(new Identifier(TerritoryMod.MOD_ID, "chunk_territory_info"),
                                          ChunkTerritoryInfoComponent.class);
    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(TERRITORY_INFO,ChunkTerritoryInfoComponent::new);
    }
}
