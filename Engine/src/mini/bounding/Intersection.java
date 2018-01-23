package mini.bounding;

import mini.math.FastMath;
import mini.math.Vector3f;
import mini.utils.TempVars;

public final class Intersection {
    private Intersection() {
    }

    public static boolean intersect(BoundingSphere boundingSphere, Vector3f center, float radius) {
        assert Vector3f.isValidVector(center) && Vector3f.isValidVector(boundingSphere.center);

        try (TempVars vars = TempVars.get()) {
            Vector3f diff = center.subtract(boundingSphere.center, vars.vect1);
            float radiusSum = boundingSphere.getRadius() + radius;
            return diff.dot(diff) <= radiusSum * radiusSum;
        }
    }

    // Algorithm from http://www.mrtc.mdh.se/projects/3Dgraphics/paperF.pdf TODO: Look at QRI
    public static boolean intersect(BoundingBox boundingbox, Vector3f center, float radius) {
        assert Vector3f.isValidVector(center) && Vector3f.isValidVector(boundingbox.center);

        float distSqr = radius * radius;

        float minX = boundingbox.center.x - boundingbox.getXExtent();
        float maxX = boundingbox.center.x + boundingbox.getXExtent();

        float minY = boundingbox.center.y - boundingbox.getYExtent();
        float maxY = boundingbox.center.y + boundingbox.getYExtent();

        float minZ = boundingbox.center.z - boundingbox.getZExtent();
        float maxZ = boundingbox.center.z + boundingbox.getZExtent();

        if (center.x < minX) {
            distSqr -= FastMath.sqr(center.x - minX);
        } else if (center.x > maxX) {
            distSqr -= FastMath.sqr(center.x - maxX);
        }

        if (center.y < minY) {
            distSqr -= FastMath.sqr(center.y - minY);
        } else if (center.y > maxY) {
            distSqr -= FastMath.sqr(center.y - maxY);
        }

        if (center.z < minZ) {
            distSqr -= FastMath.sqr(center.z - minZ);
        } else if (center.z > maxZ) {
            distSqr -= FastMath.sqr(center.z - maxZ);
        }

        return distSqr > 0;
    }
}
