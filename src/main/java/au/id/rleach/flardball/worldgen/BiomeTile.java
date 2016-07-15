package au.id.rleach.flardball.worldgen;

import com.flowpowered.math.vector.Vector2i;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class BiomeTile {
    final Path filename;
    final BufferedImage image;
    final Vector2i tileXZ;
    final int TILESIZE;

    public BiomeTile(BufferedImage image, Vector2i tileXZ, Path dir, int tilesize) {
        this.TILESIZE = tilesize;
        this.filename = toFileName(dir, tileXZ);
        this.image = image;
        this.tileXZ = tileXZ;
    }

    void flushImage() {
        try {
            ImageIO.write(image, "png", filename.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Path toFileName(Path dir, Vector2i tileXZ){
        final String name = "TileXZ_"+tileXZ.getX()+"_"+tileXZ.getY();
        final String filename = name + ".png";
        final Path file = dir.resolve(filename);
        return file;
    }

    public int x(int x) {
        int temp = x - tileXZ.getX() * TILESIZE;
        if(temp < 0)
            return TILESIZE + temp;
        else
            return temp;
    }

    public int z(int z) {
        int temp = z - tileXZ.getY()*TILESIZE;
        if(temp < 0)
            return TILESIZE + temp;
        else
            return temp;
    }
}
