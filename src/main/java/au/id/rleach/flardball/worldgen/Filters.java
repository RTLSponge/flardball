package au.id.rleach.flardball.worldgen;


import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;

import java.util.Random;
import java.util.function.Predicate;

public class Filters {

    public abstract static class FilteringGen<T, P> {
        Predicate<T> test;
        P pop;

        public FilteringGen(Predicate<T> test, P pop) {
            this.test = test;
            this.pop = pop;
        }
    }

    public static class Pop extends FilteringGen<Chunk, Populator> implements Populator {
        public Pop(Predicate<Chunk> test, Populator pop) {
            super(test, pop);
        }

        @Override public PopulatorType getType() {
            return pop.getType();
        }

        @Override public void populate(Chunk chunk, Random random) {
            if(test.test(chunk))
                pop.populate(chunk, random);
        }
    }

    public static class genPop extends FilteringGen<MutableBlockVolume, GenerationPopulator> implements GenerationPopulator {

        public genPop(Predicate<MutableBlockVolume> test, GenerationPopulator pop) {
            super(test, pop);
        }

        @Override public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeArea biomes) {
            if(test.test(buffer)) pop.populate(world, buffer, biomes);
        }
    }
}