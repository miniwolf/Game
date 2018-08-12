package mini.environment.generation;

/**
 * An interface listener that will be notified of the progress of an asynchronous generation job.
 *
 * @param <T> The type of object generated.
 */
public interface JobProgressListener<T> {
    /**
     * Called when the process starts.
     */
    void start();

    /**
     * Can be called when a step of the process has been completed with a relevant message.
     *
     * @param message the message stating of the particular step completion.
     */
    void step(String message);

    /**
     * Called when the process has made some progress.
     *
     * @param value a value from 0 to 1 representing the percentage of completion of the process.
     */
    void progress(double value);

    /**
     * Called when the process is done.
     *
     * @param result the object generated by the process.
     */
    void done(T result);
}