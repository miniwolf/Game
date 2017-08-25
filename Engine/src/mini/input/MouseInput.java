package mini.input;

/**
 * A specific API for interfacing with the mouse.
 */
public interface MouseInput extends Input {

    /**
     * Mouse X axis.
     */
    int AXIS_X = 0;

    /**
     * Mouse Y axis.
     */
    int AXIS_Y = 1;

    /**
     * Mouse wheel axis.
     */
    int AXIS_WHEEL = 2;

    /**
     * Left mouse button.
     */
    int BUTTON_LEFT = 0;

    /**
     * Right mouse button.
     */
    int BUTTON_RIGHT = 1;

    /**
     * Middle mouse button.
     */
    int BUTTON_MIDDLE = 2;

    /**
     * Set whether the mouse cursor should be visible or not.
     *
     * @param visible Whether the mouse cursor should be visible or not.
     */
    void setCursorVisible(boolean visible);

    /**
     * Returns the number of buttons the mouse has. Typically 3 for most mice.
     *
     * @return the number of buttons the mouse has.
     */
    int getButtonCount();
}
