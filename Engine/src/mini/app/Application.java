package mini.app;

import mini.input.InputManager;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.system.ApplicationContext;

/**
 * The <code>Application</code> interface represents the minimum exposed
 * capabilities of a concrete application.
 */
public interface Application {
    /**
     * @return the {@link InputManager input manager}.
     */
    InputManager getInputManager();

    /**
     * @return the {@link RenderManager render manager}
     */
    RenderManager getRenderManager();

    /**
     * @return The {@link Renderer renderer} for the application
     */
    Renderer getRenderer();

    /**
     * @return The {@link ApplicationContext display context} for the application
     */
    ApplicationContext getContext();

    /**
     * @return The main {@link Camera camera} for the application
     */
    Camera getCamera();

    /**
     * Starts the application.
     */
    void start();

    /**
     * Starts the application. Creating a rendering context.
     */
    void start(ApplicationContext.Type contextType, boolean waitFor);

    /**
     * Restarts the context, applying any changed settings.
     * <p>
     * Changes to the {@link AppSettings} of this Application are not
     * applied immediately; calling this method forces the context
     * to restart, applying the new settings.
     */
    void restart();

    /**
     * Requests the context to close, shutting down the main loop
     * and making necessary cleanup operations.
     *
     * Same as calling stop(false)
     *
     * @see #stop(boolean)
     */
    void stop();

    /**
     * Requests the context to close, shutting down the main loop
     * and making necessary cleanup operations.
     * After the application has stopped, it cannot be used anymore.
     */
    void stop(boolean waitFor);

    /**
     * @return The GUI viewport. Which is used for the on screen
     * statistics and FPS.
     */
    ViewPort getGuiViewPort();

    ViewPort getViewPort();
}

