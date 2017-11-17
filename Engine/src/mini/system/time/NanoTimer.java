package mini.system.time;

/**
 * <code>NanoTimer</code> is a System.nanoTime implementation of <code>Timer</code>.
 * This is primarily useful for headless application running on a server.
 */
public class NanoTimer implements Timer {
    private static final float TIMER_RESOLUTION = 1000000000L;

    private long startTime;
    private long previousTime;
    private float tpf;
    private float fps;

    public NanoTimer() {
        startTime = System.nanoTime();
    }

    public float getTimePerFrame() {
        return tpf;
    }

    public float getFrameRate() {
        return fps;
    }

    private long getTime() {
        return System.nanoTime() - startTime;
    }

    public void update() {
        tpf = (getTime() - previousTime) * (1.0f / TIMER_RESOLUTION);
        fps = 1.0f / tpf;
        previousTime = getTime();
    }
}
