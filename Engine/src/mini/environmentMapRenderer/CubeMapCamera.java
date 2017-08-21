package mini.environmentMapRenderer;

import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.renderEngine.Camera;

public class CubeMapCamera extends Camera {
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 200f;
    private static final float FOV = 90;// don't change!
    private static final float ASPECT_RATIO = 1;

    private final Vector3f center;
    private float pitch = 0;
    private float yaw = 0;

    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projectionViewMatrix = new Matrix4f();

    public CubeMapCamera(Vector3f center) {
        this.center = center;
        createProjectionMatrix();
    }

    public void switchToFace(int faceIndex) {
        switch (faceIndex) {
            case 0:
                pitch = 0;
                yaw = 90;
                break;
            case 1:
                pitch = 0;
                yaw = -90;
                break;
            case 2:
                pitch = -90;
                yaw = 180;
                break;
            case 3:
                pitch = 90;
                yaw = 180;
                break;
            case 4:
                pitch = 0;
                yaw = 180;
                break;
            case 5:
                pitch = 0;
                yaw = 0;
                break;
        }
        updateViewMatrix();
    }

    @Override
    public void update() {
    }

    @Override
    public Vector3f getLocation() {
        return center;
    }

    @Override
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public Matrix4f getViewProjectionMatrix() {
        return projectionViewMatrix;
    }

    private void createProjectionMatrix() {
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
        float x_scale = y_scale / ASPECT_RATIO;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m32 = -1;
        projectionMatrix.m23 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
    }

    private void updateViewMatrix() {
        viewMatrix.loadIdentity();
        viewMatrix.angleRotation(new Vector3f(0, 0, 180));
        viewMatrix.angleRotation(new Vector3f(pitch, 0, 0));
        viewMatrix.angleRotation(new Vector3f(0, yaw, 0));
        viewMatrix.setTranslation(new Vector3f(-center.x, -center.y, -center.z));

        // TODO: Projection must be left and view must be right
        projectionViewMatrix = projectionMatrix.mult(viewMatrix);
    }

}
