package mini.renderer.queue;

import mini.renderer.Camera;
import mini.scene.Geometry;

/**
 * <code>NullComparator</code> does not sort geometries. They will be in
 * arbitrary order.
 */
public class NullComparator implements GeometryComparator {
    public int compare(Geometry o1, Geometry o2) {
        return 0;
    }

    public void setCamera(Camera cam) {
    }
}
