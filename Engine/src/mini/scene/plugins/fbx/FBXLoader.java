package mini.scene.plugins.fbx;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoader;
import mini.asset.AssetManager;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.plugins.fbx.connections.FBXConnectionLoader;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXFile;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.file.FBXReader;
import mini.scene.plugins.fbx.node.FBXNode;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.scene.plugins.fbx.objects.FBXObjectLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FBXLoader implements AssetLoader {
    private AssetManager assetManager;
    private FBXReader reader = new FBXReader();
    Map<FBXId, FBXObject> objects = null;
    private Node sceneNode;
    private AssetKey key;

    @Override
    public Object load(AssetInfo info) throws IOException {
        this.assetManager = info.getManager();
        InputStream in = info.openStream();
        key = info.getKey();

        // Load the data from the stream
        loadScene(in);

        // Create the scene graph from the FBX scene graph.
        Spatial scene = constructSceneGraph();
        return scene;
    }

    private void loadScene(InputStream in) throws IOException {
        FBXFile scene = reader.readFBX(in);
        for (FBXElement fbxElement : scene.getElements()) {
            switch (fbxElement.getName()) {
                case "Objects":
                    objects = loadObjects(fbxElement);
                    break;
                case "Connections":
                    loadConnections(fbxElement);
                default:
                    System.out.println("Skipped elements: " + fbxElement.getName());
            }
        }
    }

    private Spatial constructSceneGraph() {
        // Acquire the implicit root object.
        FBXNode rootNode = (FBXNode) objects.get(FBXId.ROOT);

        // Convert it into a scene
        Node root = (Node) FBXNode.createScene(rootNode);
    }

    private void loadConnections(FBXElement fbxElement) {
        FBXConnectionLoader loader = new FBXConnectionLoader(objects);
        loader.load(fbxElement);
        //new FBXConnector(objects).connectFBXElements(load);
    }

    private Map<FBXId, FBXObject> loadObjects(FBXElement fbxElement) {
        FBXObjectLoader loader = new FBXObjectLoader(assetManager, key);
        return loader.load(fbxElement);
    }
}
