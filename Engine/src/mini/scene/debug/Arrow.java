package mini.scene.debug;

import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;

/**
 * The <code>Arrow</code> debug shape represents an arrow. An arrow is simply a line going from the
 * original towards an extent and at the tip there will be a triangle-like shape.
 */
public class Arrow extends Mesh {

    private static final float[] positions = new float[]{
            0, 0, 0,
            0, 0, 1, // tip
            .05f, 0, .9f, // tip right
            -.05f, 0, .9f, // tip left
            0, .05f, .9f, // tip top
            0, -.05f, .9f // tip buttom
    };

    /**
     * Creates an arrow mesh with the given extent. The arrow will start at the origin (0,0,0) and
     * finish at the given extent.
     *
     * @param extent Extent of the arrow from origin
     */
    public Arrow(Vector3f extent) {
        float length = extent.length();
        Vector3f dir = extent.normalize();

        Quaternion lookAtQuat = new Quaternion();
        lookAtQuat.lookAt(dir, Vector3f.UNIT_Y);
        lookAtQuat.normalizeLocal();

        float[] newPositions = new float[positions.length];
        Vector3f pointingPosition = new Vector3f();
        for (int i = 0; i < positions.length; i += 3) {
            Vector3f vec = pointingPosition.set(positions[i], positions[i + 1], positions[i + 2]);
            vec.multLocal(length);
            lookAtQuat.mult(vec, vec);

            newPositions[i] = vec.getX();
            newPositions[i + 1] = vec.getY();
            newPositions[i + 2] = vec.getZ();
        }

        setBuffer(VertexBuffer.Type.Position, 3, newPositions);
        setBuffer(VertexBuffer.Type.Index, 2,
                  new short[]{
                          0, 1,
                          1, 2,
                          1, 3,
                          1, 4,
                          1, 5});
        setMode(Mode.Lines);

        updateBound();
        updateCounts();
    }
}
