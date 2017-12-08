package mini.system.time;

import org.lwjgl.Sys;

public class LwjglTimer implements Timer {
    private static final long LWJGL_TIMER_RES = Sys.getTimerResolution();
    public static final long LWJGL_TIME_TO_NANOS = (1000000000 / LWJGL_TIMER_RES);
    private static final float INV_LWJGL_TINER_RES = (1f / LWJGL_TIMER_RES);
    private long oldTime;
    private long startTime;
    private float lastTPF;
    private float lastFPS;

    public LwjglTimer() {
        startTime = Sys.getTime();
        oldTime = getTime();
    }

    private long getTime() {
        return Sys.getTime() - startTime;
    }

    @Override
    public void update() {
        long currentTime = getTime();
        lastTPF = (currentTime - oldTime) * INV_LWJGL_TINER_RES;
        lastFPS = 1.0f / lastTPF;
    }

    @Override
    public float getTimePerFrame() {
        return lastTPF;
    }

    @Override
    public void reset() {
        startTime = Sys.getTime();
        oldTime = getTime();
    }
}
