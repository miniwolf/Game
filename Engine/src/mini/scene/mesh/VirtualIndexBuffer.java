package mini.scene.mesh;

import mini.scene.Mesh;

import java.nio.Buffer;

public class VirtualIndexBuffer extends IndexBuffer {

    protected final Mesh.Mode meshMode;
    private final int numVertices;
    protected int numIndices;

    public VirtualIndexBuffer(int numVertices, Mesh.Mode meshMode) {
        this.numVertices = numVertices;
        this.meshMode = meshMode;
        switch (meshMode) {
            case Points:
            case Lines:
            case Triangles:
                numIndices = numVertices;
                return;
            case LineLoop:
                numIndices = (numVertices - 1) * 2 + 1;
                return;
            case LineStrip:
                numIndices = (numVertices - 1) * 2;
                return;
            case TriangleFan:
            case TriangleStrip:
                numIndices = (numVertices - 2) * 3;
                return;
            case Patch:
            case Hybrid:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public int get(int i) {
        switch (meshMode) {
            case Points:
            case Lines:
            case Triangles:
                return i;
            case LineStrip:
                return (i + 1) / 2;
            case LineLoop:
                return (i == (numVertices - 1)) ? 0 : ((i + 1) / 2);
            case TriangleStrip: {
                int triangleIndex = i / 3;
                int vertexIndex = i % 3;
                boolean isBack = (i / 3) % 2 == 1;
                if (!isBack) {
                    return triangleIndex + vertexIndex;
                } else {
                    switch (vertexIndex) {
                        case 0:
                            return triangleIndex + 1;
                        case 1:
                            return triangleIndex;
                        case 2:
                            return triangleIndex + 2;
                        default:
                            throw new AssertionError();

                    }
                }
            }
            case TriangleFan: {
                int vertexIndex = i % 3;
                if (vertexIndex == 0) {
                    return 0;
                } else {
                    return (i / 3) + vertexIndex;
                }
            }
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void put(int i, int value) {
        throw new UnsupportedOperationException("This does not represent an index buffer");
    }

    @Override
    public int size() {
        return numIndices;
    }

    @Override
    public Buffer getBuffer() {
        return null;
    }
}
