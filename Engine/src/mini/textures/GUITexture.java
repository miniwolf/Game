package mini.textures;

import mini.math.Vector2f;
import mini.openglObjects.VAO;
import mini.scene.Model;

/**
 * Created by miniwolf on 17-02-2017.
 */
public class GUITexture {
    private int textureId;
    private Vector2f position;
    private Vector2f scale;
    private float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};
    private Model model;

    public GUITexture(int textureId, Vector2f position, Vector2f scale) {
        this.textureId = textureId;
        this.position = position;
        this.scale = scale;

        setupVAO();
    }

    private void setupVAO() {
        VAO vao = VAO.create();
        vao.storeUnindexedData(positions.length / 2, positions);
        model = new Model(vao);
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

    public Model getModel() {
        return model;
    }
}
