package top.leonx.territory.data;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;
import top.leonx.territory.TerritoryMod;

public class ComponentTypes implements WorldComponentInitializer, ChunkComponentInitializer {
    public static final ComponentKey<WorldTerritoryInfoComponent> WORLD_TERRITORY_INFO =
            ComponentRegistry.getOrCreate(new Identifier(TerritoryMod.MOD_ID, "world_territory_info"),
                    WorldTerritoryInfoComponent.class);

    public static final ComponentKey<ChunkTerritoryInfoComponent> CHUNK_TERRITORY_INFO =
            ComponentRegistry.getOrCreate(new Identifier(TerritoryMod.MOD_ID, "chunk_territory_info"),
                    ChunkTerritoryInfoComponent.class);

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(WORLD_TERRITORY_INFO, WorldTerritoryInfoComponent::new);
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(CHUNK_TERRITORY_INFO, ChunkTerritoryInfoComponent::new);
    }
}
