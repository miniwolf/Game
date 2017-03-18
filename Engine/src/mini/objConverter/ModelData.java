package mini.objConverter;

public class ModelData {
    private static final int DIMENSIONS = 3;

    private float[] vertices;
    private float[] textureCoords;
    private float[] normals;
    private int[] indices;

    public ModelData(float[] vertices, float[] textureCoords, float[] normals, int[] indices) {
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.indices = indices;
    }

    public int getVertexCount() {
        return vertices.length / DIMENSIONS;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public float[] getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }
}
