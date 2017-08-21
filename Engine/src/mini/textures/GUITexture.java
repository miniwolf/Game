package mini.textures;

import mini.math.Vector2f;
import mini.scene.Mesh;
import mini.openglObjects.VAO;
import mini.scene.Geometry;

/**
 * Created by miniwolf on 17-02-2017.
 */
public class GUITexture {
    private int textureId;
    private Vector2f position;
    private Vector2f scale;
    private float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};
    private Geometry geometry;

    public GUITexture(int textureId, Vector2f position, Vector2f scale) {
        this.textureId = textureId;
        this.position = position;
        this.scale = scale;

        setupVAO();
    }

    private void setupVAO() {
        VAO vao = VAO.create();
        vao.storeUnindexedData(positions.length / 2, positions);
        Mesh mesh = new Mesh();
        mesh.setId(vao.getId());
        geometry = new Geometry("texture", mesh);
    }

    public int getTextureId() {
        return textureId;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getScale() {
        return scale;
    }

    public Geometry getGeometry() {
        return geometry;
    }
}
