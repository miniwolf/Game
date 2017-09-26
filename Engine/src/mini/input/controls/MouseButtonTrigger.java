package mini.input.controls;

/**
 * A <code>MouseButtonTrigger</code> is used as a mapping to receive events
 * from mouse buttons. It is generally expected for a mouse to have at least
 * a left and right mouse button, but some mice may have a lot more buttons
 * than that.
 */
public class MouseButtonTrigger implements Trigger {

    private final int mouseButton;

    /**
     * Create a new <code>MouseButtonTrigger</code> to receive mouse button events.
     *
     * @param mouseButton Mouse button index. See BUTTON_*** constants in
     *                    {@link MouseInput}.
     */
    public MouseButtonTrigger(int mouseButton) {
        if (mouseButton < 0) {
            throw new IllegalArgumentException("Mouse Button cannot be negative");
        }

        this.mouseButton = mouseButton;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public String getName() {
        return "Mouse Button " + mouseButton;
    }

    public static int mouseButtonHash(int mouseButton) {
        assert mouseButton >= 0 && mouseButton <= 255;
        return 256 | (mouseButton & 0xff);
    }

    public int triggerHashCode() {
        return mouseButtonHash(mouseButton);
    }
}
