package mini.system;

import mini.system.time.NanoTimer;
import org.junit.Assert;

public class NanoTimerTest {
    public static final float DELTA = 0.00001f;
    private NanoTimer nanoTimer;

    @org.junit.Before
    public void before() {
        nanoTimer = new NanoTimer();
    }

    @org.junit.Test
    public void getTimePerFrame() {
        Assert.assertEquals("Starts by being zero", 0, nanoTimer.getTimePerFrame(), DELTA);
        nanoTimer.update();
        Assert.assertNotEquals("time per frame should be updated", 0, nanoTimer.getTimePerFrame(),
                               DELTA);
    }

    @org.junit.Test
    public void getFrameRate() {
        Assert.assertEquals("Starts by being zero", 0, nanoTimer.getFrameRate(), DELTA);
        nanoTimer.update();
        Assert.assertNotEquals("frame rate should be updated", 0, nanoTimer.getFrameRate(), DELTA);
    }
}
