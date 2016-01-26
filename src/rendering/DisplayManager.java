package rendering;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;

/**
 * @author miniwolf
 */
public class DisplayManager {
    private static int WIDTH = 1280;
    private static int HEIGHT = 760;
    private static int FPS = 120;

    private static long lastFrame;
    private static float delta;

    public static void create() {
        ContextAttribs attribs = new ContextAttribs(3,2)
                .withForwardCompatible(true)
                .withProfileCore(true);
        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.create(new PixelFormat(), attribs);
            Display.setTitle("MiniGame v0.7");
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        GL11.glViewport(0, 0, WIDTH, HEIGHT);
        lastFrame = getCurrentTime();
    }

    public static void update() {
        Display.sync(FPS);
        Display.update();
        long currentFrameTime = getCurrentTime();
        delta = (currentFrameTime - lastFrame) * 0.001f;
        lastFrame = currentFrameTime;
    }

    public static float getFrameTimeSeconds()  {
        return delta;
    }

    public static void close() {
        Display.destroy();
    }

    private static long getCurrentTime() {
        return (long) (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
