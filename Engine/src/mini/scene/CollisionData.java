package mini.scene;

import mini.bounding.BoundingVolume;
import mini.collision.Collidable;
import mini.collision.CollisionResults;
import mini.math.Matrix4f;

/**
 * <code>CollisionData</code> is an interface that can be used to do triangle-accurate collision
 * with bounding volumes and rays.
 */
public interface CollisionData {
    int collideWith(Collidable other, Matrix4f worldMatrix, BoundingVolume worldBound,
                    CollisionResults results);
}
