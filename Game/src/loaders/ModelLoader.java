package loaders;

import mini.objConverter.ObjFileLoader;
import mini.openglObjects.VAO;
import mini.scene.Entity;
import mini.scene.Geometry;
import mini.utils.MyFile;

public class ModelLoader {
    protected Entity loadModel(MyFile modelFile) {
        Entity data = ObjFileLoader.loadOBJ(modelFile);
        return data;
//        VAO vao = VAO.create();
//        vao.storeData(data.getIndices(), data.getVertexCount(), data.getVertices(),
//                      data.getTextureCoords(), data.getNormals());
//        return new Geometry(vao);
    }
}
