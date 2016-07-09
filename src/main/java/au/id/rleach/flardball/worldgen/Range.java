package au.id.rleach.flardball.worldgen;

import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;

public class Range {


    int minDistance;
    int maxDistance;

    public Range(int minDistance, int maxDistance) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    //TODO: double check these checks for offsets
    public boolean test(Chunk chunk) {
        float dist = chunk.getBlockMin().distance(0, 0, 0);
        return dist > minDistance && dist <= maxDistance;
    }

    public boolean test(MutableBlockVolume buffer) {
        float dist = buffer.getBlockMin().distance(0, 0, 0);
        return dist > minDistance && dist <= maxDistance;
    }

    public boolean test(MutableBiomeArea buffer) {
        float dist = buffer.getBiomeMin().distance(0, 0);
        return dist > minDistance && dist <= maxDistance;
    }
}
