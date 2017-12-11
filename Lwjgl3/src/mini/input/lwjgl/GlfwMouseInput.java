package mini.input.lwjgl;

import mini.cursors.plugins.ApplicationCursor;
import mini.input.MouseInput;
import mini.input.RawInputListener;
import mini.input.events.MouseButtonEvent;
import mini.input.events.MouseMotionEvent;
import mini.system.lwjgl.LwjglWindow;
import mini.utils.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwCreateCursor;
import static org.lwjgl.glfw.GLFW.glfwDestroyCursor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

/**
 * Captures mouse input using GLFW callbacks. It then temporarily stores these in event queues which are processed in the
 * {@link #update()} method. Due to some of the GLFW button id's there is a conversion method in this class which will
 * convert the GLFW left, middle and right mouse button to JME3 left, middle and right button codes.
 */
public class GlfwMouseInput implements MouseInput {

    private static final Logger logger = Logger.getLogger(GlfwMouseInput.class.getName());

    private static final int WHEEL_SCALE = 120;

    private LwjglWindow context;
    private RawInputListener listener;
    private boolean cursorVisible = true;
    private int mouseX;
    private int mouseY;
    private int mouseWheel;
    private boolean initialized;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWScrollCallback scrollCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private Queue<MouseMotionEvent> mouseMotionEvents = new LinkedList<MouseMotionEvent>();
    private Queue<MouseButtonEvent> mouseButtonEvents = new LinkedList<MouseButtonEvent>();

    private Map<ApplicationCursor, Long> jmeToGlfwCursorMap = new HashMap<>();

    public GlfwMouseInput(LwjglWindow context) {
        this.context = context;
    }

    private void onCursorPos(long window, double xpos, double ypos) {
        int xDelta;
        int yDelta;
        int x = (int) Math.round(xpos);
        int y = context.getSettings().getHeight() - (int) Math.round(ypos);

        if (mouseX == 0) {
            mouseX = x;
        }

        if (mouseY == 0) {
            mouseY = y;
        }

        xDelta = x - mouseX;
        yDelta = y - mouseY;
        mouseX = x;
        mouseY = y;

        if (xDelta != 0 || yDelta != 0) {
            final MouseMotionEvent mouseMotionEvent = new MouseMotionEvent(x, y, xDelta, yDelta,
                                                                           mouseWheel, 0);
            mouseMotionEvent.setTime(getInputTimeNanos());
            mouseMotionEvents.add(mouseMotionEvent);
        }
    }

    private void onWheelScroll(long window, double xOffset, double yOffset) {
        mouseWheel += yOffset;
        final MouseMotionEvent mouseMotionEvent = new MouseMotionEvent(mouseX, mouseY, 0, 0,
                                                                       mouseWheel,
                                                                       (int) Math.round(yOffset));
        mouseMotionEvent.setTime(getInputTimeNanos());
        mouseMotionEvents.add(mouseMotionEvent);
    }

    private void onMouseButton(final long window, final int button, final int action,
                               final int mods) {
        final MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(convertButton(button),
                                                                       action == GLFW_PRESS, mouseX,
                                                                       mouseY);
        mouseButtonEvent.setTime(getInputTimeNanos());
        mouseButtonEvents.add(mouseButtonEvent);
    }

    public void initialize() {
        glfwSetCursorPosCallback(context.getWindowHandle(),
                                 cursorPosCallback = new GLFWCursorPosCallback() {
                                     @Override
                                     public void invoke(long window, double xpos, double ypos) {
                                         onCursorPos(window, xpos, ypos);
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

        glfwSetScrollCallback(context.getWindowHandle(), scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(final long window, final double xOffset, final double yOffset) {
                onWheelScroll(window, xOffset, yOffset * WHEEL_SCALE);
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

        glfwSetMouseButtonCallback(context.getWindowHandle(),
                                   mouseButtonCallback = new GLFWMouseButtonCallback() {
                                       @Override
                                       public void invoke(final long window, final int button,
                                                          final int action, final int mods) {
                                           onMouseButton(window, button, action, mods);
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

        setCursorVisible(cursorVisible);
        logger.fine("Mouse created.");
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getButtonCount() {
        return GLFW_MOUSE_BUTTON_LAST + 1;
    }

    public void update() {
        while (!mouseMotionEvents.isEmpty()) {
            listener.onMouseMotionEvent(mouseMotionEvents.poll());
        }

        while (!mouseButtonEvents.isEmpty()) {
            listener.onMouseButtonEvent(mouseButtonEvents.poll());
        }
    }

    public void destroy() {
        if (!context.isRenderable()) {
            return;
        }

        cursorPosCallback.close();
        scrollCallback.close();
        mouseButtonCallback.close();

        for (long glfwCursor : jmeToGlfwCursorMap.values()) {
            glfwDestroyCursor(glfwCursor);
        }

        logger.fine("Mouse destroyed.");
    }

    public void setCursorVisible(boolean visible) {
        cursorVisible = visible;

        if (!context.isRenderable()) {
            return;
        }

        if (cursorVisible) {
            glfwSetInputMode(context.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(context.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
    }

    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    public long getInputTimeNanos() {
        return (long) (glfwGetTime() * 1000000000);
    }

    private ByteBuffer transformCursorImage(IntBuffer imageData, int w, int h) {
        ByteBuffer buf = BufferUtils.createByteBuffer(imageData.capacity() * 4);

        // Transform image: ARGB -> RGBA, vertical flip
        for (int y = h - 1; y >= 0; --y) {
            for (int x = 0; x < w; ++x) {
                int pixel = imageData.get(y * w + x);
                buf.put((byte) ((pixel >> 16) & 0xFF));  // red
                buf.put((byte) ((pixel >> 8) & 0xFF));  // green
                buf.put((byte) (pixel & 0xFF));  // blue
                buf.put((byte) ((pixel >> 24) & 0xFF));  // alpha
            }
        }

        buf.flip();
        return buf;
    }

    private long createGlfwCursor(ApplicationCursor jmeCursor) {
        // TODO: currently animated cursors are not supported
        IntBuffer imageData = jmeCursor.getImagesData();
        ByteBuffer buf = transformCursorImage(imageData, jmeCursor.getWidth(),
                                              jmeCursor.getHeight());

        GLFWImage glfwImage = new GLFWImage(BufferUtils.createByteBuffer(GLFWImage.SIZEOF));
        glfwImage.set(jmeCursor.getWidth(), jmeCursor.getHeight(), buf);

        int hotspotX = jmeCursor.getXHotSpot();
        int hotspotY = jmeCursor.getHeight() - jmeCursor.getYHotSpot();
        return glfwCreateCursor(glfwImage, hotspotX, hotspotY);
    }

    public void setNativeCursor(ApplicationCursor jmeCursor) {
        if (jmeCursor != null) {
            Long glfwCursor = jmeToGlfwCursorMap.get(jmeCursor);

            if (glfwCursor == null) {
                glfwCursor = createGlfwCursor(jmeCursor);
                jmeToGlfwCursorMap.put(jmeCursor, glfwCursor);
            }

            glfwSetCursor(context.getWindowHandle(), glfwCursor);
        } else {
            glfwSetCursor(context.getWindowHandle(), MemoryUtil.NULL);
        }
    }

    /**
     * Simply converts the GLFW button code to a JME button code. If there is no
     * match it just returns the GLFW button code. Bear in mind GLFW supports 8
     * different mouse buttons.
     *
     * @param glfwButton the raw GLFW button index.
     * @return the mapped {@link MouseInput} button id.
     */
    private int convertButton(final int glfwButton) {
        switch (glfwButton) {
            case GLFW_MOUSE_BUTTON_LEFT:
                return MouseInput.BUTTON_LEFT;
            case GLFW_MOUSE_BUTTON_MIDDLE:
                return MouseInput.BUTTON_MIDDLE;
            case GLFW_MOUSE_BUTTON_RIGHT:
                return MouseInput.BUTTON_RIGHT;
            default:
                return glfwButton;
        }
    }
}
