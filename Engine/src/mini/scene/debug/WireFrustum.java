package mini.scene.debug;

import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.utils.BufferUtils;

public class WireFrustum extends Mesh {

    public WireFrustum(Vector3f[] points) {
        if (points != null) {
            setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(points));
        }

        setBuffer(VertexBuffer.Type.Index, 2, new short[]{
                0, 1,
                1, 2,
                2, 3,
                3, 0,

                4, 5,
                5, 6,
                6, 7,
                7, 4,

                0, 4,
                1, 5,
                2, 6,
                3, 7
        });

        getBuffer(VertexBuffer.Type.Index).setUsage(VertexBuffer.Usage.Static);
        setMode(Mode.Lines);
    }
}
