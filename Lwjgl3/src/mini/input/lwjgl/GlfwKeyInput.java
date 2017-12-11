package mini.input.lwjgl;

import mini.input.KeyInput;
import mini.input.RawInputListener;
import mini.input.events.KeyInputEvent;
import mini.system.lwjgl.LwjglWindow;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

public class GlfwKeyInput implements KeyInput {

    private static final Logger logger = Logger.getLogger(GlfwKeyInput.class.getName());

    private LwjglWindow context;
    private RawInputListener listener;
    private boolean initialized;
    private GLFWKeyCallback keyCallback;
    private GLFWCharCallback charCallback;
    private Queue<KeyInputEvent> keyInputEvents = new LinkedList<KeyInputEvent>();

    public GlfwKeyInput(LwjglWindow context) {
        this.context = context;
    }

    public void initialize() {
        if (!context.isRenderable()) {
            return;
        }

        glfwSetKeyCallback(context.getWindowHandle(), keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {

                if (key < 0 || key > GLFW_KEY_LAST) {
                    return;
                }

                int jmeKey = GlfwKeyMap.toJmeKeyCode(key);

                final KeyInputEvent event = new KeyInputEvent(jmeKey, '\0', GLFW_PRESS == action,
                                                              GLFW_REPEAT == action);
                event.setTime(getInputTimeNanos());

                keyInputEvents.add(event);
            }

            @Override
            public void close() {
                super.close();
            }

            @Override
            public void callback(long args) {
                super.callback(args);
            }
        });

        glfwSetCharCallback(context.getWindowHandle(), charCallback = new GLFWCharCallback() {

            @Override
            public void invoke(long window, int codepoint) {

                final char keyChar = (char) codepoint;

                final KeyInputEvent pressed = new KeyInputEvent(KeyInput.KEY_UNKNOWN, keyChar, true,
                                                                false);
                pressed.setTime(getInputTimeNanos());

                keyInputEvents.add(pressed);

                final KeyInputEvent released = new KeyInputEvent(KeyInput.KEY_UNKNOWN, keyChar,
                                                                 false, false);
                released.setTime(getInputTimeNanos());

                keyInputEvents.add(released);
            }

            @Override
            public void close() {
                super.close();
            }

            @Override
            public void callback(long args) {
                super.callback(args);
            }
        });

        initialized = true;
        logger.fine("Keyboard created.");
    }

    public int getKeyCount() {
        // This might not be correct
        return GLFW_KEY_LAST - GLFW_KEY_SPACE;
    }

    public void update() {
        if (!context.isRenderable()) {
            return;
        }

        while (!keyInputEvents.isEmpty()) {
            listener.onKeyEvent(keyInputEvents.poll());
        }
    }

    public void destroy() {
        if (!context.isRenderable()) {
            return;
        }

        keyCallback.close();
        charCallback.close();
        logger.fine("Keyboard destroyed.");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    public long getInputTimeNanos() {
        return (long) (glfwGetTime() * 1000000000);
    }
}
