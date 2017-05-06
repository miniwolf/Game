package mini.renderEngine;

import mini.math.Vector3f;
import mini.renderEngine.opengl.GLRenderer;
import mini.shaders.ShaderProgram;
import mini.shaders.UniformBinding;
import mini.shaders.UniformBindingManager;
import mini.utils.Camera;

/**
 * Created by miniwolf on 30-04-2017.
 */
public class RenderManager {
    private final UniformBindingManager uniformBindingManager = new UniformBindingManager();
    private final GLRenderer renderer;

    /**
     * Create a high-level rendering interface over the
     * low-level rendering interface.
     *
     * @param renderer
     */
    public RenderManager(GLRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Internal use only.
     * Updates the given list of uniforms with {@link UniformBinding uniform bindings}
     * based on the current world state.
     */
    public void updateUniformBindings(ShaderProgram shader) {
        uniformBindingManager.updateUniformBindings(shader);
    }

    /**
     * Set the camera to use for rendering.
     * <p>
     * First, the camera's
     * {@link Camera#setViewPort(float, float, float, float) view port parameters}
     * are applied. Then, the camera's {@link Camera#getViewMatrix() view} and
     * {@link Camera#getProjectionMatrix() projection} matrices are set
     * on the renderer.
     *
     * @param cam The camera to set
     */
    public void setCamera(Camera cam) {
        //setViewPort(cam);
        setViewProjection(cam);
    }

    private void setViewProjection(Camera cam) {
        uniformBindingManager.setCamera(cam);
    }

    /**
     * The renderer implementation used for rendering operations.
     *
     * @return The renderer implementation
     * @see #RenderManager(GLRenderer)
     * @see GLRenderer
     */
    public GLRenderer getRenderer() {
        return renderer;
    }

    public void setLightDir(Vector3f lightDir) {
        uniformBindingManager.setLightDir(lightDir);
    }
}
