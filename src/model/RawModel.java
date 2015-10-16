package model;

import entities.Face;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * @author miniwolf
 */
public class RawModel {
    private int VAOID;
    private int vertexCount;

    private List<Vector2f> texCoords = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private List<Vector3f> vertices = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();

    public RawModel(int VAOID, int vertexCount) {
        this.VAOID = VAOID;
        this.vertexCount = vertexCount;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVAOID() {
        return VAOID;
    }

    public void addTexture(Vector2f texCoord) {
        texCoords.add(texCoord);
    }

    public void addVertex(Vector3f vertex) {
        vertices.add(vertex);
    }

    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    public void addIndex(Integer index) {
        indices.add(index);
    }

    public List<Vector3f> getNormals() {
        return normals;
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public List<Vector2f> getTexCoords() {
        return texCoords;
    }

    public void addFace(Face mf) {
        faces.add(mf);
    }
}
