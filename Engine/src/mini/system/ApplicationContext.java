package mini.system;

import mini.input.KeyInput;
import mini.input.MouseInput;
import mini.renderer.Renderer;
import mini.renderer.opengl.GLRenderer;
import mini.system.time.Timer;

public interface ApplicationContext {

    /**
     * @return The current display settings. Note that they might be
     * different from the ones set with setDisplaySettings() if the context
     * was restarted or the settings changed internally.
     */
    ApplicationSettings getSettings();

    /**
     * @return True if the context contains a valid render surface, if any of the rendering methods
     * in {@link GLRenderer} are called while this is <code>false</code>, then the result is
     * undefined.
     */
    boolean isRenderable();

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
     * @return The timer for this context, or null if not created yet.
     */
    Timer getTimer();

    /**
     * @return The renderer for this context, or null if not created yet.
     */
    Renderer getRenderer();

    /**
     * @return Mouse input implementation. May be null if not available.
     */
    MouseInput getMouseInput();

    /**
     * @return Keyboard input implementation. May be null if not available.
     */
    KeyInput getKeyInput();

    /**
     * @param settings the display settings to use for the created context. If
     *                 the context has already been created, then <code>restart()</code> must be called
     *                 for the changes to be applied.
     */
    void setSettings(ApplicationSettings settings);

    /**
     * The type of context.
     */
    enum Type {
        /**
         * A display can represent a windowed or a fullscreen-exclusive display.
         * If windowed, the graphics are rendered to a new on-screen surface
         * enclosed in a window defined by the operating system. Implementations
         * are encouraged to not use AWT or Swing to create the OpenGL display
         * but rather use native operating system functions to set up a native
         * display with the windowing system.
         */
        Display,

        /**
         * A canvas type context makes a rendering surface available as an
         * AWT {@link java.awt.Canvas} object that can be embedded in a Swing/AWT
         * frame. To retrieve the Canvas object, you should cast the context
         * to {@link ApplicationCanvasContext}.
         */
        Canvas,

        /**
         * An <code>OffscreenSurface</code> is a context that is not visible
         * by the user. The application can use the offscreen surface to do
         * General Purpose GPU computations or render a scene into a buffer
         * in order to save it as a screenshot, video or send through a network.
         */
        OffscreenSurface,

        /**
         * A <code>Headless</code> context is not visible and does not have
         * any drawable surface. The implementation does not provide any
         * display, input, or sound support.
         */
        Headless
    }

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
