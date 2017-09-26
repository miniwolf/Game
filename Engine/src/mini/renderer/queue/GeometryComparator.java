package mini.renderer.queue;

import mini.renderer.Camera;
import mini.scene.Geometry;

import java.util.Comparator;

/**
 * <code>GeometryComparator</code> is a special version of {@link Comparator}
 * that is used to sort geometries for rendering in the {@link RenderQueue}.
 *
 * @author miniwolf
 */
public interface GeometryComparator extends Comparator<Geometry> {
    /**
     * Set the camera to use for sorting.
     *
     * @param cam The camera to use for sorting
     */
    void setCamera(Camera cam);
}
