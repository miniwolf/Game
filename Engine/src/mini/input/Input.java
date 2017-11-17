package mini.input;

/**
 * Abstract interface for an input device.
 *
 * @see MouseInput
 * @see KeyInput
 * @see JoyInput
 */
public interface Input {

    /**
     * Initializes the native side to listen into events from the device.
     */
    void initialize();

    /**
     * Queries the device for input. All events should be sent to the
     * RawInputListener set with setInputListener.
     *
     * @see #setInputListener(mini.input.RawInputListener)
     */
    void update();

    /**
     * Ceases listening to events from the device.
     */
    void destroy();

    /**
     * @return True if the device has been initialized and not destroyed.
     * @see #initialize()
     * @see #destroy()
     */
    boolean isInitialized();

    /**
     * Sets the input listener to receive events from this device. The
     * appropriate events should be dispatched through the callbacks
     * in RawInputListener.
     *
     * @param listener
     */
    void setInputListener(RawInputListener listener);

    /**
     * @return The current absolut time as nanoseconds. This time is expected to be relative to the
     * time given in InputEvents time property.
     */
    long getInputTimeNanos();
}
