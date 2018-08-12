package mini.editor.util;

import mini.collision.CollisionResult;
import mini.collision.CollisionResults;
import mini.editor.annotation.FromAnyThread;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.scene.Spatial;

public class GeomUtils {
    @FromAnyThread
    public static Vector3f getContactPointFromScreenPos(
            Spatial spatial,
            Camera camera,
            float screenX,
            float screenY) {
        var collision = getCollisionFromScreenPos(spatial, camera, screenX, screenY);
        return collision == null ? null : collision.getContactPoint();
    }

    private static CollisionResult getCollisionFromScreenPos(
            Spatial spatial,
            Camera camera,
            float screenX,
            float screenY) {
        var results = getCollisionsFromScreenPos(spatial, camera, screenX, screenY);
        if (results.size() < 1) {
            return null;
        }

        return results.getClosestCollision();
    }

    private static CollisionResults getCollisionsFromScreenPos(
            Spatial spatial,
            Camera camera,
            float screenX,
            float screenY) {
        var local = LocalObjects.get();

        var cursor = local.nextVector(screenX, screenY);
        var click3D = camera.getWorldCoordinates(cursor, 0f, local.nextVector());
        var direction = camera.getWorldCoordinates(cursor, 1f, local.nextVector())
                .subtractLocal(click3D)
                .normalizeLocal();

        var ray = local.nextRay();
        ray.setOrigin(click3D);
        ray.setDirection(direction);

        var results = local.nextCollisionResults();

        spatial.updateModelBound();
        spatial.collideWith(ray, results);

        return results;
    }
}
