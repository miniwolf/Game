package mini.app;

import mini.input.FlyByCamera;
import mini.input.controls.ActionListener;
import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.renderEngine.RenderManager;
import mini.renderEngine.Renderer;
import mini.renderEngine.ViewPort;
import mini.renderEngine.opengl.GLRenderer;
import mini.renderEngine.queue.RenderQueue;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.system.ApplicationContext;
import mini.system.ApplicationSystem;
import mini.system.SystemListener;

/**
 * <code>SimpleApplication</code> is the base class for all jME3 Applications.
 * <code>SimpleApplication</code> will display a statistics view
 * using the {@link com.jme3.app.StatsAppState} AppState. It will display
 * the current frames-per-second value on-screen in addition to the statistics.
 * Several keys have special functionality in <code>SimpleApplication</code>:<br/>
 *
 * <table>
 * <tr><td>Esc</td><td>- Close the application</td></tr>
 * <tr><td>C</td><td>- Display the camera position and rotation in the console.</td></tr>
 * <tr><td>M</td><td>- Display memory usage in the console.</td></tr>
 * </table>
 *
 * A {@link com.jme3.app.FlyCamAppState} is by default attached as well and can
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
        super();
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * Retrieves flyCam
     * @return flyCam Camera object
     *
     */
    public FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    /**
     * Retrieves guiNode
     * @return guiNode Node object
     *
     */
    public Node getGuiNode() {
        return guiNode;
    }

    /**
     * Retrieves rootNode
     * @return rootNode Node object
     *
     */
    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    /**
     * Toggles settings window to display at start-up
     * @param showSettings Sets true/false
     *
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

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        super.update(); // makes sure to execute AppTasks
        if (speed == 0 || paused) {
            return;
        }

        // simple update and root node
        simpleUpdate();

        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        // render states
//        stateManager.render(renderManager);

        renderManager.render(0, context.isRenderable());
        simpleRender(renderManager);
    }

    public abstract void simpleInitApp();

    public void simpleUpdate() {
    }

    public void simpleRender(RenderManager rm) {
    }
}
