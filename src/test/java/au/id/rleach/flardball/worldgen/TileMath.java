package au.id.rleach.flardball.worldgen;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.math.IntMath;
import net.minecraft.util.BlockPos;
import org.junit.Assert;
import org.junit.Test;

import java.math.RoundingMode;

public class TileMath {

    @Test
    public void testMath() throws Exception {
        Vector2i zero = new Vector2i(0, 0);
        Vector2i onex = new Vector2i(1, 0);
        Vector2i onez = new Vector2i(0, 1);
        int tilesize = 512;
        Vector2i x512 = new Vector2i(512, 0);
        Vector2i z512 = new Vector2i(0, 512);
        //sanity
        Assert.assertEquals(x512.div(tilesize), onex);
        Assert.assertEquals(z512.div(tilesize), onez);

        //rounding
        Vector2i halfway = x512.add(256, 0);
        Assert.assertEquals(halfway.div(tilesize), onex);
        Vector2i halfway2 = x512.add(257, 0);
        Assert.assertEquals(halfway.div(tilesize), onex);

        int ineg1 = -1;
        Assert.assertEquals(ineg1, GenericMath.floor(-0.5));

        Assert.assertEquals(512, 1<<9);
        Assert.assertEquals(512, IntMath.pow(2, 9));
        Assert.assertEquals(9, IntMath.log2(512, RoundingMode.UNNECESSARY));

    }
}
