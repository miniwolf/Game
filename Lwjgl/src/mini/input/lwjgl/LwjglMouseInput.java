package mini.input.lwjgl;

import mini.input.MouseInput;
import mini.input.RawInputListener;
import mini.input.events.MouseButtonEvent;
import mini.input.events.MouseMotionEvent;
import mini.system.lwjgl.LwjglAbstractDisplay;
import mini.system.time.LwjglTimer;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;


public class LwjglMouseInput implements MouseInput {
    private LwjglAbstractDisplay context;
    private RawInputListener listener;
    private boolean cursorVisible = true;
    private int curX, curY, curWheel;

    public LwjglMouseInput(LwjglAbstractDisplay context) {
        this.context = context;
    }

    public void initialize() {
        if (!context.isRenderable()) {
            return;
        }

        try {
            Mouse.create();
            System.out.println("Mouse created.");

            // Recall state that was set before initialization
            Mouse.setGrabbed(!cursorVisible);
        } catch (LWJGLException ex) {
            System.err.println("Error while creating mouse " + ex.getMessage());
        }
    }

    public boolean isInitialized() {
        return Mouse.isCreated();
    }

    public int getButtonCount() {
        return Mouse.getButtonCount();
    }

    public void update() {
        if (!context.isRenderable()) {
            return;
        }

        while (Mouse.next()) {
            int btn = Mouse.getEventButton();

            int wheelDelta = Mouse.getEventDWheel();
            int xDelta = Mouse.getEventDX();
            int yDelta = Mouse.getEventDY();
            int x = Mouse.getX();
            int y = Mouse.getY();

            curWheel += wheelDelta;
            if (cursorVisible) {
                xDelta = x - curX;
                yDelta = y - curY;
                curX = x;
                curY = y;
            } else {
                x = curX + xDelta;
                y = curY + yDelta;
                curX = x;
                curY = y;
            }

            if (xDelta != 0 || yDelta != 0 || wheelDelta != 0) {
                MouseMotionEvent evt = new MouseMotionEvent(x, y, xDelta, yDelta, curWheel,
                                                            wheelDelta);
                evt.setTime(Mouse.getEventNanoseconds());
                listener.onMouseMotionEvent(evt);
            }
            if (btn != -1) {
                MouseButtonEvent evt = new MouseButtonEvent(btn,
                                                            Mouse.getEventButtonState(), x, y);
                evt.setTime(Mouse.getEventNanoseconds());
                listener.onMouseButtonEvent(evt);
            }
        }
    }

    public void destroy() {
        if (!context.isRenderable()) {
            return;
        }

        Mouse.destroy();

        System.out.println("Mouse destroyed.");
    }

    public void setCursorVisible(boolean visible) {
        cursorVisible = visible;
        if (!context.isRenderable()) {
            return;
        }

        Mouse.setGrabbed(!visible);
    }

    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    @Override
    public long getInputTimeNanos() {
        return Sys.getTime() * LwjglTimer.LWJGL_TIME_TO_NANOS;
    }
}
