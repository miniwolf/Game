package mini.system;

import mini.renderEngine.Renderer;
import mini.renderEngine.opengl.GLRenderer;

public interface ApplicationContext {
    /**
     * @return True if the context has been created but not yet destroyed.
     */
    boolean isCreated();

    /**
     * Creates the context and makes it active.
     *
     * @param waitFor If true, will wait until context has initialized.
     */
    void create(boolean waitFor);

    /**
     * @return True if the context contains a valid render surface,
     * if any of the rendering methods in {@link GLRenderer} are called
     * while this is <code>false</code>, then the result is undefined.
     */
    boolean isRenderable();

    /**
     * @return The renderer for this context, or null if not created yet.
     */
    Renderer getRenderer();

    /**
     * Sets the listener that will receive events relating to context
     * creation, update, and destroy.
     */
    void setSystemListener(SystemListener listener);

    /**
     * Destroys the context completely, making it inactive.
     *
     * @param waitFor If true, will wait until the context is destroyed fully.
     */
    void destroy(boolean waitFor);
}
