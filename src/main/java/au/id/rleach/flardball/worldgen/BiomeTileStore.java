package au.id.rleach.flardball.worldgen;

import au.id.rleach.flardball.Plugin;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalListeners;

import com.google.common.math.IntMath;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

public class BiomeTileStore {

    private final Path dir;

    private final RemovalListener<Vector2i, BiomeTile> onRemove = notification -> {
        if(!notification.getCause().equals(RemovalCause.EXPLICIT))
            notification.getValue().flushImage();
    };

    private CacheLoader<Vector2i, BiomeTile> load = new CacheLoader<Vector2i, BiomeTile>() {
        @Override public BiomeTile load(Vector2i key) throws Exception {
            return BiomeTileStore.this.loadImage(key);
        }
    };

    final LoadingCache<Vector2i, BiomeTile> loadingCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .removalListener(
                    RemovalListeners.asynchronous(
                            onRemove,
                            Sponge.getScheduler().createAsyncExecutor(Sponge.getPluginManager().getPlugin(Plugin.ID).get())
                    )
            )
            .build(load);

    private static final int BITSHIFT = 9; //512
    private static final int TILE_SIZE = IntMath.pow(2, BITSHIFT);
    private final Supplier<BufferedImage> newImage = () -> new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);


    public BiomeTileStore(Path dir) {
        if(!Files.exists(dir))
            try {
                Files.createDirectories(dir);

            } catch (IOException e) {
                //We can't fix this, the server owner needs to fix their permissions
                throw new RuntimeException(e);
            }
        this.dir = dir;
        PluginContainer plugin = Sponge.getPluginManager().getPlugin(Plugin.ID).get();
        Sponge.getEventManager().registerListeners(plugin, this);
        Sponge.getScheduler().createTaskBuilder()
                .name("BiomeTileCache Task")
                .intervalTicks(50)
                .execute(
                        this.loadingCache::cleanUp
                ).submit(plugin);
    }

    BiomeTile getImageForXZ(Vector2i vector2i){
        final Vector2i tileXZ = new Vector2i(vector2i.getX() >> BITSHIFT, vector2i.getY() >> BITSHIFT);
        try {
            return loadingCache.get(tileXZ);
        } catch (ExecutionException e) {
            //Admin problem.
            throw new RuntimeException(e);
        }
    }

    private BiomeTile loadImage(Vector2i tileXZ) throws IOException {
        BufferedImage image;
        final Path file = BiomeTile.toFileName(dir, tileXZ);
        if(Files.exists(file)) {
            image = ImageIO.read(file.toFile());
        } else {
            image = newImage.get();
        }
        return new BiomeTile(image, tileXZ, dir, TILE_SIZE);
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent stop){
        for (Map.Entry<Vector2i, BiomeTile> entry:loadingCache.asMap().entrySet()){
            entry.getValue().flushImage();
        }
        loadingCache.invalidateAll();
    }
}
