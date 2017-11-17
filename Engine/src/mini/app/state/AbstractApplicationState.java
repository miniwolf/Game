package mini.app.state;

import mini.app.Application;
import mini.renderer.RenderManager;

public class AbstractApplicationState implements ApplicationState {
    /**
     * <code>initialized</code> is set to true when the method
     * {@link AbstractApplicationState#initialized(ApplicationStateManager, Application)()} is
     * called. When {@link AbstractApplicationState#cleanup()} is called, <code>initialized</code>
     * is set back to false.
     */
    private boolean initialized;
    private boolean enabled = true;

    @Override
    public void initialize(ApplicationStateManager manager, Application app) {
        initialized = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void stateAttached(ApplicationStateManager stateManager) {
    }

    @Override
    public void stateDetached(ApplicationStateManager stateManager) {
    }

    @Override
    public void update() {
    }

    @Override
    public void render(RenderManager renderManager) {
    }

    @Override
    public void postRender() {
    }

    @Override
    public void cleanup() {
        initialized = false;
    }
}
