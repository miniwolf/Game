package mini.scene.debug;

import mini.animation.Bone;
import mini.animation.Skeleton;
import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Map;

/**
 * A class that displays a dotted line between a bone tail and its childrens' heads.
 */
public class SkeletonInterBoneWire extends Mesh {
    private static final int POINT_AMOUNT = 10;
    /**
     * The amount of connections between bones.
     */
    private int connectionsAmount;
    /**
     * The skeleton that will be showed.
     */
    private Skeleton skeleton;
    /**
     * The map between the bone index and its length.
     */
    private Map<Integer, Float> boneLengths;

    /**
     * Creates buffers for points. Each line has POINT_AMOUNT of points.
     *
     * @param skeleton    the skeleton that will be showed
     * @param boneLengths the lengths of the bones
     */
    public SkeletonInterBoneWire(Skeleton skeleton, Map<Integer, Float> boneLengths) {
        this.skeleton = skeleton;

        for (Bone bone : skeleton.getRoots()) {
            this.countConnections(bone);
        }

        this.setMode(Mode.Points);
        this.boneLengths = boneLengths;

        VertexBuffer pb = new VertexBuffer(VertexBuffer.Type.Position);
        FloatBuffer fpb = BufferUtils.createFloatBuffer(POINT_AMOUNT * connectionsAmount * 3);
        pb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, fpb);
        this.setBuffer(pb);

        this.updateCounts();
    }

    /**
     * The method updates the geometry according to the poitions of the bones.
     */
    public void updateGeometry() {
        VertexBuffer vb = this.getBuffer(VertexBuffer.Type.Position);
        FloatBuffer posBuf = this.getFloatBuffer(VertexBuffer.Type.Position);
        posBuf.clear();
        for (int i = 0; i < skeleton.getBoneCount(); ++i) {
            Bone bone = skeleton.getBone(i);
            Vector3f parentTail = bone.getModelSpacePosition().add(bone.getModelSpaceRotation()
                                                                       .mult(Vector3f.UNIT_Y
                                                                                     .mult(boneLengths
                                                                                                   .get(i))));

            for (Bone child : bone.getChildren()) {
                Vector3f childHead = child.getModelSpacePosition();
                Vector3f v = childHead.subtract(parentTail);
                float pointDelta = v.length() / POINT_AMOUNT;
                v.normalizeLocal().multLocal(pointDelta);
                Vector3f pointPosition = parentTail.clone();
                for (int j = 0; j < POINT_AMOUNT; ++j) {
                    posBuf.put(pointPosition.getX()).put(pointPosition.getY())
                          .put(pointPosition.getZ());
                    pointPosition.addLocal(v);
                }
            }
        }
        posBuf.flip();
        vb.updateData(posBuf);

        this.updateBound();
    }

    /**
     * Th method couns the connections between bones.
     *
     * @param bone the bone where counting starts
     */
    private void countConnections(Bone bone) {
        for (Bone child : bone.getChildren()) {
            ++connectionsAmount;
            this.countConnections(child);
        }
    }
}
