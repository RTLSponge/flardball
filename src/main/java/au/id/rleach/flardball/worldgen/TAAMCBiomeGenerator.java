package au.id.rleach.flardball.worldgen;

import com.google.common.collect.Maps;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;

import java.util.Map;

public class TAAMCBiomeGenerator implements BiomeGenerator {

    Map<Range, BiomeGenerator> biomeMap = Maps.newLinkedHashMap();

    public TAAMCBiomeGenerator(BiomeGenerator ogBiomes, Range r) {

        append(r, ogBiomes);

    }

    void append(Range r, BiomeGenerator biomeGenerator){
        biomeMap.put(r, biomeGenerator);
    }

    @Override public void generateBiomes(MutableBiomeArea buffer) {
        for (Map.Entry<Range, BiomeGenerator> entry : biomeMap.entrySet()) {
            Range range = entry.getKey();
            BiomeGenerator biomeGenerator = entry.getValue();

            if (range.test(buffer)) {
                biomeGenerator.generateBiomes(buffer);
            }
        }
    }
}
