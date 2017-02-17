package mini.scene;

import mini.openglObjects.VAO;

public class Model {
    private final VAO vao;

    public Model(VAO vao) {
        this.vao = vao;
    }

    public VAO getVao() {
        return vao;
    }

    public void delete() {
        vao.delete();
    }
}
