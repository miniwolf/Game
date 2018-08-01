package mini.app;

import mini.app.state.AbstractApplicationState;
import mini.app.state.ApplicationStateManager;
import mini.input.FlyByCamera;
import mini.renderer.RenderManager;

/**
 * Manages a FlyByCamera
 */
public class FlyCamAppState extends AbstractApplicationState {
    private Application app;
    private FlyByCamera flyCam;

    /**
     * This is called by <code>SimpleApplication</code> during initialize().
     */
    void setCamera(FlyByCamera flyCam) {
        this.flyCam = flyCam;
    }

    public FlyByCamera getCamera() {
        return flyCam;
    }

    @Override
    public void initialize(ApplicationStateManager manager, Application app) {
        super.initialize(manager, app);

        this.app = app;

        if (app.getInputManager() == null) {
            return;
        }

        if (flyCam == null) {
            flyCam = new FlyByCamera(app.getCamera());
        }

        flyCam.registerWithInput(app.getInputManager());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        flyCam.setEnabled(enabled);
    }

    @Override
    public void stateAttached(ApplicationStateManager stateManager) {
    }

    @Override
    public void stateDetached(ApplicationStateManager stateManager) {
    }

    @Override
    public void update(float tpf) {
    }

    @Override
    public void render(RenderManager renderManager) {
    }

    @Override
    public void postRender() {
    }

    @Override
    public void cleanup() {
        super.cleanup();

        if (app.getInputManager() != null) {
            flyCam.unregisterInput();
        }
    }
}
