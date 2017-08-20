package mini.app;

import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.renderEngine.RenderManager;
import mini.renderEngine.Renderer;
import mini.renderEngine.ViewPort;
import mini.system.ApplicationContext;
import mini.system.ApplicationSystem;
import mini.system.SystemListener;
import scala.App;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * The <code>LegacyApplication</code> class represents an instance of a
 * real-time 3D rendering jME application.
 * <p>
 * An <code>LegacyApplication</code> provides all the tools that are commonly used in jME3
 * applications.
 * <p>
 * jME3 applications *SHOULD NOT EXTEND* this class but extend {@link mini.app.SimpleApplication} instead.
 */
public class LegacyApplication implements Application, SystemListener {

    protected Renderer renderer;
    protected RenderManager renderManager;
    protected ViewPort viewPort;
    protected ViewPort guiViewPort;

    protected ApplicationContext context;
    protected Camera cam;

    protected boolean inputEnabled = true;
    protected float speed = 1f;
    protected boolean paused = false;


    private void initDisplay() {
        // aquire important objects
        // from the context
        renderer = context.getRenderer();
    }

    /**
     * Creates the camera to use for rendering. Default values are perspective
     * projection with 45° field of view, with near and far values 1 and 1000
     * units respectively.
     */
    private void initCamera() {
        cam = new Camera(1024, 768);

        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 1f, 1000f);
        cam.setLocation(new Vector3f(0f, 0f, 10f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        renderManager = new RenderManager(renderer);

        viewPort = renderManager.createMainView("Default", cam);
        viewPort.setClearFlags(true, true, true);

        // Create a new cam for the gui
        Camera guiCam = new Camera(1024, 768);
        guiViewPort = renderManager.createPostView("Gui Default", guiCam);
        guiViewPort.setClearFlags(false, false, false);
    }

    /**
     * @return the {@link RenderManager render manager}
     */
    public RenderManager getRenderManager() {
        return renderManager;
    }

    /**
     * @return The {@link Renderer renderer} for the application
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * @return The {@link JmeContext display context} for the application
     */
    public ApplicationContext getContext() {
        return context;
    }

    /**
     * @return The {@link Camera camera} for the application
     */
    public Camera getCamera() {
        return cam;
    }

    /**
     * Starts the application in {@link Type#Display display} mode.
     *
     * @see #start(mini.system.JmeContext.Type)
     */
    public void start() {
        start(false);
    }

    /**
     * Starts the application.
     * Creating a rendering context and executing
     * the main loop in a separate thread.
     */
    public void start(boolean waitFor) {
        if (context != null && context.isCreated()) {
            System.err.println("start() called when application already created!");
            return;
        }

        System.out.println("Starting application: " + getClass().getName());
        context = ApplicationSystem.newContext();
        context.setSystemListener(this);
        context.create(waitFor);
    }

    /**
     * Initializes the application's canvas for use.
     * <p>
     * After calling this method, cast the {@link #getContext() context} to
     * {@link JmeCanvasContext},
     * then acquire the canvas with {@link JmeCanvasContext#getCanvas() }
     * and attach it to an AWT/Swing Frame.
     * The rendering thread will start when the canvas becomes visible on
     * screen, however if you wish to start the context immediately you
     * may call {@link #startCanvas() } to force the rendering thread
     * to start.
     *
     * @see JmeCanvasContext
     * @see Type#Canvas
     */
    public void createCanvas() {
        if (context != null && context.isCreated()) {
            System.err.println("createCanvas() called when application already created!");
            return;
        }

        System.out.println("Starting application: " + getClass().getName());
        context = ApplicationSystem.newContext();
        context.setSystemListener(this);
    }

    /**
     * Starts the rendering thread after createCanvas() has been called.
     * <p>
     * Same as calling startCanvas(false)
     *
     * @see #startCanvas(boolean)
     */
    public void startCanvas() {
        startCanvas(false);
    }

    /**
     * Starts the rendering thread after createCanvas() has been called.
     * <p>
     * Calling this method is optional, the canvas will start automatically
     * when it becomes visible.
     *
     * @param waitFor If true, the current thread will block until the
     *                rendering thread is running
     */
    public void startCanvas(boolean waitFor) {
        context.create(waitFor);
    }

    /**
     * Internal use only.
     */
    public void reshape(int w, int h) {
        if (renderManager != null) {
            renderManager.notifyReshape(w, h);
        }
    }

    /**
     * Restarts the context, applying any changed settings.
     * <p>
     * Changes to the {@link AppSettings} of this Application are not
     * applied immediately; calling this method forces the context
     * to restart, applying the new settings.
     */
    public void restart() {
    }

    /**
     * Requests the context to close, shutting down the main loop
     * and making necessary cleanup operations.
     * <p>
     * Same as calling stop(false)
     *
     * @see #stop(boolean)
     */
    public void stop() {
        stop(false);
    }

    /**
     * Requests the context to close, shutting down the main loop
     * and making necessary cleanup operations.
     * After the application has stopped, it cannot be used anymore.
     */
    public void stop(boolean waitFor) {
        System.out.println("Closing application: " + getClass().getName());
        context.destroy(waitFor);
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     * <p>
     * Initializes the <code>Application</code>, by creating a display and
     * default camera. If display settings are not specified, a default
     * 640x480 display is created. Default values are used for the camera;
     * perspective projection with 45° field of view, with near
     * and far values 1 and 1000 units respectively.
     */
    public void initialize() {
        initDisplay();
        initCamera();

        // user code here..
    }

    /**
     * Internal use only.
     */
    public void handleError(String errMsg, Throwable t) {
        // Print error to log.
        System.err.println(errMsg + t.getMessage());
        // Display error message on screen if not in headless mode
        if (t != null) {
            ApplicationSystem.showErrorDialog(errMsg + "\n" + t.getClass().getSimpleName() +
                                      (t.getMessage() != null ? ": " + t.getMessage() : ""));
        } else {
            ApplicationSystem.showErrorDialog(errMsg);
        }

        stop(); // stop the application
    }

    /**
     * Internal use only.
     */
    public void requestClose(boolean esc) {
        context.destroy(false);
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void update() {
    }

    protected void destroyInput() {
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void destroy() {
        destroyInput();
    }

    /**
     * @return The GUI viewport. Which is used for the on screen
     * statistics and FPS.
     */
    public ViewPort getGuiViewPort() {
        return guiViewPort;
    }

    public ViewPort getViewPort() {
        return viewPort;
    }

    private class RunnableWrapper implements Callable {
        private final Runnable runnable;

        public RunnableWrapper(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public Object call() {
            runnable.run();
            return null;
        }

    }

}
