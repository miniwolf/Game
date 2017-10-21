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
import mini.scene.plugins.fbx.mesh.FBXMesh;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.scene.plugins.fbx.objects.FBXMaterial;
import mini.scene.plugins.fbx.utils.RotationOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FBXNode extends FBXObject<Spatial> {
    protected final Transform localNodeTransformScale = new Transform();

    private Node node;
    private RenderState.FaceCullMode cullMode;
    private FBXNode parent;
    private FBXMesh mesh;
    private List<FBXNode> children = new ArrayList<>();
    private List<FBXMaterial> materials = new ArrayList<>();
    private FBXNodeAttribute nodeAttribute;

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
    }

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
            Vector3f localScale = localNodeTransformScale.getScale();
            if (!FastMath.approximateEquals(localScale.x, localScale.y) ||
                !FastMath.approximateEquals(localScale.x, localScale.z)) {
                System.err.println("Non-uniform scale detected on parent node. "
                                   + "The model may appear distorted.");
            }
        }

        spatial.setLocalTransform(localNodeTransformScale);
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

    @Override
    public void link(FBXObject object) {
        if (object instanceof FBXNode) { // Scene Graph Object
            FBXNode childNode = (FBXNode) object;
            if (childNode.parent != null) {
                throw new IllegalStateException("Cannot attach " + childNode + " to " + this + "."
                                                + " It is already attached to " + childNode.parent);
            }

            childNode.parent = this;
            children.add(childNode);
        } else if (object instanceof FBXMesh) {
            if (mesh != null) {
                throw new IllegalStateException("A mesh (" + mesh + ") is already attached to " +
                                                this + ". Only one attribute allowed per node.");
            }

            mesh = (FBXMesh) object;
        } else if (object instanceof FBXMaterial) {
            materials.add((FBXMaterial) object);
        } else if (object instanceof FBXNodeAttribute) {
            if (nodeAttribute != null) {
                throw new IllegalStateException("An FBXNodeAttribute (" + nodeAttribute + ") is "
                                                + "already attached to " + this + ". Only one "
                                                + "attribute allowed per node.");
            }

            nodeAttribute = (FBXNodeAttribute) object;
            if (nodeAttribute instanceof FBXNullAttribute) {
                nodeAttribute.getImplObject();
            }
        } else {
            System.out.println(object);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        System.out.println("Did not implement FBXNode: " + obj);
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
                }
            }
            return this;
        }
    }
}
