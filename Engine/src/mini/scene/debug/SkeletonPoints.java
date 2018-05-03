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
 * The class that displays either heads of the bones if no length data is supplied or both heads and tails otherwise.
 */
public class SkeletonPoints extends Mesh {
    /**
     * The skeleton to be displayed.
     */
    private Skeleton skeleton;
    /**
     * The map between the bone index and its length.
     */
    private Map<Integer, Float> boneLengths;

    /**
     * Creates a points with no length data. The points will only show the bone's heads.
     *
     * @param skeleton the skeleton that will be shown
     */
    public SkeletonPoints(Skeleton skeleton) {
        this(skeleton, null);
    }

    /**
     * Creates a points with bone lengths data. If the data is supplied then the points will show both head and tail of each bone.
     *
     * @param skeleton    the skeleton that will be shown
     * @param boneLengths a map between the bone's index and the bone's length
     */
    public SkeletonPoints(Skeleton skeleton, Map<Integer, Float> boneLengths) {
        this.skeleton = skeleton;
        this.setMode(Mode.Points);
        int pointsCount = skeleton.getBoneCount();

        if (boneLengths != null) {
            this.boneLengths = boneLengths;
            pointsCount *= 2;
        }

        VertexBuffer pb = new VertexBuffer(VertexBuffer.Type.Position);
        FloatBuffer fpb = BufferUtils.createFloatBuffer(pointsCount * 3);
        pb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, fpb);
        this.setBuffer(pb);

        this.updateCounts();

    }

    /**
     * The method updates the geometry according to the positions of the bones.
     */
    public void updateGeometry() {
        VertexBuffer vb = this.getBuffer(VertexBuffer.Type.Position);
        FloatBuffer posBuf = this.getFloatBuffer(VertexBuffer.Type.Position);
        posBuf.clear();
        for (int i = 0; i < skeleton.getBoneCount(); ++i) {
            Bone bone = skeleton.getBone(i);
            Vector3f head = bone.getModelSpacePosition();

            posBuf.put(head.getX()).put(head.getY()).put(head.getZ());
            if (boneLengths != null) {
                Vector3f tail = head.add(bone.getModelSpaceRotation()
                                             .mult(Vector3f.UNIT_Y.mult(boneLengths.get(i))));
                posBuf.put(tail.getX()).put(tail.getY()).put(tail.getZ());
            }
        }
        posBuf.flip();
        vb.updateData(posBuf);

        this.updateBound();
    }
}

