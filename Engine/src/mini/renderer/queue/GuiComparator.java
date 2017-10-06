package mini.renderer.queue;

import mini.renderer.Camera;
import mini.scene.Geometry;

/**
 * <code>GuiComparator</code> sorts geometries back-to-front based
 * on their Z position.
 */
public class GuiComparator implements GeometryComparator {

    public int compare(Geometry o1, Geometry o2) {
        float z1 = o1.getWorldTranslation().getZ();
        float z2 = o2.getWorldTranslation().getZ();
        return Float.compare(z1, z2);
    }

    public void setCamera(Camera cam) {
    }

}