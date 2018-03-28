package mini.scene.plugins.fbx;

import mini.animation.Animation;
import mini.animation.AnimationControl;
import mini.animation.SpatialTrack;
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
import mini.scene.plugins.fbx.anim.FBXTake;
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
import java.util.Optional;

public class FBXLoader implements AssetLoader {
    private AssetManager assetManager;
    private FBXReader reader = new FBXReader();
    private Node sceneNode;
    private AssetKey key;
    private Map<FBXId, FBXObject> objects = null;
    private List<FBXAnimStack> animStacks;
    private List<FBXBindPose> bindPoses;
    private FBXTake take;
    private String animationName;

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

        // Create animation
        attachAnimation(rootNode, take.getTracks());

        // Convert it into a scene
        Node root = (Node) FBXNode.createScene(rootNode);
        root.setName("FBX-scene");

        return root;
    }

    private void attachAnimation(FBXNode rootNode, Map<String, SpatialTrack> tracks) {
        if (tracks == null) {
            return;
        }
        for (Map.Entry<String, SpatialTrack> entry : tracks.entrySet()) {
            String name = entry.getKey().split("Model::")[1];

            FBXNode matchingChild = findName(name, rootNode);
            if (matchingChild == null) {
                throw new IllegalStateException("Could not find match for: " + name);
            }
            var animation = new Animation(animationName, take.getTime());
            animation.setTracks(new SpatialTrack[] {entry.getValue()});

            // Create spatial animation control
            AnimationControl animationControl = new AnimationControl();
            animationControl.addAnimation(animation);
            matchingChild.setAnimationController(animationControl);
        }
    }

    private FBXNode findName(String name, FBXNode rootNode) {
        if (name.equals(rootNode.getClassName())) {
            return rootNode;
        }
        for (FBXNode fbxNode : rootNode.getChildren()) {
            FBXNode tryGet = findName(name, fbxNode);
            if (tryGet != null) return tryGet;
        }
        return null;
    }

    private void loadConnections(FBXElement fbxElement) {
        FBXConnectionLoader loader = new FBXConnectionLoader(objects);
        loader.load(fbxElement);
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
        animationName = fbxTakeLoader.getTakeName();
        take = fbxTakeLoader.load(fbxElement);
    }
}
