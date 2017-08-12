package mini.system;

/**
 * The <code>ContextListener> provides a means for an application
 * to receive events relating to a context.
 */
public interface SystemListener {

    /**
     * Callback to indicate the application to initialize. This method
     * is called in the GL/Rendering thread so any GL-dependent resources
     * can be initialized.
     */
    void initialize();

    /**
     * Called to notify the application that the resolution has changed.
     *
     * @param width
     * @param height
     */
    void reshape(int width, int height);

    /**
     * Callback to update the application state, and render the scene
     * to the back buffer.
     */
    void update();

    /**
     * Called when the user requests to close the application. This
     * could happen when he clicks the X button on the window, presses
     * the Alt-F4 combination, attempts to shutdown the process from
     * the task manager, or presses ESC.
     *
     * @param esc If true, the user pressed ESC to close the application.
     */
    void requestClose(boolean esc);

    /**
     * Called when an error has occured. This is typically
     * invoked when an uncought exception is thrown in the render thread.
     *
     * @param errorMsg The error message, if any, or null.
     * @param t        Throwable object, or null.
     */
    void handleError(String errorMsg, Throwable t);

    /**
     * Callback to indicate that the context has been destroyed (either
     * by the user or requested by the application itself). Typically
     * cleanup of native resources should happen here. This method is called
     * in the GL/Rendering thread.
     */
    void destroy();
}
