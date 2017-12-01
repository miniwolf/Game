package mini.shadow;

import mini.bounding.BoundingBox;
import mini.bounding.BoundingVolume;
import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.renderer.ViewPort;
import mini.renderer.queue.GeometryList;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.utils.TempVars;

/**
 * Helper class to collect splitOccluders from scene recursively. It utilizes the scene hierarchy,
 * instead of making the huge flat geometries list first. Instead of adding all geometries from
 * scene to the RenderQueue.shadowCast and checking all of them one by one against the camera
 * frustum the whole Node is checked first to hopefully avoid the check on its children.
 */
public class OccludersExtractor {
    private Matrix4f viewProjectionMatrix;
    private int casterCount;
    private TempVars vars;
    private BoundingBox splitBBox, casterBBox;
    private GeometryList splitOccluders;

    public OccludersExtractor(Matrix4f viewProjectionMatrix,
                              BoundingBox splitBBox,
                              BoundingBox casterBBox,
                              GeometryList splitOccluders,
                              TempVars vars) {
        this.viewProjectionMatrix = viewProjectionMatrix;
        this.splitBBox = splitBBox;
        this.casterBBox = casterBBox;
        this.splitOccluders = splitOccluders;
        this.vars = vars;
    }

    /**
     * Check the rootScene against the camera furstum and if it intersects process it recursively.
     * The global OccludersExtractor variables need to be initialized first.
     * Variables are updated and used in {@link ShadowUtils#updateShadowCamera(ViewPort, GeometryList, Camera, Vector3f[], GeometryList, int)}
     * at last.
     *
     * @param scene rootNode
     */
    public int addOccluders(Spatial scene) {
        if (scene != null) {
            process(scene);
        }
        return casterCount;
    }

    private void process(Spatial scene) {
        if (scene.getCullHint() == Spatial.CullHint.Always) {
            return;
        }

        RenderQueue.ShadowMode shadowMode = scene.getShadowMode();
        if (scene instanceof Geometry) {
            // Convert bounding box to light's viewproj space
            Geometry occluder = (Geometry) scene;
            if (shadowMode == RenderQueue.ShadowMode.Off
                || shadowMode == RenderQueue.ShadowMode.Receive
                || occluder.isGrouped() || occluder.getWorldBound() == null) {
                return;
            }

            BoundingVolume bv = occluder.getWorldBound();
            BoundingVolume occludersBV = bv.transform(viewProjectionMatrix, vars.bbox);

            boolean intersects = splitBBox.intersects(occludersBV);
            if (!intersects && occludersBV instanceof BoundingBox) {
                BoundingBox occluderBBox = (BoundingBox) occludersBV;
                // Extend the occluder further into the frustum, makes sure shadows doesn't
                // disappear when caster itself is not in the view camera but its shadow is in the
                // camera. Number is world units
                occluderBBox.setZExtent(occluderBBox.getZExtent() + 50);
                occluderBBox.setCenter(occluderBBox.getCenter().addLocal(0, 0, 25));
                if (!splitBBox.intersects(occluderBBox)) {
                    return;
                }

                if (!Float.isNaN(occluderBBox.getCenter().x) && !Float
                        .isInfinite(occluderBBox.getCenter().x)) {
                    // Prevent extending the depth range too much we return the bound to its former
                    // shape before adding it
                    occluderBBox.setZExtent(occluderBBox.getZExtent() - 50);
                    occluderBBox.setCenter(occluderBBox.getCenter().subtractLocal(0, 0, 25));
                    casterBBox.mergeLocal(occluderBBox);
                    casterCount++;
                }
                if (splitOccluders != null) {
                    splitOccluders.add(occluder);
                }
            } else if (intersects) {
                casterBBox.mergeLocal(occludersBV);
                casterCount++;
                if (splitOccluders != null) {
                    splitOccluders.add(occluder);
                }
            }
        } else if (scene instanceof Node && scene.getWorldBound() != null) {
            Node nodeOccluder = (Node) scene;

            BoundingVolume bv = nodeOccluder.getWorldBound();
            BoundingVolume occluderBV = bv.transform(viewProjectionMatrix, vars.bbox);
            boolean intersects = splitBBox.intersects(occluderBV);
            if (!intersects && occluderBV instanceof BoundingBox) {
                BoundingBox occluderBBox = (BoundingBox) occluderBV;

                // Extend the occluder further into the frustum, makes sure shadows doesn't
                // disappear when caster itself is not in the view camera but its shadow is in the
                // camera. Number is world units
                occluderBBox.setZExtent(occluderBBox.getZExtent() + 50);
                occluderBBox.setCenter(occluderBBox.getCenter().addLocal(0, 0, 25));
                intersects = splitBBox.intersects(occluderBBox);
            }

            if (intersects) {
                for (Spatial child : ((Node) scene).getChildren()) {
                    process(child);
                }
            }
        }
    }

    public int getCasterCount() {
        return casterCount;
    }
}
