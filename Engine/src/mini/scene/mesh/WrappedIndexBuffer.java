package mini.scene.mesh;

import mini.scene.Mesh;

public class WrappedIndexBuffer extends VirtualIndexBuffer {
    private final IndexBuffer ib;

    public WrappedIndexBuffer(Mesh mesh) {
        super(mesh.getVertexCount(), mesh.getMode());
        this.ib = mesh.getIndexBuffer();
        switch (meshMode) {
            case Points:
                numIndices = mesh.getTriangleCount();
                break;
            case Lines:
            case LineLoop:
            case LineStrip:
                numIndices = mesh.getTriangleCount() * 2;
                break;
            case Triangles:
            case TriangleStrip:
            case TriangleFan:
                numIndices = mesh.getTriangleCount() * 3;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
