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
}
