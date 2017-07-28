package mini.utils;

import mini.input.Action;
import mini.input.Keyboard;
import mini.input.KeyboardKey;
import mini.input.Mouse;
import mini.renderEngine.opengl.GLDebug;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_DEPTH_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT;

public class DisplayManager {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private static final int FPS_CAP = 100;
    private static final String TITLE = "Socuwan Scene";

    private static float delta;
    private static long lastFrameTime;
    private static long window;
    private static Callback debugProc;

    private long variableYieldTime, lastTime;

    public static DisplayManager createDisplay() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_DEPTH_BITS, 24);
        glfwWindowHint(GLFW_SAMPLES, 4);

        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, 0L, 0L);
        if (window == 0L) {
            System.err.println("Couldn't create display!");
            System.exit(-1);
        }

        setupCallbackFunctions();

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        return new DisplayManager();
    }

    private static void setupCallbackFunctions() {
        glfwSetMouseButtonCallback(window, (window, button, action, mods) ->
                Mouse.mouseAction(button, Action.getValues()[action], mods));

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> Mouse.updatePosition(xpos, ypos));

        glfwSetScrollCallback(window, (window, xoffset, yoffset) ->
                Mouse.updateScrollWheel(xoffset, yoffset));

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
                Keyboard.updateKey(KeyboardKey.getValues().get(key), Action.values()[action], mods));
    }

    private DisplayManager() {
        GL.createCapabilities();

        debugProc = GLUtil.setupDebugMessageCallback();
        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL13.GL_MULTISAMPLE);
        GL11.glViewport(0, 0, WIDTH, HEIGHT);

        lastFrameTime = getCurrentTime();
    }

    public static boolean isCloseRequested() {
        return glfwWindowShouldClose(window);
    }

    public static long getDisplay() {
        return window;
    }

    /**
     * Synchronizes the update to match the fps
     * Swap the color buffers to render.
     * Next we pull the input events and update delta for external use.
     */
    public void update() {
        clearInputActions();
        sync(FPS_CAP);
        glfwSwapBuffers(window); // swap the color buffers
        // Poll for window events. The key callback above will only be invoked during this call.
        glfwPollEvents();
        long currentFrameTime = getCurrentTime();
        delta = (currentFrameTime - lastFrameTime) / 1000f;
        lastFrameTime = currentFrameTime;
    }

    private void clearInputActions() {
        Mouse.clearActions();
        Keyboard.clearActions();
    }

    /**
     * An accurate sync method that adapts automatically
     * to the system it runs on to provide reliable results.
     *
     * @param fps The desired frame rate, in frames per second
     */
    private void sync(int fps) {
        if (fps <= 0) {
            return;
        }

        long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
        // yieldTime + remainder micro & nano seconds if smaller than sleepTime
        long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000));
        long overSleep = 0; // time the sync goes over by

        try {
            while (true) {
                long t = System.nanoTime() - lastTime;

                if (t < sleepTime - yieldTime) {
                    Thread.sleep(1);
                } else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                } else {
                    overSleep = t - sleepTime;
                    break; // exit while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);

            // auto tune the time sync should yield
            if (overSleep > variableYieldTime) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime);
            } else if (overSleep < variableYieldTime - 200 * 1000) {
                // decrease by 2 microseconds
                variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
            }
        }
    }

    public float getFrameTime() {
        return delta;
    }

    public void closeDisplay() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
        debugProc.free();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

}
