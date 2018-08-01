package mini.app;

import mini.app.state.AbstractApplicationState;
import mini.app.state.ApplicationStateManager;
import mini.renderer.RenderManager;

public class ResetStatsState extends AbstractApplicationState {
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
        //renderManager.getRenderer(). TODO: Missing statistics
    }

    @Override
    public void postRender() {
    }
}
