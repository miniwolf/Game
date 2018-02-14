package mini.scene.control;

import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.Spatial;

/**
 * An interface for scene-graph controls.
 * <p>
 * <code>Control</code>s are used to specify certain update and render logic for a {@link Spatial}
 */
public interface Control {
    /**
     * Updates the control. This should never be called from user code.
     *
     * @param tpf Time per frame
     */
    void update(float tpf);

    /**
     * @param spatial the spatial to be controlled. This should not be called from user code.
     */
    void setSpatial(Spatial spatial);

    Spatial getSpatial();

    /**
     * Should be called prior to queueing the spatial by the RenderManager. This should not be
     * called from user code.
     */
    void render(RenderManager renderManager, ViewPort vp);
}
