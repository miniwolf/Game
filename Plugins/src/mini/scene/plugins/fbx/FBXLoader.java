package mini.scene.plugins.fbx;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoader;
import mini.asset.AssetManager;
import mini.math.Matrix4f;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.plugins.fbx.anim.FBXAnimLayer;
import mini.scene.plugins.fbx.anim.FBXAnimStack;
import mini.scene.plugins.fbx.anim.FBXBindPose;
import mini.scene.plugins.fbx.connections.FBXConnectionLoader;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXFile;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.file.FBXReader;
import mini.scene.plugins.fbx.node.FBXNode;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.scene.plugins.fbx.objects.FBXObjectLoader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FBXLoader implements AssetLoader {
    private AssetManager assetManager;
    private FBXReader reader = new FBXReader();
    private Node sceneNode;
    private AssetKey key;
    private Map<FBXId, FBXObject> objects = null;
    private List<FBXAnimStack> animStacks;
    private List<FBXBindPose> bindPoses;

    @Override
    public Object load(AssetInfo info) throws IOException {
        this.assetManager = info.getManager();
        key = info.getKey();

        // Load the data from the stream
        loadScene(info);

        // Bind poses are needed to compute world transforms.
        applyBindPoses();

        updateWorldTransforms();

        // Create the scene graph from the FBX scene graph.
        Spatial scene = constructSceneGraph();

        constructAnimations();

        return scene;
    }

    private void constructAnimations() {
        for (FBXAnimStack animStack : animStacks) {
            FBXAnimLayer layer = animStack.getLayer();
        }
    }

    private void updateWorldTransforms() {
        FBXNode fbxRoot = (FBXNode) objects.get(FBXId.ROOT);
        fbxRoot.updateWorldTransforms(null, null);
    }

    private void loadScene(AssetInfo info) throws IOException {
        FBXFile scene = reader.readFBX(info);
        for (FBXElement fbxElement : scene.getElements()) {
            switch (fbxElement.name) {
                case "Objects":
                    loadObjects(fbxElement);
                    break;
                case "Connections":
                    loadConnections(fbxElement);
                    break;
                case "Takes":
                    loadTakes(fbxElement);
                    break;
                default:
                    System.out.println("Skipped elements: " + fbxElement.name);
            }
        }
    }

    private void applyBindPoses() {
        for (FBXBindPose bindPose : bindPoses) {
            Map<FBXId, Matrix4f> bindPoseData = bindPose.getImplObject();
            for (Map.Entry<FBXId, Matrix4f> entry : bindPoseData.entrySet()) {
                FBXObject obj = objects.get(entry.getKey());
                if (obj instanceof FBXNode) {
                    FBXNode node = (FBXNode) obj;
                    node.setWorldBindPose(entry.getValue());
                } else {
                    System.err.println("Bind poses can only be applied to FBX nodes");
                }
            }
        }
    }

    private Spatial constructSceneGraph() {
        // Acquire the implicit root object.
        FBXNode rootNode = (FBXNode) objects.get(FBXId.ROOT);

        // Convert it into a scene
        Node root = (Node) FBXNode.createScene(rootNode);

        root.setName("FBX-scene");

        return root;
    }

    private void loadConnections(FBXElement fbxElement) {
        FBXConnectionLoader loader = new FBXConnectionLoader(objects);
        loader.load(fbxElement);
        //new FBXConnector(objects).connectFBXElements(load);
    }

    private void loadObjects(FBXElement fbxElement) {
        FBXObjectLoader loader = new FBXObjectLoader(assetManager, key);
        loader.load(fbxElement);
        objects = loader.getObjectMap();
        bindPoses = loader.getBindPoses();
        animStacks = loader.getAnimStacks();
    }

    private void loadTakes(FBXElement fbxElement) {
        FBXTakeLoader fbxTakeLoader = new FBXTakeLoader(assetManager, key);
        fbxTakeLoader.load(fbxElement);
    }
}
