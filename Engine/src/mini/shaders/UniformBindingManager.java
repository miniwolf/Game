package mini.shaders;

import mini.material.Material;
import mini.math.Matrix3f;
import mini.math.Matrix4f;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.renderEngine.Camera;
import mini.scene.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>UniformBindingManager</code> helps {@link RenderManager} to manage
 * {@link UniformBinding uniform bindings}.
 * <p>
 * The {@link #updateUniformBindings(java.util.List) } will update
 * a given list of uniforms based on the current state
 * of the manager.
 *
 * @author Kirill Vainer
 */
public class UniformBindingManager {
    private float near, far;
    private Float time, tpf;
    private int viewX, viewY, viewWidth, viewHeight;
    private Vector3f camUp = new Vector3f(),
            camLeft = new Vector3f(),
            camDir = new Vector3f(),
            camLoc = new Vector3f();
    private Matrix4f tempMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projMatrix = new Matrix4f();
    private Matrix4f viewProjMatrix = new Matrix4f();
    private Matrix4f worldMatrix = new Matrix4f();
    private Matrix4f worldViewMatrix = new Matrix4f();
    private Matrix4f worldViewProjMatrix = new Matrix4f();
    private Matrix3f normalMatrix = new Matrix3f();
    private Matrix4f worldMatrixInv = new Matrix4f();
    private Matrix3f worldMatrixInvTrsp = new Matrix3f();
    private Matrix4f viewMatrixInv = new Matrix4f();
    private Matrix4f projMatrixInv = new Matrix4f();
    private Matrix4f viewProjMatrixInv = new Matrix4f();
    private Matrix4f worldViewMatrixInv = new Matrix4f();
    private Matrix3f normalMatrixInv = new Matrix3f();
    private Matrix4f worldViewProjMatrixInv = new Matrix4f();
    private Vector4f viewPort = new Vector4f();
    private Vector2f resolution = new Vector2f();
    private Vector2f resolutionInv = new Vector2f();
    private Vector2f nearFar = new Vector2f();

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
                case ViewMatrix:
                    u.setValue(VarType.Matrix4f, viewMatrix);
                    break;
                case ProjectionMatrix:
                    u.setValue(VarType.Matrix4f, projMatrix);
                    break;
                case ViewProjectionMatrix:
                    u.setValue(VarType.Matrix4f, viewProjMatrix);
                    break;
                case WorldViewMatrix:
                    worldViewMatrix.set(viewMatrix);
                    worldViewMatrix.multLocal(worldMatrix);
                    u.setValue(VarType.Matrix4f, worldViewMatrix);
                    break;
                case NormalMatrix:
                    tempMatrix.set(viewMatrix);
                    tempMatrix.multLocal(worldMatrix);
                    tempMatrix.toRotationMatrix(normalMatrix);
                    normalMatrix.invertLocal();
                    normalMatrix.transposeLocal();
                    u.setValue(VarType.Matrix3f, normalMatrix);
                    break;
                case WorldViewProjectionMatrix:
                    worldViewProjMatrix.set(viewProjMatrix);
                    worldViewProjMatrix.multLocal(worldMatrix);
                    u.setValue(VarType.Matrix4f, worldViewProjMatrix);
                    break;
                case WorldMatrixInverse:
                    worldMatrixInv.set(worldMatrix);
                    worldMatrixInv.invertLocal();
                    u.setValue(VarType.Matrix4f, worldMatrixInv);
                    break;
                case WorldMatrixInverseTranspose:
                    worldMatrix.toRotationMatrix(worldMatrixInvTrsp);
                    worldMatrixInvTrsp.invertLocal().transposeLocal();
                    u.setValue(VarType.Matrix3f, worldMatrixInvTrsp);
                    break;
                case ViewMatrixInverse:
                    viewMatrixInv.set(viewMatrix);
                    viewMatrixInv.invertLocal();
                    u.setValue(VarType.Matrix4f, viewMatrixInv);
                    break;
                case ProjectionMatrixInverse:
                    projMatrixInv.set(projMatrix);
                    projMatrixInv.invertLocal();
                    u.setValue(VarType.Matrix4f, projMatrixInv);
                    break;
                case ViewProjectionMatrixInverse:
                    viewProjMatrixInv.set(viewProjMatrix);
                    viewProjMatrixInv.invertLocal();
                    u.setValue(VarType.Matrix4f, viewProjMatrixInv);
                    break;
                case WorldViewMatrixInverse:
                    worldViewMatrixInv.set(viewMatrix);
                    worldViewMatrixInv.multLocal(worldMatrix);
                    worldViewMatrixInv.invertLocal();
                    u.setValue(VarType.Matrix4f, worldViewMatrixInv);
                    break;
                case NormalMatrixInverse:
                    tempMatrix.set(viewMatrix);
                    tempMatrix.multLocal(worldMatrix);
                    tempMatrix.toRotationMatrix(normalMatrixInv);
                    normalMatrixInv.invertLocal();
                    normalMatrixInv.transposeLocal();
                    normalMatrixInv.invertLocal();
                    u.setValue(VarType.Matrix3f, normalMatrixInv);
                    break;
                case WorldViewProjectionMatrixInverse:
                    worldViewProjMatrixInv.set(viewProjMatrix);
                    worldViewProjMatrixInv.multLocal(worldMatrix);
                    worldViewProjMatrixInv.invertLocal();
                    u.setValue(VarType.Matrix4f, worldViewProjMatrixInv);
                    break;
                case ViewPort:
                    viewPort.set(viewX, viewY, viewWidth, viewHeight);
                    u.setValue(VarType.Vector4f, viewPort);
                    break;
                case Resolution:
                    resolution.set(viewWidth, viewHeight);
                    u.setValue(VarType.Vector2f, resolution);
                    break;
                case ResolutionInverse:
                    resolutionInv.set(1f / viewWidth, 1f / viewHeight);
                    u.setValue(VarType.Vector2f, resolutionInv);
                    break;
                case Aspect:
                    float aspect = ((float) viewWidth) / viewHeight;
                    u.setValue(VarType.Float, aspect);
                    break;
                case FrustumNearFar:
                    nearFar.set(near, far);
                    u.setValue(VarType.Vector2f, nearFar);
                    break;
                case CameraPosition:
                    u.setValue(VarType.Vector3f, camLoc);
                    break;
                case CameraDirection:
                    u.setValue(VarType.Vector3f, camDir);
                    break;
                case CameraLeft:
                    u.setValue(VarType.Vector3f, camLeft);
                    break;
                case CameraUp:
                    u.setValue(VarType.Vector3f, camUp);
                    break;
                case Time:
                    u.setValue(VarType.Float, time);
                    break;
                case Tpf:
                    u.setValue(VarType.Float, tpf);
                    break;
            }
        }
    }

    /**
     * Internal use only. Sets the world matrix to use for future
     * rendering. This has no effect unless objects are rendered manually
     * using {@link Material#render(mini.scene.Geometry, mini.renderer.RenderManager) }.
     * Using {@link #renderGeometry(mini.scene.Geometry) } will
     * override this value.
     *
     * @param mat The world matrix to set
     */
    public void setWorldMatrix(Matrix4f mat) {
        worldMatrix.set(mat);
    }

    public void setCamera(Camera cam, Matrix4f viewMatrix, Matrix4f projMatrix,
                          Matrix4f viewProjMatrix) {
        this.viewMatrix.set(viewMatrix);
        this.projMatrix.set(projMatrix);
        this.viewProjMatrix.set(viewProjMatrix);

        camLoc.set(cam.getLocation());
        cam.getLeft(camLeft);
        cam.getUp(camUp);
        cam.getDirection(camDir);

        near = cam.getFrustumNear();
        far = cam.getFrustumFar();
    }

    public void setViewPort(int viewX, int viewY, int viewWidth, int viewHeight) {
        this.viewX = viewX;
        this.viewY = viewY;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    /**
     * Internal use only.  Called by the RenderManager at the beginning of a
     * new application frame.
     */
    public void newFrame() {
        // Avoid per-material Float allocations and lock in the
        // time for this frame to avoid inter-frame drift.
//        time = timer.getTimeInSeconds();
//        tpf = timer.getTimePerFrame();
    }
}

