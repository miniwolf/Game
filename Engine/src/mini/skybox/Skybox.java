package mini.skybox;

import mini.openglObjects.VAO;
import mini.textures.Texture;

public class Skybox {
    private VAO cube;
    private Texture texture;

    public Skybox(Texture cubeMapTexture, float size) {
        cube = CubeGenerator.generateCube(size);
        this.texture = cubeMapTexture;
    }

    public VAO getCubeVao() {
        return cube;
    }

    public Texture getTexture() {
        return texture;
    }

    public void delete() {
        cube.delete();
        texture.delete();
    }
}
