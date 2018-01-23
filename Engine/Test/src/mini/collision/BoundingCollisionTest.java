package mini.collision;

import mini.bounding.BoundingBox;
import mini.bounding.BoundingSphere;
import mini.bounding.BoundingVolume;
import mini.math.FastMath;
import mini.math.Ray;
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

    private static void checkExpectedCollisions(Collidable a, Collidable b,
                                                int expectedCollisions) {
        checkCollision(a, b, expectedCollisions > 0);
        checkCollision(b, a, expectedCollisions > 0);

        countCollisions(a, b, expectedCollisions);
        countCollisions(b, a, expectedCollisions);
    }

    private static void countCollisions(Collidable a, Collidable b, int expectedCollisions) {
        CollisionResults results = new CollisionResults();
        int numCollisions = a.collideWith(b, results);
        Assert.assertEquals(results.size(), numCollisions); // Seems redundant
        Assert.assertEquals(numCollisions, expectedCollisions);

        // Implicitly sorting the results
        results.getClosestCollision();

        if (results.size() > 0) {
            Assert.assertEquals(results.getCollision(0), results.getClosestCollision());
        }
        if (results.size() == 1) {
            Assert.assertEquals(results.getClosestCollision(), results.getFarthestCollision());
        }
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

    @Test
    public void testSphereSphereCollision() {
        BoundingSphere sphereA = new BoundingSphere(1, Vector3f.ZERO);
        BoundingSphere sphereB = new BoundingSphere(1, Vector3f.ZERO);

        // Putting it at the very edge
        sphereB.setCenter(new Vector3f(2f, 0, 0));
        checkCollision(sphereA, sphereB, true);

        // Little further away
        sphereB.setCenter(new Vector3f(2f + FastMath.ZERO_TOLERANCE, 0, 0));
        checkCollision(sphereA, sphereB, false);
    }

    @Test
    public void testBoxSphereCollision() {
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 1, 1, 1);
        BoundingSphere sphere = new BoundingSphere(1, Vector3f.ZERO);
        checkCollision(box, sphere, true);

        // Putting it at the very edge
        sphere.setCenter(new Vector3f(2f, 0, 0));
        checkCollision(box, sphere, false);

        // Little closer
        sphere.setCenter(new Vector3f(2f - FastMath.ZERO_TOLERANCE, 0, 0));
        checkCollision(box, sphere, true);

        // Conversion sphere to a box before testing collision
        float sqrt3 = FastMath.sqrt(3);

        sphere.setCenter(Vector3f.UNIT_XYZ.mult(2));
        sphere.setRadius(sqrt3);
        checkCollision(box, sphere, false);

        // Making it a little bit larger
        sphere.setRadius(sqrt3 + FastMath.ZERO_TOLERANCE);
        checkCollision(box, sphere, true);
    }

    @Test
    public void testBoxRayCollision() {
        BoundingBox box = new BoundingBox(Vector3f.ZERO, 1, 1, 1);
        Ray ray = new Ray(Vector3f.ZERO, Vector3f.UNIT_Z);

        // Technically ray is inside the box and should only hit once? TODO: Think about this
        checkExpectedCollisions(box, ray, 2);

        ray.setOrigin(new Vector3f(0, 0, -5));
        checkExpectedCollisions(box, ray, 2);

        ray.setOrigin(new Vector3f(0, 0, 2)); // Inside of box, see above comment
        checkExpectedCollisions(box, ray, 0);

        ray.setOrigin(new Vector3f(0, 0, -2));
        checkExpectedCollisions(box, ray, 2);

        ray.setOrigin(new Vector3f(0, 1, -2)); // Parallel to the edge, touching the side
        checkExpectedCollisions(box, ray, 2);

        ray.setOrigin(new Vector3f(0, 1 + FastMath.ZERO_TOLERANCE,
                                   -2)); // Still parallel but not touch the side
        checkExpectedCollisions(box, ray, 0);
    }
}
