package mini.scene.control;

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
}
