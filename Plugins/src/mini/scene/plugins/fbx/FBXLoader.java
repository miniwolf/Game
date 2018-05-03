package mini.scene.plugins.fbx;

import mini.animation.Animation;
import mini.animation.AnimationControl;
import mini.animation.BoneTrack;
import mini.animation.Track;
import mini.animation.presets.HumanoidPreset;
import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoader;
import mini.asset.AssetManager;
import mini.math.Matrix4f;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.plugins.fbx.anim.FBXAnimCurveNode;
import mini.scene.plugins.fbx.anim.FBXAnimLayer;
import mini.scene.plugins.fbx.anim.FBXAnimStack;
import mini.scene.plugins.fbx.anim.FBXBindPose;
import mini.scene.plugins.fbx.anim.FBXLimbNode;
import mini.scene.plugins.fbx.connections.FBXConnectionLoader;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXFile;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.file.FBXReader;
import mini.scene.plugins.fbx.node.FBXNode;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.scene.plugins.fbx.objects.FBXObjectLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBXLoader implements AssetLoader {
    private AssetManager assetManager;
    private FBXReader reader = new FBXReader();
    private AssetKey key;
    private Map<FBXId, FBXObject> objects = null;
    private List<FBXAnimStack> animStacks;
    private List<FBXBindPose> bindPoses;
    private String animationName;

    @Override
    public Object load(AssetInfo info) throws IOException {
        this.assetManager = info.getManager();
        key = info.getKey();
        loadScene(info);

        // Bind poses are needed to compute world transforms.
        applyBindPoses();

        updateWorldTransforms();

        constructSkeleton();

        Node node = constructSceneGraph();

        constructAnimations();

        return node;
    }

    private void constructSkeleton() {
        FBXNode fbxRoot = (FBXNode) objects.get(FBXId.ROOT);
        FBXSkeletonCreator.createSkeletons(fbxRoot);
        HumanoidPreset humanoidPreset = new HumanoidPreset();
        humanoidPreset.setupBonesForPreset(fbxRoot.getSkeleton());
        fbxRoot.setPreset(humanoidPreset);
    }

    private void constructAnimations() {
        Map<FBXTrack, FBXTrack> pairs = new HashMap<>();
        for (FBXAnimStack stack : animStacks) {
            for (FBXAnimLayer layer : stack.getLayer()) {
                for (FBXAnimCurveNode curveNode : layer.getAnimationCurveNodes()) {
                    FBXTrack fbxTrack = new FBXTrack();
                    fbxTrack.animStack = stack;
                    fbxTrack.animLayer = layer;
                    var first = objects.keySet().stream()
                                       .filter(fbxId -> fbxId.equals(FBXId.create(layer.getName())))
                                       .findFirst();
                    if (!first.isPresent()) {
                        throw new IllegalStateException(
                                "Could not find matching node for " + layer.getName());
                    }
                    fbxTrack.node = (FBXNode) objects.get(first.get());

                    FBXTrack storedTrack = pairs.get(fbxTrack);
                    if (storedTrack == null) {
                        storedTrack = fbxTrack;
                        pairs.put(storedTrack, storedTrack);
                    }

                    storedTrack.animCurves.add(curveNode);
                }
            }
        }
        HumanoidPreset preset = ((FBXNode) objects.get(FBXId.ROOT)).getPreset();

        for (FBXTrack pair : pairs.values()) {
            String animationName = pair.animStack.getName();
            float duration = pair.animStack.getDuration();

            if (pair.node instanceof FBXLimbNode) {
                var limbNode = (FBXLimbNode) pair.node;
                var bone = limbNode.getBone();
                var spatial = limbNode.getSkeletonHolder().getImplObject();
                var skeleton = limbNode.getSkeletonHolder().getSkeleton();

                var animationControl = spatial.getControl(AnimationControl.class);
                if (animationControl.getSkeleton() != skeleton) {
                    throw new UnsupportedOperationException();
                }

                var animation = animationControl.getAnimation(animationName);
                if (animation == null) {
                    animation = new Animation(animationName, duration);
                    animationControl.addAnimation(animation);
                }

                BoneTrack boneTrack = pair
                        .toBoneTrack(preset.getJointName(bone), skeleton.getBoneIndex(bone),
                                     bone.getBindInverseTransform());

                animation.addTrack(boneTrack);
            } else {
                Animation anim = new Animation(animationName, duration);
                anim.setTracks(new Track[]{pair.toSpatialTrack()});

                Spatial spatial = pair.node.getImplObject();
                AnimationControl animationControl = spatial.getControl(AnimationControl.class);

                if (animationControl == null) {
                    animationControl = new AnimationControl(null);
                    spatial.addControl(animationControl);
                }

                animationControl.addAnimation(anim);
            }
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

    private Node constructSceneGraph() {
        // Acquire the implicit root object.
        FBXNode rootNode = (FBXNode) objects.get(FBXId.ROOT);

        // Convert it into a scene
        Node root = FBXNode.createScene(rootNode);
        root.setName("FBX-scene");

        return root;
    }

//    private void attachAnimation(FBXNode rootNode, Map<String, SpatialTrack> tracks) {
//        if (tracks == null || tracks.isEmpty()) {
//            return;
//        }
//        for (Map.Entry<String, SpatialTrack> entry : tracks.entrySet()) {
//            String name = entry.getKey().split("Model::")[1];
//
//            FBXNode matchingChild = findName(name, rootNode);
//            if (matchingChild == null) {
//                throw new IllegalStateException("Could not find match for: " + name);
//            }
//            var animation = new Animation(animationName, take.getTime());
//            animation.setTracks(new SpatialTrack[] {entry.getValue()});
//
//            // Create spatial animation control
//            AnimationControl animationControl = new AnimationControl();
//            animationControl.addAnimation(animation);
//            matchingChild.setAnimationController(animationControl);
//        }
//    }

    private FBXNode findName(String name, FBXNode rootNode) {
        if (name.equals(rootNode.getClassName())) {
            return rootNode;
        }
        for (FBXNode fbxNode : rootNode.getChildren()) {
            FBXNode tryGet = findName(name, fbxNode);
            if (tryGet != null) {
                return tryGet;
            }
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
        animStacks = fbxTakeLoader.load(fbxElement); // TODO: Use result
        animationName = fbxTakeLoader.getTakeName();
    }
}
