package mini.scene.plugins.fbx.objects;

import mini.material.RenderState;
import mini.math.Matrix4f;
import mini.math.Quaternion;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.scene.Node;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.utils.RotationOrder;

import java.util.List;
import java.util.Optional;

public class FBXNode extends FBXObject {
    private Node node;
    private RenderState.FaceCullMode cullMode;

    public FBXNode(FBXElement element) {
        super(element);
        initializeElement();
    }

    protected void initializeElement() {
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
        transformMatrix.scale(scaleLocalRaw);

        return new Transform(transformMatrix.toTranslationVector(),
                             transformMatrix.toRotationQuat(),
                             transformMatrix.toScaleVector());
    }

    public Node getNode() {
        return node;
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
