package mini.renderEngine.queue;

import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.scene.Geometry;

public class TransparentComparator implements GeometryComparator {

    private Camera cam;
    private final Vector3f tempVec = new Vector3f();

    public void setCamera(Camera cam) {
        this.cam = cam;
    }

    /**
     * Calculates the distance from a spatial to the camera. Distance is a
     * squared distance.
     *
     * @param spat Spatial to distancize.
     * @return Distance from Spatial to camera.
     */
    private float distanceToCam2(Geometry spat) {
        if (spat == null) {
            return Float.NEGATIVE_INFINITY;
        }

        if (spat.queueDistance != Float.NEGATIVE_INFINITY) {
            return spat.queueDistance;
        }

        Vector3f camPosition = cam.getLocation();
        Vector3f viewVector = cam.getDirection();
        Vector3f spatPosition = spat.getWorldTranslation();

        spatPosition.subtract(camPosition, tempVec);
        spat.queueDistance = tempVec.dot(tempVec);

        float retval = Math.abs(tempVec.dot(viewVector)
                                / viewVector.dot(viewVector));
        viewVector.mult(retval, tempVec);

        spat.queueDistance = tempVec.length();

        return spat.queueDistance;
    }

    private float distanceToCam(Geometry spat) {
        // NOTE: It is best to check the distance
        // to the bound's closest edge vs. the bound's center here.
        return spat.getWorldBound().distanceToEdge(cam.getLocation());
    }

    public int compare(Geometry o1, Geometry o2) {
        float d1 = distanceToCam(o1);
        float d2 = distanceToCam(o2);

        return Float.compare(d2, d1);
    }
}
