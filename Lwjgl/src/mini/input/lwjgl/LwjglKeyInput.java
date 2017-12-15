package mini.input.lwjgl;

import mini.input.Input;
import mini.input.KeyboardKey;
import mini.input.RawInputListener;
import mini.input.events.KeyInputEvent;
import mini.system.lwjgl.LwjglAbstractDisplay;
import mini.system.lwjgl.LwjglTimer;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

public class LwjglKeyInput implements Input {
    private LwjglAbstractDisplay context;
    private RawInputListener listener;

    public LwjglKeyInput(LwjglAbstractDisplay context) {
        this.context = context;
    }

    public void initialize() {
        if (!context.isRenderable()) {
            return;
        }

        try {
            Keyboard.create();
            Keyboard.enableRepeatEvents(true);
            System.out.println("Keyboard created.");
        } catch (LWJGLException ex) {
            System.err.println("Error while creating keyboard." + ex.getMessage());
        }
    }

    public int getKeyCount() {
        return Keyboard.KEYBOARD_SIZE;
    }

    public void update() {
        if (!context.isRenderable()) {
            return;
        }

        Keyboard.poll();
        while (Keyboard.next()) {
            KeyboardKey keyCode = KeyboardKey.getValues().get(Keyboard.getEventKey());
            boolean pressed = Keyboard.getEventKeyState();
            boolean down = Keyboard.isRepeatEvent();
            long time = Keyboard.getEventNanoseconds();
            KeyInputEvent evt = new KeyInputEvent(keyCode, pressed, down);
            evt.setTime(time);
            listener.onKeyEvent(evt);
        }
    }

    public void destroy() {
        if (!context.isRenderable()) {
            return;
        }

        Keyboard.destroy();
        System.out.println("Keyboard destroyed.");
    }

    public boolean isInitialized() {
        return Keyboard.isCreated();
    }

    public void setInputListener(RawInputListener listener) {
        this.listener = listener;
    }

    @Override
    public long getInputTimeNanos() {
        return Sys.getTime() * LwjglTimer.LWJGL_TIME_TO_NANOS;
    }
}
