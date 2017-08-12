package mini.shaders;

import mini.material.Material;
import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.scene.Geometry;

import java.util.List;

/**
 * Created by miniwolf on 22-04-2017.
 */
public class UniformBindingManager {
    private Matrix4f worldMatrix = new Matrix4f();
    private Matrix4f viewProjMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projMatrix = new Matrix4f();
    private Vector3f camLoc = new Vector3f();
    private Vector3f lightDir = new Vector3f();

    private int viewX, viewY, viewWidth, viewHeight;

    /**
     * Internal use only.
     * Updates the given list of uniforms with {@link UniformBinding uniform bindings}
     * based on the current world state.
     */
    public void updateUniformBindings(ShaderProgram shader) {
        List<Uniform> params = shader.getBoundUniforms();
        for (Uniform u : params) {
            switch (u.getBinding()) {
                case WorldMatrix:
                    u.setValue(VarType.Matrix4f, worldMatrix);
                    break;
                case ViewProjectionMatrix:
                    u.setValue(VarType.Matrix4f, viewProjMatrix);
                    break;
                case CameraPosition:
                    u.setValue(VarType.Vector3f, camLoc);
                    break;
                case LightDirection:
                    u.setValue(VarType.Vector3f, lightDir);
            }
        }
    }

    /**
     * Internal use only. Sets the world matrix to use for future
     * rendering. This has no effect unless objects are rendered manually
     * using {@link Material#render(Geometry) }.
     *
     * @param mat The world matrix to set
     */
    public void setWorldMatrix(Matrix4f mat) {
        worldMatrix.set(mat);
    }

    public void setViewProjMatrix(Matrix4f viewProjMatrix) {
        this.viewProjMatrix.set(viewProjMatrix);
    }

    public void setCamera(Camera camera) {
        viewProjMatrix.set(camera.getProjectionViewMatrix());
        viewMatrix.set(camera.getViewMatrix());
        projMatrix.set(camera.getProjectionMatrix());
        camLoc = camera.getPosition();
    }

    public void setLightDir(Vector3f lightDir) {
        this.lightDir = lightDir;
    }

    public void setViewPort(int viewX, int viewY, int viewWidth, int viewHeight) {
        this.viewX = viewX;
        this.viewY = viewY;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }
}
