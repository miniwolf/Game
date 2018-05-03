package mini.scene.debug;

import mini.animation.Bone;
import mini.animation.Skeleton;
import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Map;

/**
 * The class that displays either wires between the bones' heads if no length data is supplied and
 * full bones' shapes otherwise.
 */
public class SkeletonWire extends Mesh {
    /**
     * The number of bones' connections. Used in non-length mode.
     */
    private int numConnections;
    /**
     * The skeleton to be displayed.
     */
    private Skeleton skeleton;
    /**
     * The map between the bone index and its length.
     */
    private Map<Integer, Float> boneLengths;

    /**
     * Creates a wire with no length data. The wires will be a connection between the bones' heads only.
     *
     * @param skeleton the skeleton that will be shown
     */
    public SkeletonWire(Skeleton skeleton) {
        this(skeleton, null);
    }

    /**
     * Creates a wire with bone lengths data. If the data is supplied then the wires will show each full bone (from head to tail).
     *
     * @param skeleton    the skeleton that will be shown
     * @param boneLengths a map between the bone's index and the bone's length
     */
    public SkeletonWire(Skeleton skeleton, Map<Integer, Float> boneLengths) {
        this.skeleton = skeleton;

        for (Bone bone : skeleton.getRoots()) {
            this.countConnections(bone);
        }

        this.setMode(Mode.Lines);
        int lineVerticesCount = skeleton.getBoneCount();
        if (boneLengths != null) {
            this.boneLengths = boneLengths;
            lineVerticesCount *= 2;
        }

        VertexBuffer pb = new VertexBuffer(VertexBuffer.Type.Position);
        FloatBuffer fpb = BufferUtils.createFloatBuffer(lineVerticesCount * 3);
        pb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, fpb);
        this.setBuffer(pb);

        VertexBuffer ib = new VertexBuffer(VertexBuffer.Type.Index);
        ShortBuffer sib = BufferUtils
                .createShortBuffer(boneLengths != null ? lineVerticesCount : numConnections * 2);
        ib.setupData(VertexBuffer.Usage.Static, 2, VertexBuffer.Format.UnsignedShort, sib);
        this.setBuffer(ib);

        if (boneLengths != null) {
            for (int i = 0; i < lineVerticesCount; ++i) {
                sib.put((short) i);
            }
        } else {
            for (Bone bone : skeleton.getRoots()) {
                this.writeConnections(sib, bone);
            }
        }
        sib.flip();

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

    /**
     * Th method couns the connections between bones.
     *
     * @param bone the bone where counting starts
     */
    private void countConnections(Bone bone) {
        for (Bone child : bone.getChildren()) {
            numConnections++;
            this.countConnections(child);
        }
    }

    /**
     * The method writes the indexes for the connection vertices. Used in non-length mode.
     *
     * @param indexBuf the index buffer
     * @param bone     the bone
     */
    private void writeConnections(ShortBuffer indexBuf, Bone bone) {
        for (Bone child : bone.getChildren()) {
            // write myself
            indexBuf.put((short) skeleton.getBoneIndex(bone));
            // write the child
            indexBuf.put((short) skeleton.getBoneIndex(child));

            this.writeConnections(indexBuf, child);
        }
    }
}
