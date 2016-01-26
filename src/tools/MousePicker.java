package tools;

import entities.Camera;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import terrain.Terrain;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author miniwolf
 */
public class MousePicker {
    private static final int RECURSION_COUNT = 200;
    private static final float RAY_RANGE = 600;

    private Vector3f ray;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Camera camera;
    private Vector3f currentRay = new Vector3f();
    private Vector3f currentTerrainPoint;
    private List<Terrain> terrains;

    public MousePicker(Matrix4f projectionMatrix, Camera camera, List<Terrain> terrains) {
        this.projectionMatrix = projectionMatrix;
        this.viewMatrix = Maths.createViewMatrix(camera);
        this.camera = camera;
        this.terrains = terrains;
    }

    public void update() {
        viewMatrix = Maths.createViewMatrix(camera);
        currentRay = calculateRay();
        if ( intersectionInRange(0, RAY_RANGE, currentRay) ) {
            currentTerrainPoint = binarySearch(0, 0, RAY_RANGE, currentRay);
        } else {
            currentTerrainPoint = null;
        }
    }

    private Vector3f calculateRay() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        Vector2f normalized = getNormalizedDeviceCoordinates(mouseX, mouseY);
        Vector4f clipCoords = new Vector4f(normalized.x, normalized.y, -1f, 1f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        return toWorldCoords(eyeCoords);
    }

    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
        Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay.normalise();
        return mouseRay;
    }

    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
        Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    private Vector2f getNormalizedDeviceCoordinates(float mouseX, float mouseY) {
        float x = (2f * mouseX) / org.lwjgl.opengl.Display.getWidth() - 1;
        float y = (2f * mouseY) / org.lwjgl.opengl.Display.getHeight() - 1;
        return new Vector2f(x, y);
    }

    private Vector3f getPointOnRay(Vector3f ray, float distance) {
        Vector3f camPos = camera.getPosition();
        Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
        Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
        return Vector3f.add(start, scaledRay, null);
    }

    private Vector3f binarySearch(int count, float start, float finish, Vector3f ray) {
        float half = start + ((finish - start) / 2f);
        if ( count >= RECURSION_COUNT ) {
            Vector3f endPoint = getPointOnRay(ray, half);
            Terrain terrain = getTerrain(endPoint.getX(), endPoint.getZ());
            return terrain != null ? endPoint : null;
        }
        if ( intersectionInRange(start, half, ray) ) {
            return binarySearch(count + 1, start, half, ray);
        } else {
            return binarySearch(count + 1, half, finish, ray);
        }
    }

    private boolean intersectionInRange(float start, float finish, Vector3f ray) {
        Vector3f startPoint = getPointOnRay(ray, start);
        Vector3f endPoint = getPointOnRay(ray, finish);
        return !isUnderGround(startPoint) && isUnderGround(endPoint);
    }

    private boolean isUnderGround(Vector3f testPoint) {
        Terrain terrain = getTerrain(testPoint.getX(), testPoint.getZ());
        float height = 0;
        if ( terrain != null ) {
            height = terrain.getHeightOfTerrain(testPoint.getX(), testPoint.getZ());
        }
        return testPoint.y < height;
    }

    private Terrain getTerrain(float worldX, float worldZ) {
        int x = (int) (worldX / Terrain.SIZE);
        int z = (int) (worldZ / Terrain.SIZE);
        //System.out.println("x: " + x + "z: " + z);
        return terrains.parallelStream().filter(terrain -> terrain.getX() == x && terrain.getZ() == z)
                       .findAny().orElse(null);
    }

    public Vector3f getCurrentTerrainPoint() {
        return currentTerrainPoint;
    }

    public Vector3f getCurrentRay() {
        return currentRay;
    }
}
