package au.id.rleach.flardball.worldgen;

import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;

import java.util.Map;

public class TAAMCBiomeGenerator implements BiomeGenerator {

    BiomeGenerator baseGen;
    Map<Range, BiomeGenerator> biomeMap;

    public TAAMCBiomeGenerator(BiomeGenerator ogBiomes) {
        baseGen = ogBiomes;
    }

    void append(Range r, BiomeGenerator biomeGenerator){
        biomeMap.put(r, biomeGenerator);
    }

    @Override public void generateBiomes(MutableBiomeArea buffer) {
        boolean modified;
        modified = false;
        for (Map.Entry<Range, BiomeGenerator> entry : biomeMap.entrySet()) {
            Range range = entry.getKey();
            BiomeGenerator biomeGenerator = entry.getValue();

            if (range.test(buffer)) {
                modified = true;
                biomeGenerator.generateBiomes(buffer);
                break;
            }
        }
        if (!modified) {
            baseGen.generateBiomes(buffer);
        }
    }
}
