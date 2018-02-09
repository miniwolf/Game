package mini.scene.plugins.fbx.node;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.FastMath;
import mini.math.Matrix4f;
import mini.math.Quaternion;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.plugins.fbx.anim.FBXLimbNode;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.material.FBXMaterial;
import mini.scene.plugins.fbx.mesh.FBXMesh;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.scene.plugins.fbx.utils.RotationOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FBXNode extends FBXObject<Spatial> {
    protected final Transform localNodeTransform = new Transform();
    protected final Transform worldNodeTransform = new Transform();
    private Matrix4f cachedWorldBindPose;

    private Node node;
    private RenderState.FaceCullMode cullMode;
    private FBXNode parent;
    private FBXMesh mesh;
    private List<FBXNode> children = new ArrayList<>();
    private List<FBXMaterial> materials = new ArrayList<>();
    private FBXNodeAttribute nodeAttribute;
    private Transform miniWorldBindPose;
    private Transform miniLocalBindPose;
    private InheritMode inheritMode = InheritMode.ScaleAfterChildRotation;

    @Override
    protected Spatial toImplObject() {
        Spatial spatial;

        if (nodeAttribute instanceof FBXMesh) {
            spatial = nodeFromMesh((FBXMesh) nodeAttribute);
        } else {
            if (nodeAttribute != null) {
                // Just specifies that this is a "null" node.
                nodeAttribute.getImplObject();
            }

            // TODO: handle other node attribute types.
            //       right now everything we don't know about gets converted
            //       to jME3 Node.
            spatial = new Node(getName() + "-node");
        }

        if (!children.isEmpty()) {
            // Check uniform scale.
            // Although, if inheritType is 0 (eInheritRrSs)
            // it might not be a problem.
            Vector3f localScale = localNodeTransform.getScale();
            if (!FastMath.approximateEquals(localScale.x, localScale.y) ||
                !FastMath.approximateEquals(localScale.x, localScale.z)) {
                System.err.println("Non-uniform scale detected on parent node. "
                                   + "The model may appear distorted.");
            }
        }

        spatial.setLocalTransform(localNodeTransform);

        // TODO: take care of userdata, should be added to the spatial

        return spatial;
    }

    public FBXNode(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    public static Spatial createScene(FBXNode rootNode) {
        Spatial spatial = rootNode.getImplObject();

        if (spatial instanceof Node) {
            attachChildren((Node) spatial, rootNode);
        }

        return spatial;
    }

    private static void attachChildren(Node node, FBXNode rootNode) {
        for (FBXNode fbxChild : rootNode.children) {
            if (fbxChild instanceof FBXLimbNode) {
                continue;
            }

            createScene(fbxChild);
            Spatial child = fbxChild.getImplObject();
            node.attachChild(child);
        }
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);
        node = new Node(name);
        FBXTransform FBXTransform = new FBXTransform(element.getFBXProperties()).invoke();

        Transform transform = setupTransform(FBXTransform);
        node.setLocalTransform(transform);

        Optional<FBXElement> culling = element.getChildByName("Culling");
        culling.ifPresent(cullingElement -> {
            if (cullingElement.getProperties().get(0).equals("CullingOff")) {
                cullMode = RenderState.FaceCullMode.Off;
            } else {
                throw new UnsupportedOperationException(
                        "Missing culling: " + cullingElement.getProperties().get(0));
            }
        });

        if (element.getChildByName("Vertices").isPresent()) {
            FBXMesh mesh = new FBXMesh(assetManager, key);
            mesh.fromElement(element);
            link(mesh);
        }
    }

    @Override
    public void link(FBXObject object) {
        if (object.getFullClassName().startsWith("Null:")) {
            return;
        }

        if (object instanceof FBXNode) { // Scene Graph Object
            FBXNode childNode = (FBXNode) object;
            if (childNode.parent != null) {
                throw new IllegalStateException("Cannot attach " + childNode + " to " + this + "."
                                                + " It is already attached to " + childNode.parent);
            }

            childNode.parent = this;
            children.add(childNode);
        } else if (object instanceof FBXNodeAttribute) {
            if (nodeAttribute != null) {
                throw new IllegalStateException("An FBXNodeAttribute (" + nodeAttribute + ") is "
                                                + "already attached to " + this + ". Only one "
                                                + "attribute allowed per node.");
            }
            if (object instanceof FBXMesh) {
                if (((FBXMesh) object).getVertices() == null) {
                    System.err.println(
                            "This might be wrong, but we are removing an empty mesh child"); // TODO: Verify this by documentation
                    return;
                }
            }

            nodeAttribute = (FBXNodeAttribute) object;
            if (nodeAttribute instanceof FBXNullAttribute) {
                nodeAttribute.getImplObject();
            }
        } else if (object instanceof FBXMaterial) {
            materials.add((FBXMaterial) object);
        } else {
            unsupportedConnectObject(object);
        }
    }

    private Spatial nodeFromMesh(FBXMesh fbxMesh) {
        Map<Integer, Mesh> meshes = fbxMesh.getImplObject();

        if (meshes == null || meshes.size() == 0) {
            System.err.println("No meshes could be loaded. Creating empty node.");
            return new Node(getName() + "-node");
        } else {
            // Multiple jME3 geometries required for a single FBXMesh.
            String nodeName;
            if (children.isEmpty()) {
                nodeName = getName() + "-mesh";
            } else {
                nodeName = getName() + "-node";
            }
            Node node = new Node(nodeName);
            boolean singleMesh = meshes.size() == 1;
            for (Map.Entry<Integer, Mesh> meshInfo : meshes.entrySet()) {
                node.attachChild(
                        tryCreateGeometry(meshInfo.getKey(), meshInfo.getValue(), singleMesh));
            }
            return node;
        }
    }

    private Spatial tryCreateGeometry(int materialIndex, Mesh jmeMesh, boolean single) {
        // Map meshes without material indices to material 0.
        if (materialIndex == -1) {
            materialIndex = 0;
        }

        Material mat;
        if (materialIndex >= materials.size()) {
            // Material index does not exist. Create default material.
            mat = new Material(assetManager, "MatDefs/Light/Lighting.j3md");
            mat.setReceivesShadows(true);
        } else {
            FBXMaterial fbxMat = materials.get(materialIndex);
            mat = fbxMat.getImplObject();
        }

        String geomName = getName();
        if (single) {
            geomName += "-submesh";
        } else {
            geomName += "-mat-" + materialIndex + "-submesh";
        }
        Spatial spatial = new Geometry(geomName, jmeMesh);
        spatial.setMaterial(mat);
        if (mat.isTransparent()) {
            spatial.setQueueBucket(RenderQueue.Bucket.Transparent);
        }
        if (mat.isReceivesShadows()) {
            spatial.setShadowMode(RenderQueue.ShadowMode.Receive);
        }
        return spatial;
    }

    private Transform setupTransform(FBXTransform FBXTransform) {
        Matrix4f transformMatrix = new Matrix4f();
        Vector3f translationLocalRaw = FBXTransform.getTranslationLocalRaw();
        transformMatrix.setTranslation(translationLocalRaw.x, translationLocalRaw.y,
                                       translationLocalRaw.z);

        Vector3f rotationLocalRaw = FBXTransform.getRotationLocalRaw();
        boolean rotationActive = FBXTransform.isRotationActive();
        if (rotationActive) {
            Quaternion postRotation = new Quaternion();
            Quaternion localRotation = RotationOrder
                    .rotate(rotationLocalRaw.x, rotationLocalRaw.y, rotationLocalRaw.z);
            Vector3f rotationPreRaw = FBXTransform.getRotationPreRaw();
            Quaternion preRotation = RotationOrder
                    .rotate(rotationPreRaw.x, rotationPreRaw.y, rotationPreRaw.z);
            postRotation.multLocal(localRotation).multLocal(preRotation);
            transformMatrix.multLocal(postRotation);
        } else {
            transformMatrix.multLocal(RotationOrder.rotate(rotationLocalRaw.x, rotationLocalRaw.y,
                                                           rotationLocalRaw.z));
        }

        Vector3f scaleLocalRaw = FBXTransform.getScaleLocalRaw();
        if (!scaleLocalRaw.equals(Vector3f.ZERO)) {
            transformMatrix.scale(scaleLocalRaw);
        }

        return new Transform(transformMatrix.toTranslationVector(),
                             transformMatrix.toRotationQuat(),
                             transformMatrix.toScaleVector());
    }

    public Node getNode() {
        return node;
    }

    public void setWorldBindPose(Matrix4f worldBindPose) {
        if (cachedWorldBindPose != null && !cachedWorldBindPose.equals(worldBindPose)) {
            throw new UnsupportedOperationException("Bind poses doesn't match");
        }

        cachedWorldBindPose = worldBindPose;

        miniWorldBindPose = new Transform();
        miniWorldBindPose.setTranslation(worldBindPose.toTranslationVector());
        miniWorldBindPose.setRotation(worldBindPose.toRotationQuat());
        miniWorldBindPose.setScale(worldBindPose.toScaleVector());

        System.out.println("\tBind Pose for " + getName());
        System.out.println(miniWorldBindPose);

        float[] angles = new float[3];
        miniWorldBindPose.getRotation().toAngles(angles);
        System.out.println("Angles " + angles[0] * FastMath.RAD_TO_DEG + ", " +
                           angles[1] * FastMath.RAD_TO_DEG + ", " +
                           angles[2] * FastMath.RAD_TO_DEG);
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        System.out.println("Did not implement FBXNode: " + obj);
    }

    public void updateWorldTransforms(Transform miniParentNodeTransform, Transform parentBindPose) {
        Transform fbxLocalTransform = computeFBXLocalTransform();
        localNodeTransform.set(fbxLocalTransform);
        worldNodeTransform.set(localNodeTransform);

        if (miniParentNodeTransform != null) {
            miniParentNodeTransform = miniParentNodeTransform.clone();
            switch (inheritMode) {
                case ScaleAfterChildRotation:
                    worldNodeTransform.combineWithParent(miniParentNodeTransform);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        if (miniWorldBindPose != null) {
            miniLocalBindPose = new Transform();

            // Need to derive local bind pose from world bind pose (this is to be expected for FBX limbs)
            miniLocalBindPose.set(miniWorldBindPose);
            miniLocalBindPose.combineWithParent(parentBindPose.invert());

            // Check miniLocalBindPose against localNodeTransform should be the same...
            // Check miniWorldBindPose against localWorldTransform should be the same...
        } else {
            miniLocalBindPose = new Transform();
            miniWorldBindPose = new Transform();

            miniLocalBindPose.set(localNodeTransform);
            if (parentBindPose != null) {
                miniWorldBindPose.set(localNodeTransform);
                miniWorldBindPose.combineWithParent(parentBindPose);
            } else {
                miniWorldBindPose.set(worldNodeTransform);
            }
        }

        for (FBXNode child : children) {
            child.updateWorldTransforms(worldNodeTransform, miniWorldBindPose);
        }
    }

    private Transform computeFBXLocalTransform() { // TODO: Can these be used directly, as the reference is not touched. Instantiating is expensive
        // TODO: Actual algorithm:
        // Render Local Translation =
        //      Inv Scale Pivot * Lcl Scale * Scale Pivot * Scale Offset * Inv Rota Pivot * Post Rotation * Rotation * Pre Rotation * Rotation Pivot * Rotation Offset * Translation

        Matrix4f scaleMatrix = new Matrix4f();
        scaleMatrix.setScale(localNodeTransform.getScale());

        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.setRotationQuaternion(localNodeTransform.getRotation());

        Matrix4f translationMatrix = new Matrix4f();
        translationMatrix.setTranslation(localNodeTransform.getTranslation());

        Matrix4f result = new Matrix4f();
        result.multLocal(scaleMatrix).multLocal(rotationMatrix).multLocal(translationMatrix);

        Transform transform = new Transform();
        transform.fromTransformMatrix(result);

        return transform;
    }

    private enum InheritMode {
        /**
         * Apply parent scale after child rotation. This is the only correctly supported by this engine.
         */
        ScaleAfterChildRotation,
    }

    private class FBXTransform {
        private List<FBXElement> properties;
        private Vector3f rotationPreRaw = new Vector3f();
        private Vector3f rotationLocalRaw = new Vector3f();
        private Vector3f translationLocalRaw = new Vector3f();
        private Vector3f scaleLocalRaw = new Vector3f();
        private boolean rotationActive;

        public FBXTransform(List<FBXElement> properties) {
            this.properties = properties;
        }

        public Vector3f getRotationPreRaw() {
            return rotationPreRaw;
        }

        public Vector3f getRotationLocalRaw() {
            return rotationLocalRaw;
        }

        public Vector3f getTranslationLocalRaw() {
            return translationLocalRaw;
        }

        public Vector3f getScaleLocalRaw() {
            return scaleLocalRaw;
        }

        public boolean isRotationActive() {
            return rotationActive;
        }

        public FBXTransform invoke() {
            for (FBXElement element : properties) {
                String propertyName = (String) element.getProperties().get(0);
                switch (propertyName) {
                    case "RotationActive":
                        rotationActive = ((Integer) element.getProperties().get(4)) == 1;
                        break;
                    case "PreRotation":
                        rotationPreRaw = readVectorFromProperty(element);
                        break;
                    case "Lcl Rotation":
                        rotationLocalRaw = readVectorFromProperty(element);
                        break;
                    case "Lcl Translation":
                        translationLocalRaw = readVectorFromProperty(element);
                        break;
                    case "Lcl Scaling":
                        scaleLocalRaw = readVectorFromProperty(element);
                        break;
                    case "InheritType":
                        int inheritType = (int) element.getProperties().get(4);
                        inheritMode = InheritMode.values()[inheritType];
                        break;
                }
            }
            return this;
        }
    }
}
