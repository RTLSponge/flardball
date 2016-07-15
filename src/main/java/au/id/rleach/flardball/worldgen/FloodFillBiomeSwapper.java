package au.id.rleach.flardball.worldgen;

import au.id.rleach.flardball.Plugin;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaFiller;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaMapper;
import org.spongepowered.api.world.extent.worker.procedure.BiomeAreaMerger;
import org.spongepowered.api.world.gen.BiomeGenerator;

import java.awt.Color;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiFunction;

public class FloodFillBiomeSwapper implements BiomeGenerator {

    private final Path dir;
    Map<BiomeType,BiomeType> mapping;
    BiomeGenerator generator;

    BiomeTileStore tiles;

    public FloodFillBiomeSwapper(BiomeGenerator generator, Path biomeMapDir) {
        this.generator = generator;
        this.mapping = Maps.newLinkedHashMap();
        mapping.put(BiomeTypes.RIVER, BiomeTypes.EXTREME_HILLS_PLUS_MOUNTAINS);
        mapping.put(BiomeTypes.DEEP_OCEAN, BiomeTypes.MUSHROOM_ISLAND);
        mapping.put(BiomeTypes.PLAINS, BiomeTypes.MUSHROOM_ISLAND);
        this.dir = biomeMapDir;
        tiles = new BiomeTileStore(biomeMapDir);

    }

    @Override public void generateBiomes(MutableBiomeArea buffer) {
        generator.generateBiomes(buffer);
        BiomeAreaMapper swapper = (area,x,z)->get(area.getBiome(x,z));
        buffer.getBiomeWorker().map(swapper);
        buffer.getBiomeWorker().iterate(
                (a,x,z) -> {
                    BiomeGenBase biome = (BiomeGenBase) a.getBiome(x, z);
                    BiomeTile image = tiles.getImageForXZ(new Vector2i(x, z));
                    Color color = new Color(biome.color);
                    image.image.setRGB(image.x(x), image.z(z), color.getRGB());
                }
        );
    }

    private BiomeType get(BiomeType biome) {
        final BiomeType out = mapping.get(biome);
        return out != null ? out : biome;
    }
}
