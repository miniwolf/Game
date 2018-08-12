package mini.scene.shape;

import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;

public class Line extends Mesh {
    public Line(Vector3f start, Vector3f end) {
        setMode(Mode.Lines);
        updateGeometry(start, end);
    }

    private void updateGeometry(Vector3f start, Vector3f end) {
        setBuffer(
                VertexBuffer.Type.Position,
                3,
                new float[]{
                        start.x, start.y, start.z,
                        end.x, end.y, end.z
                });
        setBuffer(
                VertexBuffer.Type.TexCoord,
                2,
                new float[]{
                        0, 0,
                        1, 1
                });
        setBuffer(
                VertexBuffer.Type.Normal,
                3,
                new float[]{
                        0, 0, 1,
                        0, 0, 1
                });
        setBuffer(
                VertexBuffer.Type.Index,
                2,
                new short[]{0, 1});

        updateBound();
    }
}
