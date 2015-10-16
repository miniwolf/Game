package entities;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import rendering.Material;

/**
 * @author miniwolf
 */
public class Face {
    private Material material;

    private Vector2f[] texCoords;
    private Vector3f[] vertices;
    private Vector3f[] normals;

    public Face(Vector3f... vertices) {
        this.vertices = vertices;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Vector2f[] texCoords) {
        this.texCoords = texCoords;
    }

    public Vector3f[] getVertices() {
        return vertices;
    }

    public void setVertices(Vector3f[] vertices) {
        this.vertices = vertices;
    }

    public Vector3f[] getNormals() {
        return normals;
    }

    public void setNormals(Vector3f[] normals) {
        this.normals = normals;
    }
}
