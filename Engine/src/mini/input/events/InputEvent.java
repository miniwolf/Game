package mini.input.events;

/**
 * An abstract input event.
 */
public abstract class InputEvent {
    protected long time;

    protected boolean consumed = false;

    /**
     * The time when the event occurred. This is relative to
     * {@link Input#getInputTimeNanos() }.
     *
     * @return time when the event occured
     */
    public long getTime() {
        return time;
    }

    /**
     * Set the time when the event occurred.
     *
     * @param time time when the event occurred.
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Returns true if the input event has been consumed, meaning it is no longer valid
     * and should not be forwarded to input listeners.
     *
     * @return true if the input event has been consumed
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Call to mark this input event as consumed, meaning it is no longer valid
     * and should not be forwarded to input listeners.
     */
    public void setConsumed() {
        this.consumed = true;
    }
}
