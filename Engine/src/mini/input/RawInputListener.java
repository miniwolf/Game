package mini.input;

import mini.input.events.KeyInputEvent;
import mini.input.events.MouseButtonEvent;
import mini.input.events.MouseMotionEvent;

/**
 * An interface used for receiving raw input from devices.
 */
public interface RawInputListener {

    /**
     * Called before a batch of input will be sent to this
     * <code>RawInputListener</code>.
     */
    void beginInput();

    /**
     * Called after a batch of input was sent to this
     * <code>RawInputListener</code>.
     * <p>
     * The listener should set the {@link InputEvent#setConsumed() consumed flag}
     * on any events that have been consumed either at this call or previous calls.
     */
    void endInput();

    /**
     * Invoked on mouse movement/motion events.
     *
     * @param evt
     */
    void onMouseMotionEvent(MouseMotionEvent evt);

    /**
     * Invoked on mouse button events.
     *
     * @param evt
     */
    void onMouseButtonEvent(MouseButtonEvent evt);

    /**
     * Invoked on keyboard key press or release events.
     *
     * @param evt
     */
    void onKeyEvent(KeyInputEvent evt);

}
