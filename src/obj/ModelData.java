package obj;

/**
 * @author miniwolf
 */
public class ModelData {
    private float[] vertices;
    private float[] texCoords;
    private float[] normals;
    private int[] indices;
    private float furthestPoint;

    public ModelData(float[] vertices, float[] texCoords, float[] normals, int[] indices, float furthestPoint) {
        this.vertices = vertices;
        this.texCoords = texCoords;
        this.normals = normals;
        this.indices = indices;
        this.furthestPoint = furthestPoint;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getTexCoords() {
        return texCoords;
    }

    public float[] getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }

    public float getFurthestPoint() {
        return furthestPoint;
    }

}
