package mini.app;

import mini.app.state.AbstractApplicationState;
import mini.renderer.RenderManager;

public class ResetStatsState extends AbstractApplicationState {
    @Override
    public void render(RenderManager renderManager) {
        super.render(renderManager);
        //renderManager.getRenderer(). TODO: Missing statistics
    }
}
