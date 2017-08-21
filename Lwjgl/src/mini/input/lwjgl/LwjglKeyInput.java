package mini.input.lwjgl;

import mini.input.KeyInput;
import mini.input.RawInputListener;
import mini.input.events.KeyInputEvent;
import mini.system.lwjgl.LwjglAbstractDisplay;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;

public class LwjglKeyInput implements KeyInput {
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
            int keyCode = Keyboard.getEventKey();
            char keyChar = Keyboard.getEventCharacter();
            boolean pressed = Keyboard.getEventKeyState();
            boolean down = Keyboard.isRepeatEvent();
            long time = Keyboard.getEventNanoseconds();
            KeyInputEvent evt = new KeyInputEvent(keyCode, keyChar, pressed, down);
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
}
