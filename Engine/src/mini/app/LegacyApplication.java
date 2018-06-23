package mini.app;

import mini.app.state.ApplicationState;
import mini.app.state.ApplicationStateManager;
import mini.asset.AssetManager;
import mini.input.Input;
import mini.input.InputManager;
import mini.input.MouseInput;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.system.ApplicationContext;
import mini.system.ApplicationSettings;
import mini.system.ApplicationSystem;
import mini.system.SystemListener;
import mini.system.time.NanoTimer;
import mini.system.time.Timer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * The <code>LegacyApplication</code> class represents an instance of a real-time 3D rendering
 * application.
 * <p>
 * An <code>LegacyApplication</code> provides all the tools that are commonly used.
 * <p>
 * applications *SHOULD NOT EXTEND* this class but extend {@link mini.app.SimpleApplication} instead.
 */
public class LegacyApplication implements Application, SystemListener {
    protected Renderer renderer;
    protected RenderManager renderManager;
    protected ViewPort viewPort;
    protected ViewPort guiViewPort;

    protected Timer timer = new NanoTimer();
    protected ApplicationContext context;
    protected Camera cam;

    protected boolean inputEnabled = true;
    protected float speed = 1f;
    protected boolean paused = false;
    protected MouseInput mouseInput;
    protected Input keyInput;
    protected InputManager inputManager;
    protected AssetManager assetManager;
    protected ApplicationStateManager stateManager;
    private final ConcurrentLinkedQueue<ApplicationTask<?>> taskQueue
            = new ConcurrentLinkedQueue<>();
    protected ApplicationSettings settings;

    public LegacyApplication(ApplicationState... initialStates) {
        initStateManager();

        if (initialStates != null) {
            Arrays.stream(initialStates)
                    .filter(Objects::nonNull)
                    .forEach(state -> stateManager.attach(state));
        }
    }

    private void initStateManager() {
        stateManager = new ApplicationStateManager(this);

        // Always register a ResetStateState to make sure that the stats are cleared every frame
        stateManager.attach(new ResetStatsState());
    }

    /**
     * Set the display settings to define the display created.
     * <p>
     * Examples of display parameters include display pixel width and height,
     * color bit depth, z-buffer bits, anti-aliasing samples, and update frequency.
     * If this method is called while the application is already running, then
     * {@link #restart() } must be called to apply the settings to the display.
     *
     * @param settings The settings to set.
     */
    public void setSettings(ApplicationSettings settings) {
        this.settings = settings;
        if (context != null && settings.useInput() != inputEnabled) {
            // may need to create or destroy input based on settings change
            inputEnabled = !inputEnabled;
            if (inputEnabled) {
                initInput();
            } else {
                destroyInput();
            }
        } else {
            inputEnabled = settings.useInput();
        }
    }

    private void initDisplay() {
        // acquire important objects from the context
        settings = context.getSettings();

        // Reset timer if a user has not already provided one
        if (timer == null) {
            timer = context.getTimer();
        }

        renderer = context.getRenderer();
    }

    /**
     * Initializes mouse and keyboard input. Also
     * initializes joystick input if joysticks are enabled in the
     * AppSettings.
     */
    private void initInput() {
        mouseInput = context.getMouseInput();
        if (mouseInput != null) {
            mouseInput.initialize();
        }

        keyInput = context.getKeyInput();
        if (keyInput != null) {
            keyInput.initialize();
        }

        inputManager = new InputManager(mouseInput, keyInput);
    }

    /**
     * Creates the camera to use for rendering. Default values are perspective
     * projection with 45° field of view, with near and far values 1 and 1000
     * units respectively.
     */
    private void initCamera() {
        cam = new Camera(1280, 768);

        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 1f, 1000f);
        cam.setLocation(new Vector3f(0f, 0f, 10f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        renderManager = new RenderManager(renderer);
        renderManager.setTimer(timer);

        viewPort = renderManager.createMainView("Default", cam);
        viewPort.setClearFlags(true, true, true);

        // Create a new cam for the gui
        Camera guiCam = new Camera(1280, 768);
        guiViewPort = renderManager.createPostView("Gui Default", guiCam);
        guiViewPort.setClearFlags(false, false, false);
    }

    /**
     * @return The {@link AssetManager asset manager} for this application.
     */
    public AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * @return the {@link InputManager input manager}.
     */
    public InputManager getInputManager() {
        return inputManager;
    }

    /**
     * @return the {@link ApplicationStateManager application state manager}.
     */
    public ApplicationStateManager getStateManager() {
        return stateManager;
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
     * @return The {@link ApplicationContext display context} for the application
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
     * Starts the application in {@link mini.system.ApplicationContext.Type.Display} mode.
     *
     * @see #start(mini.system.ApplicationContext.Type)
     */
    public void start() {
        start(ApplicationContext.Type.Display, false);
    }

    /**
     * Starts the application in {@link mini.system.ApplicationContext.Type.Display} mode.
     *
     * @see #start(mini.system.ApplicationContext.Type)
     */
    public void start(boolean waitFor) {
        start(ApplicationContext.Type.Display, waitFor);
    }

    /**
     * Starts the application. Creating a rendering context and executing the main loop in a
     * separate thread.
     */
    public void start(ApplicationContext.Type contextType) {
        start(contextType, false);
    }

    /**
     * Starts the application. Creating a rendering context and executing the main loop in a
     * separate thread.
     */
    public void start(ApplicationContext.Type contextType, boolean waitFor) {
        if (context != null && context.isCreated()) {
            System.err.println("start() called when application already created!");
            return;
        }

        if (settings == null) {
            settings = new ApplicationSettings(true);
        }

        System.out.println("Starting application: " + getClass().getName());
        context = ApplicationSystem.newContext(settings, contextType);
        context.setSystemListener(this);
        context.create(waitFor);
    }

    /**
     * Initializes the application's canvas for use.
     * <p>
     * After calling this method, cast the {@link #getContext() context} to
     * {@link ApplicationCanvasContext},
     * then acquire the canvas with {@link ApplicationCanvasContext#getCanvas() }
     * and attach it to an AWT/Swing Frame.
     * The rendering thread will start when the canvas becomes visible on
     * screen, however if you wish to start the context immediately you
     * may call {@link #startCanvas() } to force the rendering thread
     * to start.
     *
     * @see ApplicationCanvasContext
     * @see Type#Canvas
     */
    public void createCanvas() {
        if (context != null && context.isCreated()) {
            System.err.println("createCanvas() called when application already created!");
            return;
        }

        if (settings == null) {
            settings = new ApplicationSettings(true);
        }

        System.out.println("Starting application: " + getClass().getName());
        context = ApplicationSystem.newContext(settings, ApplicationContext.Type.Canvas);
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
        initAssetManager();

        initDisplay();
        initCamera();

        if (inputEnabled) {
            initInput();
        }

        timer.reset();

        // user code here..
    }

    private void initAssetManager() {
        URL assetCfgUrl = null;

        if (settings != null) {
            String assetCfg = settings.getString("AssetConfigURL");
            if (assetCfg != null) {
                try {
                    assetCfgUrl = new URL(assetCfg);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (assetCfgUrl == null) {
                    assetCfgUrl = LegacyApplication.class.getClassLoader().getResource(assetCfg);
                    if (assetCfgUrl == null) {
                        System.err.println(
                                "Unable to access AssetConfigURL in asset config: " + assetCfg);
                    }
                }
            }
        }
        if (assetCfgUrl == null) {
            assetCfgUrl = ApplicationSystem.getPlatformAssetConfigURL();
        }
        if (assetManager == null) {
            assetManager = ApplicationSystem.newAssetManager(assetCfgUrl);
        }
    }

    /**
     * Internal use only.
     */
    public void handleError(String errMsg, Throwable t) {
        // Print error to log.
        System.err.println(errMsg + t.getMessage());
        t.printStackTrace();
        // Display error message on screen if not in headless mode
        if (t != null) {
            ApplicationSystem.showErrorDialog(errMsg + "\n" + t.getClass().getSimpleName() +
                    (t.getMessage() != null ? ": " + t.getMessage() :
                            ""));
        } else {
            ApplicationSystem.showErrorDialog(errMsg);
        }

        stop(); // stop the application
    }

    /**
     * Internal use only.
     */
    public void gainFocus() {
        // TODO: implement the lost and gained focus behaviour
    }

    /**
     * Internal use only.
     */
    public void loseFocus() {
        // TODO: implement the lost and gained focus behaviour
    }

    /**
     * Internal use only.
     */
    public void requestClose(boolean esc) {
        context.destroy(false);
    }

    /**
     * Enqueues a task/callable object to execute in the rendering thread.
     * <p>
     * Callables are executed right at the beginning of the main loop. They are executed even if the
     * application is currently paused or out of focus.
     *
     * @param callable The callables to run in the main thread
     */
    public <V> Future<V> enqueue(Callable<V> callable) {
        ApplicationTask<V> task = new ApplicationTask<>(callable);
        taskQueue.add(task);
        return task;
    }

    /**
     * Runs tasks enqueued via {@link #enqueue(Callable)}
     */
    protected void runQueuedTasks() {
        ApplicationTask<?> task;
        while ((task = taskQueue.poll()) != null) {
            if (!task.isCancelled()) {
                task.invoke();
            }
        }
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void update() {
        if (speed == 0 || paused) {
            return;
        }

        timer.update();

        if (inputEnabled) {
            inputManager.update(timer.getTimePerFrame());
        }
    }

    protected void destroyInput() {
        if (mouseInput != null) {
            mouseInput.destroy();
        }

        if (keyInput != null) {
            keyInput.destroy();
        }

        inputManager = null;
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void destroy() {
        stateManager.cleanup();

        destroyInput();

        timer.reset();
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
