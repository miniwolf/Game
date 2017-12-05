package mini.shadow;

import mini.bounding.BoundingBox;
import mini.bounding.BoundingVolume;
import mini.math.Matrix4f;
import mini.renderer.queue.GeometryList;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.utils.TempVars;

/**
 * OccludersExtractor is a helper class to collect splitOccluders from scene recursively.
 * It utilizes the scene hierarchy, instead of making the huge flat geometries list first.
 * Instead of adding all geometries from scene to the RenderQueue.shadowCast and checking
 * all of them one by one against camera frustum the whole Node is checked first
 * to hopefully avoid the check on its children.
 */
public class OccludersExtractor {
    public Integer casterCount;
    // global variables set in order not to have recursive process method with too many parameters
    Matrix4f viewProjMatrix;
    BoundingBox splitBB, casterBB;
    GeometryList splitOccluders;
    TempVars vars;

    public OccludersExtractor() {
    }

    // initialize the global OccludersExtractor variables
    public OccludersExtractor(Matrix4f vpm, int cc, BoundingBox sBB, BoundingBox cBB,
                              GeometryList sOCC, TempVars v) {
        viewProjMatrix = vpm;
        casterCount = cc;
        splitBB = sBB;
        casterBB = cBB;
        splitOccluders = sOCC;
        vars = v;
    }

    /**
     * Check the rootScene against camera frustum and if intersects process it recursively.
     * The global OccludersExtractor variables need to be initialized first.
     * Variables are updated and used in {@link ShadowUtil#updateShadowCamera} at last.
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
            // convert bounding box to light's viewproj space
            Geometry occluder = (Geometry) scene;
            if (shadowMode != RenderQueue.ShadowMode.Off
                && shadowMode != RenderQueue.ShadowMode.Receive
                && !occluder.isGrouped() && occluder.getWorldBound() != null) {
                BoundingVolume bv = occluder.getWorldBound();
                BoundingVolume occBox = bv.transform(viewProjMatrix, vars.bbox);

                boolean intersects = splitBB.intersects(occBox);
                if (!intersects && occBox instanceof BoundingBox) {
                    BoundingBox occBB = (BoundingBox) occBox;
                    //Kirill 01/10/2011
                    // Extend the occluder further into the frustum
                    // This fixes shadow dissapearing issues when
                    // the caster itself is not in the view camera
                    // but its shadow is in the camera
                    //      The number is in world units
                    occBB.setZExtent(occBB.getZExtent() + 50);
                    occBB.setCenter(occBB.getCenter().addLocal(0, 0, 25));
                    if (splitBB.intersects(occBB)) {
                        //Nehon : prevent NaN and infinity values to screw the final bounding box
                        if (!Float.isNaN(occBox.getCenter().x) && !Float
                                .isInfinite(occBox.getCenter().x)) {
                            // To prevent extending the depth range too much
                            // We return the bound to its former shape
                            // Before adding it
                            occBB.setZExtent(occBB.getZExtent() - 50);
                            occBB.setCenter(occBB.getCenter().subtractLocal(0, 0, 25));
                            casterBB.mergeLocal(occBox);
                            casterCount++;
                        }
                        if (splitOccluders != null) {
                            splitOccluders.add(occluder);
                        }
                    }
                } else if (intersects) {
                    casterBB.mergeLocal(occBox);
                    casterCount++;
                    if (splitOccluders != null) {
                        splitOccluders.add(occluder);
                    }
                }
            }
        } else if (scene instanceof Node && scene.getWorldBound() != null) {
            Node nodeOcc = (Node) scene;
            boolean intersects = false;
            // some
            BoundingVolume bv = nodeOcc.getWorldBound();
            BoundingVolume occBox = bv.transform(viewProjMatrix, vars.bbox);

            intersects = splitBB.intersects(occBox);
            if (!intersects && occBox instanceof BoundingBox) {
                BoundingBox occBB = (BoundingBox) occBox;
                //Kirill 01/10/2011
                // Extend the occluder further into the frustum
                // This fixes shadow dissapearing issues when
                // the caster itself is not in the view camera
                // but its shadow is in the camera
                //      The number is in world units
                occBB.setZExtent(occBB.getZExtent() + 50);
                occBB.setCenter(occBB.getCenter().addLocal(0, 0, 25));
                intersects = splitBB.intersects(occBB);
            }

            if (intersects) {
                for (Spatial child : ((Node) scene).getChildren()) {
                    process(child);
                }
            }
        }
    }
}
