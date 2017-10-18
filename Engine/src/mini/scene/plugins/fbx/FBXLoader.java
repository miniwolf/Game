package mini.scene.plugins.fbx;

import mini.asset.AssetInfo;
import mini.asset.AssetLoader;
import mini.asset.AssetManager;
import mini.scene.Node;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXFile;
import mini.scene.plugins.fbx.file.FBXReader;
import mini.scene.plugins.fbx.objects.FBXObject;
import mini.scene.plugins.fbx.objects.FBXObjectLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FBXLoader implements AssetLoader {
    private AssetManager assetManager;
    private FBXReader reader = new FBXReader();

    @Override
    public Object load(AssetInfo info) throws IOException {
        this.assetManager = info.getManager();

        final Node sceneNode = new Node("-scene");
        InputStream in = info.openStream();
        loadScene(in);
        return sceneNode;
    }

    private void loadScene(InputStream in) throws IOException {
        FBXFile scene = reader.readFBX(in);
        for (FBXElement fbxElement : scene.getElements()) {
            switch (fbxElement.getName()) {
                case "Objects":
                    loadObjects(fbxElement);
                    break;
                case "Connections":
                    loadConnections(fbxElement);
                default:
                    System.out.println("Skipped elements: " + fbxElement.getName());
            }
        }
    }

    private void loadConnections(FBXElement fbxElement) {

    }

    private void loadObjects(FBXElement fbxElement) {
        FBXObjectLoader loader = new FBXObjectLoader(assetManager);
        List<FBXObject> load = loader.load(fbxElement);
    }
}
