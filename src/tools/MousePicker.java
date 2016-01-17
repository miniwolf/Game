package tools;

import entities.Camera;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Display;
import org.lwjgl.util.vector.*;
import rendering.DisplayManager;

/**
 * Created by miniwolf on 22-12-2015.
 */
public class MousePicker {
    private Vector3f ray;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Camera camera;

    public MousePicker(Matrix4f projectionMatrix, Camera camera) {
        this.projectionMatrix = projectionMatrix;
        this.viewMatrix = Maths.createViewMatrix(camera);
        this.camera = camera;
    }

    public void update() {
        viewMatrix = Maths.createViewMatrix(camera);
        //ray = calculateRay();
    }

    /*private Vector3f calculateRay() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        Vector2f normalized = getNormalizedDeviceCoordinates(mouseX, mouseY);
        Vector4f clipCoords = new Vector4f(normalized.x, normalized.y, -1f, 1f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
    }

    private Vector4f toWorldCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);

    }  */

    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
        Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    private Vector2f getNormalizedDeviceCoordinates(float mouseX, float mouseY) {
        float x = (2f * mouseX) / org.lwjgl.opengl.Display.getWidth() - 1;
        float y = (2f * mouseY) / org.lwjgl.opengl.Display.getWidth() - 1;
        return new Vector2f(x, y);
    }

    public Vector3f getRay() {
        return ray;
    }
}
