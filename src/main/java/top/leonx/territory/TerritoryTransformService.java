package top.leonx.territory;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import top.leonx.territory.transform.SendChunkDataTransform;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public class TerritoryTransformService implements ITransformationService {
    @Nonnull
    @Override
    public String name() {
        return TerritoryMod.MODID;
    }

    @Override
    public void initialize(IEnvironment environment) {

    }

    @Override
    public void beginScanning(IEnvironment environment) {

    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @SuppressWarnings("rawtypes")
    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        List<ITransformer> list=new ArrayList<>();
        list.add(new SendChunkDataTransform());
        return list;
    }
}
