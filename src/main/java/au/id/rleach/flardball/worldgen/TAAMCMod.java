package au.id.rleach.flardball.worldgen;

import au.id.rleach.flardball.Plugin;
import com.google.common.base.Objects;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TAAMCMod implements WorldGeneratorModifier {

    Range range;
    Optional<WorldGenerator> wg;


    public TAAMCMod(Range range) {
        this.range = range;
        wg = Optional.empty();
    }

    public TAAMCMod(Range range, WorldGenerator wg){
        this.wg = Optional.of(wg);
    }


    @Override public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator wgOut) {

        WorldGenerator wgIn = this.wg.orElse(wgOut);

        modifyBiomeSettings(wgIn, wgOut);

        GenerationPopulator ogBase = wgOut.getBaseGenerationPopulator();
        GenerationPopulator append = wgIn.getBaseGenerationPopulator();
        GenerationPopulator newBase = transformBase(ogBase, append);
        wgOut.setBaseGenerationPopulator(newBase);

        BiomeGenerator ogBiomes = wgOut.getBiomeGenerator();
        BiomeGenerator appendBiome = wgIn.getBiomeGenerator();
        BiomeGenerator newBiomeGen = transform(ogBiomes, appendBiome);
        wgOut.setBiomeGenerator(newBiomeGen);

        //mutable
        List<GenerationPopulator> ogGenPop = wgIn.getGenerationPopulators();
        List<GenerationPopulator> newGenPop = transformGen(ogGenPop);
        ogGenPop.clear();
        wgOut.getGenerationPopulators().addAll(newGenPop);

        //mutable
        List<Populator> ogPop = wgIn.getPopulators();
        List<Populator> newPop = transformPop(ogPop);
        ogPop.clear();
        wgOut.getPopulators().addAll(newPop);
    }

    private void modifyBiomeSettings(WorldGenerator wgIn, WorldGenerator wgOut) {
        Collection<BiomeType> allBiomes = Sponge.getRegistry().getAllOf(BiomeType.class);
        for(BiomeType t:allBiomes){

            BiomeGenerationSettings settingsIn = wgIn.getBiomeSettings(t);
            BiomeGenerationSettings settingsOut = wgOut.getBiomeSettings(t);

            List<GenerationPopulator> oldGen = settingsIn.getGenerationPopulators();
            List<GenerationPopulator> newGen = transformGen(oldGen);
            settingsOut.getGenerationPopulators().addAll(newGen);

            List<Populator> oldPop = settingsIn.getPopulators();
            List<Populator> newPop = transformPop(oldPop);
            settingsOut.getPopulators().addAll(newPop);
        }

    }

    protected GenerationPopulator transformBase(GenerationPopulator original, GenerationPopulator append) {
        if(! (original instanceof TAAMCBaseGenerationPopulator)) {
            return new TAAMCBaseGenerationPopulator(original, range);
        }
        else {
            ((TAAMCBaseGenerationPopulator) original).append(range, append);
            return original;
        }
    }

    protected BiomeGenerator transform(BiomeGenerator ogBiomes, BiomeGenerator append) {

        if(!(ogBiomes instanceof TAAMCBiomeGenerator))
            return new TAAMCBiomeGenerator(ogBiomes, range);
        else
            ((TAAMCBiomeGenerator) ogBiomes).append(range, append);
            return ogBiomes;
    }

    protected List<Populator> transformPop(List<Populator> ogPop) {
        return ogPop.stream()
                .map(
                        x -> {
                            if(!(x instanceof Filters.Pop))
                                return new Filters.Pop(range::test, x);
                            else return x;
                        }
                )
                .collect(Collectors.toList());
    }

    protected List<GenerationPopulator> transformGen(List<GenerationPopulator> ogGenPop) {
        return ogGenPop.stream()
                .map(
                        this::transform
                )
                .collect(Collectors.toList());
    }

    protected GenerationPopulator transform(GenerationPopulator original) {
        if(!(original instanceof Filters.genPop))
            return new Filters.genPop(range::test, original);
        else
            return original;

    }


    @Override public String getId() {
        return Plugin.ID+":basemod";
    }

    @Override public String getName() {
        return "TAAMC_BaseMod";
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TAAMCMod taamcMod = (TAAMCMod) o;
        return Objects.equal(this.range, taamcMod.range) &&
                Objects.equal(this.wg, taamcMod.wg);
    }

    @Override public int hashCode() {
        return Objects.hashCode(this.range, this.wg);
    }
}
