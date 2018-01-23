package mini.scene.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetLoader;
import mini.asset.plugins.UrlAssetInfo;
import mini.math.Vector3f;
import mini.scene.Mesh;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;

public class AssimpLoader implements AssetLoader {
    @Override
    public Object load(AssetInfo assetInfo) {
        File file = new File(((UrlAssetInfo) assetInfo).getUrl().getFile());
        AIScene aiScene = Assimp.aiImportFile(file.getAbsolutePath(),
                                              aiProcess_JoinIdenticalVertices
                                              | aiProcess_Triangulate);
        return new AssimpModel(aiScene);
    }

    static class AssimpModel {
        private final List<Mesh> meshes;
        AIScene scene;

        public AssimpModel(AIScene scene) {
            this.scene = scene;
            int meshCount = scene.mNumMeshes();
            PointerBuffer meshBuffer = scene.mMeshes();
            meshes = new ArrayList<>();
            for (int i = 0; i < meshCount; i++) {
                meshes.add(new AssimpMesh(AIMesh.create(meshBuffer.get(i))));
            }
        }

        private class AssimpMesh extends Mesh {
            private final AIMesh mesh;

            public AssimpMesh(AIMesh mesh) {
                this.mesh = mesh;

                AIVector3D.Buffer aiVector3DS = mesh.mVertices();
                for (int i = 0; i < aiVector3DS.capacity(); i++) {
                    AIVector3D aiVector3D = aiVector3DS.get(i);
                    Vector3f vertex = new Vector3f(aiVector3D.x(), aiVector3D.y(),
                                                   aiVector3D.z());
                }
            }
        }
    }
}
