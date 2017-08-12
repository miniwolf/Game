package mini.app;

import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.renderEngine.RenderManager;
import mini.renderEngine.Renderer;
import mini.renderEngine.ViewPort;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Node;
import mini.system.ApplicationContext;
import mini.system.ApplicationSystem;
import mini.system.SystemListener;

public abstract class Application implements SystemListener {
    protected Node rootNode = new Node("Root Node");
    private ApplicationContext context;
    protected RenderManager renderManager;
    protected Camera cam;
    private Renderer renderer;
    private ViewPort viewPort;
    private ViewPort guiViewPort;

    /**
     * Starts the application.
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
            System.err.println("Warning: start() called when application already created!");
            return;
        }

        System.out.println("Starting application: " + getClass().getName());
        context = ApplicationSystem.newContext();
        context.setSystemListener(this);
        context.create(waitFor);
    }

    /**
     * Internal use only.
     */
    public void handleError(String errMsg, Throwable t) {
        // Print error to log.
        System.err.println(t + " : " + errMsg);
        // Display error message on screen
        if (t != null) {
            ApplicationSystem.showErrorDialog(errMsg + "\n" + t.getClass().getSimpleName() +
                    (t.getMessage() != null ? ": " + t.getMessage() : ""));
        } else {
            ApplicationSystem.showErrorDialog(errMsg);
        }

        stop(); // stop the application
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

//        if (inputEnabled) {
//            initInput();
//        }



        // Several things rely on having this
//        guiFont = loadGuiFont();
//
//        guiNode.setQueueBucket(Bucket.Gui);
//        guiNode.setCullHint(CullHint.Never);
        viewPort.attachScene(rootNode);
        //guiViewPort.attachScene(guiNode);

//        if (inputManager != null) {
//
//            // We have to special-case the FlyCamAppState because too
//            // many SimpleApplication subclasses expect it to exist in
//            // simpleInit().  But at least it only gets initialized if
//            // the app state is added.
//            if (stateManager.getState(FlyCamAppState.class) != null) {
//                flyCam = new FlyByCamera(cam);
//                flyCam.setMoveSpeed(1f); // odd to set this here but it did it before
//                stateManager.getState(FlyCamAppState.class).setCamera( flyCam );
//            }
//
//            if (context.getType() == Type.Display) {
//                inputManager.addMapping(INPUT_MAPPING_EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
//            }
//
//            if (stateManager.getState(StatsAppState.class) != null) {
//                inputManager.addMapping(INPUT_MAPPING_HIDE_STATS, new KeyTrigger(KeyInput.KEY_F5));
//                inputManager.addListener(actionListener, INPUT_MAPPING_HIDE_STATS);
//            }
//
//            inputManager.addListener(actionListener, INPUT_MAPPING_EXIT);
//        }

//        if (stateManager.getState(StatsAppState.class) != null) {
//            // Some of the tests rely on having access to fpsText
//            // for quick display.  Maybe a different way would be better.
//            stateManager.getState(StatsAppState.class).setFont(guiFont);
//            fpsText = stateManager.getState(StatsAppState.class).getFpsText();
//        }

        // call user code
        simpleInitApp();
    }

    private void initDisplay() {
        renderer = context.getRenderer();
    }

    /**
     * Creates the camera to use for rendering. Default values are perspective
     * projection with 45° field of view, with near and far values 1 and 1000
     * units respectively.
     */
    private void initCamera(){
        cam = new Camera(1024, 768);

        cam.setFrustumPerspective(45f, (float)cam.getWidth() / cam.getHeight(), 1f, 1000f);
        cam.setPosition(new Vector3f(0f, 0f, 10f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        renderManager = new RenderManager(renderer);

        viewPort = renderManager.createMainView("Default", cam);
        viewPort.setClearFlags(true, true, true);

        // Create a new cam for the gui
//        Camera guiCam = new Camera(1024, 768);
//        guiViewPort = renderManager.createPostView("Gui Default", guiCam);
//        guiViewPort.setClearFlags(false, false, false);
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
        //runQueuedTasks();

        //timer.update();

//        if (inputEnabled){
//            inputManager.update(timer.getTimePerFrame());
//        }

//        if (speed == 0 || paused) {
//            return;
//        }

//        float tpf = timer.getTimePerFrame() * speed;

        // update states
//        stateManager.update(tpf);

        // simple update and root node (User code)
        // simpleUpdate(tpf);

//        if (prof!=null) prof.appStep(AppStep.SpatialUpdate);
//        rootNode.updateLogicalState(tpf);
//        guiNode.updateLogicalState(tpf);

        rootNode.updateGeometricState();
//        guiNode.updateGeometricState();

        // render states
//        stateManager.render(renderManager);

        renderManager.render(context.isRenderable());
        //simpleRender(renderManager); User render step
//        stateManager.postRender();

        // user code here..
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void destroy() {
    }

    public abstract void simpleInitApp();
}
