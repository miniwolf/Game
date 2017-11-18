package mini.collision;

import mini.bounding.BoundingBox;
import mini.bounding.BoundingVolume;
import mini.math.FastMath;
import mini.math.Vector3f;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests collision detection between bounding volumes.
 */
public class BoundingCollisionTest {
    private static void checkCollision(Collidable a, Collidable b, boolean expected) {
        if (a instanceof BoundingVolume && b instanceof BoundingVolume) {
            BoundingVolume bv1 = (BoundingVolume) a;
            BoundingVolume bv2 = (BoundingVolume) b;
            Assert.assertEquals(expected, bv1.intersects(bv2));
        }
    }

    private static void checkCollisions(Collidable a, Collidable b, boolean expected) {
        checkCollision(a, b, expected);
        checkCollision(b, a, expected);
    }

    @Test
    public void testBoxBoxCollision() {
        BoundingBox box1 = new BoundingBox(Vector3f.ZERO, 1, 1, 1);
        BoundingBox box2 = new BoundingBox(Vector3f.ZERO, 1, 1, 1);

        checkCollisions(box1, box2, true);

        // Put box2 at the very edge - should still intersect.
        box2.setCenter(new Vector3f(2f, 0, 0));
        checkCollisions(box1, box2, true);

        // Put it a bit father - no intersection expected
        box2.setCenter(new Vector3f(2f + FastMath.ZERO_TOLERANCE, 0, 0));
        checkCollisions(box1, box2, false);

        // Set it in a corner
        box2.setCenter(new Vector3f(2f, 2f, 2f));
        checkCollision(box1, box2, true);

        box2.setCenter(new Vector3f(2f, 2f, 2f + FastMath.ZERO_TOLERANCE));
        checkCollision(box1, box2, false);
    }
}
