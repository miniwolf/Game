package mini.input.events;

/**
 * Mouse button press/release event.
 */
public class MouseButtonEvent extends InputEvent {
    private int x;
    private int y;
    private int btnIndex;
    private boolean pressed;

    public MouseButtonEvent(int btnIndex, boolean pressed, int x, int y) {
        this.btnIndex = btnIndex;
        this.pressed = pressed;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the mouse button index.
     * <p>
     * See constants in {@link MouseInput}.
     *
     * @return the mouse button index.
     */
    public int getButtonIndex() {
        return btnIndex;
    }

    /**
     * Returns true if the mouse button was pressed, false if it was released.
     *
     * @return true if the mouse button was pressed, false if it was released.
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Returns true if the mouse button was released, false if it was pressed.
     *
     * @return true if the mouse button was released, false if it was pressed.
     */
    public boolean isReleased() {
        return !pressed;
    }

    /**
     * The X coordinate of the mouse when the event was generated.
     *
     * @return X coordinate of the mouse when the event was generated.
     */
    public int getX() {
        return x;
    }

    /**
     * The Y coordinate of the mouse when the event was generated.
     *
     * @return Y coordinate of the mouse when the event was generated.
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        String str = "MouseButton(BTN=" + btnIndex;
        if (pressed) {
            return str + ", PRESSED)";
        } else {
            return str + ", RELEASED)";
        }
    }
}
