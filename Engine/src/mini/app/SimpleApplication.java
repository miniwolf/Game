package mini.app;

import mini.app.state.ApplicationState;
import mini.input.FlyByCamera;
import mini.input.KeyInput;
import mini.input.controls.ActionListener;
import mini.input.controls.KeyTrigger;
import mini.renderer.RenderManager;
import mini.renderer.queue.RenderQueue;
import mini.scene.Node;
import mini.scene.Spatial;

/**
 * <code>SimpleApplication</code> is the base class for all Applications.
 * <code>SimpleApplication</code> will display a statistics view
 * using the {@link mini.app.StatsAppState} AppState. It will display
 * the current frames-per-second value on-screen in addition to the statistics.
 * Several keys have special functionality in <code>SimpleApplication</code>:<br/>
 * <p>
 * <table>
 * <tr><td>Esc</td><td>- Close the application</td></tr>
 * <tr><td>C</td><td>- Display the camera position and rotation in the console.</td></tr>
 * <tr><td>M</td><td>- Display memory usage in the console.</td></tr>
 * </table>
 * <p>
 * A {@link mini.app.FlyCamAppState} is by default attached as well and can
 * be removed by calling <code>stateManager.detach( stateManager.getState(FlyCamAppState.class) );</code>
 */
public abstract class SimpleApplication extends LegacyApplication {

    public static final String INPUT_MAPPING_EXIT = "SIMPLEAPP_Exit";

    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");
    protected FlyByCamera flyCam;
    protected boolean showSettings = true;
    private AppActionListener actionListener = new AppActionListener();

    private class AppActionListener implements ActionListener {

        public void onAction(String name, boolean value, float tpf) {
            if (!value) {
                return;
            }

            if (name.equals(INPUT_MAPPING_EXIT)) {
                stop();
            }
        }
    }

    public SimpleApplication() {
        this(new FlyCamAppState());
    }

    public SimpleApplication(ApplicationState... initialStates) {
        super(initialStates);
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * Retrieves flyCam
     *
     * @return flyCam Camera object
     */
    public FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    /**
     * Retrieves guiNode
     *
     * @return guiNode Node object
     */
    public Node getGuiNode() {
        return guiNode;
    }

    /**
     * Retrieves rootNode
     *
     * @return rootNode Node object
     */
    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    /**
     * Toggles settings window to display at start-up
     *
     * @param showSettings Sets true/false
     */
    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    @Override
    public void initialize() {
        super.initialize();
        guiNode.setQueueBucket(RenderQueue.Bucket.Gui);
        guiNode.setCullHint(Spatial.CullHint.Never);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null) {

            // We have to special-case the FlyCamAppState because too
            // many SimpleApplication subclasses expect it to exist in
            // simpleInit().  But at least it only gets initialized if
            // the app state is added.
            if (stateManager.getState(FlyCamAppState.class) != null) {
                flyCam = new FlyByCamera(cam);
                flyCam.setMoveSpeed(1f); // odd to set this here but it did it before
                stateManager.getState(FlyCamAppState.class).setCamera(flyCam);
            }

            inputManager.addMapping(INPUT_MAPPING_EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
            inputManager.addListener(actionListener, INPUT_MAPPING_EXIT);
        }

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        super.update(); // makes sure to execute AppTasks
        if (speed == 0 || paused) {
            return;
        }

        float tpf = timer.getTimePerFrame() * speed;

        // update states
        stateManager.update(tpf);

        // simple update and root node
        simpleUpdate(tpf);

        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);

        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        // render states
        stateManager.render(renderManager);

        renderManager.render(tpf, context.isRenderable());
        simpleRender(renderManager);
        stateManager.postRender();
    }

    @Override
    public void destroy() {
        simpleDestroy();
        super.destroy();
    }

    public abstract void simpleInitApp();

    public void simpleDestroy() {
    }

    public void simpleUpdate(float tpf) {
    }

    public void simpleRender(RenderManager rm) {
    }
}
