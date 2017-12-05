package mini.app.state;

import mini.app.Application;
import mini.renderer.RenderManager;

/**
 * <code>AbstractApplicationState</code> implements some common methods that makes creation of
 * {@link ApplicationState} easier.
 */
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
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        initialized = false;
    }
}
