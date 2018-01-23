package mini.collision;

import mini.bounding.BoundingBox;
import mini.math.Ray;
import mini.math.Vector3f;

public class TestRayCollision {
    public static void main(String[] args) {
        Ray ray = new Ray(Vector3f.ZERO, Vector3f.UNIT_Z);
        BoundingBox boundingBox = new BoundingBox(new Vector3f(0, 0, 2), 1, 1, 1);

        CollisionResults results = new CollisionResults();
        boundingBox.collideWith(ray, results);

        for (int i = 0; i < results.size(); i++) {
            System.out.println("Collision " + i);
            float distance = results.getCollision(i).getDistance();
            Vector3f contactPoint = results.getCollision(i).getContactPoint();
            System.out.println("Distance " + distance);
            System.out.println("ContactPoint " + contactPoint);
        }
    }
}
