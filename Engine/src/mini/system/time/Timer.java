package mini.system.time;

/**
 * <code>Timer</code> defines the interface for a high resolution timer. It is greated from
 * getTimer("display system")
 */
public interface Timer {
    /**
     * <code>update</code> recalculates the frame rate based on the previous call to update. It is
     * assumed that update is called each frame.
     */
    void update();

    /**
     * Returns the time, in seconds, between the last call and the current one. (Delta time)
     *
     * @return Time between this call and the last one.
     */
    float getTimePerFrame();

    /**
     * Reset the timer to 0. Clear any tpf history.
     */
    void reset();

    /**
     * @return the time in seconds. The timer starts at 0.0 seconds.
     */
    default float getTimeInSeconds() {
        return getTime() / getResolution();
    }

    /**
     * @return the number of ticks per second
     */
    float getResolution();

    /**
     * Returns the current time in ticks. A tick is an arbitrary measure of time defined by the
     * timer implementation. The number of ticks per second is given by <code>getResolution()</code>
     * <p>
     * The timer starts at 0 ticks.
     *
     * @return a long value representing the current time
     */
    long getTime();
}
