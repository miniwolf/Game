package mini.scene;

/**
 * Ensures that geometries behave correctly, by casting rays and ensure they don't break.
 */
public class ShapeGeometryTest {

    protected static final int NUMBER_OF_TRIES = 1000;
    
//    @Test
//    public void testCylinders() {
//        Random random = new Random();
//
//        // Create a cylinder, cast a random ray, and ensure everything goes well.
//        Node scene = new Node("Scene Node");
//
//        for (int i = 0; i < NUMBER_OF_TRIES; i++) {
//            scene.detachAllChildren();
//
//            Cylinder cylinder = new Cylinder(2, 8, 1, 1, true);
//            Geometry geometry = new Geometry("cylinder", cylinder);
//            geometry.rotate(FastMath.HALF_PI, 0, 0);
//            scene.attachChild(geometry);
//
//            // Cast a random ray, and count successes and IndexOutOfBoundsExceptions.
//            Vector3f randomPoint = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
//            Vector3f randomDirection = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
//            randomDirection.normalizeLocal();
//
//            Ray ray = new Ray(randomPoint, randomDirection);
//            CollisionResults collisionResults = new CollisionResults();
//
//            // If the geometry is invalid, this should throw various exceptions.
//            scene.collideWith(ray, collisionResults);
//        }
//    }
}
