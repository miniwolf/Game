package loaders;

import mini.objConverter.ModelData;
import mini.objConverter.OBJFileLoader;
import mini.openglObjects.VAO;
import mini.scene.Model;
import mini.utils.MyFile;

public class ModelLoader {
    protected Model loadModel(MyFile modelFile) {
        ModelData data = OBJFileLoader.loadOBJ(modelFile);
        VAO vao = VAO.create();
        vao.storeData(data.getIndices(), data.getVertexCount(), data.getVertices(),
                      data.getTextureCoords(), data.getNormals());
        return new Model(vao);
    }
}
