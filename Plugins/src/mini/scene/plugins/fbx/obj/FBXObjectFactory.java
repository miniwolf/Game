package mini.scene.plugins.fbx.obj;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.anim.FBXAnimLayer;
import mini.scene.plugins.fbx.anim.FBXAnimStack;
import mini.scene.plugins.fbx.anim.FBXBindPose;
import mini.scene.plugins.fbx.anim.FBXCluster;
import mini.scene.plugins.fbx.anim.FBXLimbNode;
import mini.scene.plugins.fbx.anim.FBXSkinDeformer;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.material.FBXImage;
import mini.scene.plugins.fbx.material.FBXMaterial;
import mini.scene.plugins.fbx.mesh.FBXMesh;
import mini.scene.plugins.fbx.node.FBXNode;
import mini.scene.plugins.fbx.node.FBXNullAttribute;
import mini.scene.plugins.fbx.objects.FBXTexture;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FBXObjectFactory {
    public static FBXObject createObject(FBXElement fbxElement, AssetManager assetManager,
                                         AssetKey assetKey) {
        String elementName = fbxElement.getName();
        String subclassName;

        if (fbxElement.getPropertyTypes().length == 3) {
            // FBX 7.x (all objects start with Long ID)
            subclassName = (String) fbxElement.getProperties().get(2);
        } else if (fbxElement.getPropertyTypes().length == 2) {
            // FBX 6.x (objects only have name and subclass)
            subclassName = (String) fbxElement.getProperties().get(1);
        } else {
            //"Unknown object or invalid data"
            return null;
        }

        Class<? extends FBXObject> javaFBXClass = getImplementingClass(elementName, subclassName);

        try {
            // This object is supported by FBX importer, create new instance.
            // Import the data into the object from the element, then return it.
            Constructor<? extends FBXObject> ctor = javaFBXClass
                    .getConstructor(AssetManager.class, AssetKey.class);
            FBXObject fbxObject = ctor.newInstance(assetManager, assetKey);
            fbxObject.fromElement(fbxElement);

            if ("Null:Model".equals(fbxObject.getFullClassName())) {
                fbxObject.id = FBXId.ROOT;
            }
            if (elementName.startsWith("MotionBuilder_")) { // TODO: This is horrible
                return fbxObject;
            }

            String subClassName = elementName + ", " + subclassName;
            if (fbxObject.getAssetManager() == null) {
                throw new IllegalStateException("FBXObject subclass (" + subClassName + ") forgot" +
                                                " to call super() in their constructor");
            } else if (fbxObject.getClassName() == null) {
                throw new IllegalStateException("FBXObject subclass (" + subClassName + ") forgot" +
                                                " to call super.fromElement in their constructor");
            }
            return fbxObject;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException(); // Programmer error
        }
    }

    private static Class<? extends FBXObject> getImplementingClass(String elementName,
                                                                   String subclassName) {
        if ("NodeAttribute".equals(elementName)) {
            if ("LimbNode".equals(subclassName)) {
                return FBXNullAttribute.class;
            } else if ("Camera".equals(subclassName) || "CameraSwitcher".equals(subclassName)) {
                // TODO: Support cameras
                return FBXNullAttribute.class;
            } else if ("Null".equals(subclassName)) {
                // An "Empty" or "Node" without any specific behaviour
                return FBXNullAttribute.class;
            } else {
                System.err.println(
                        "Warning: unknown object subclass: " + subclassName + ". Ignoring");
                return FBXUnknownObject.class;
            }
        } else if ("Geometry".equals(elementName)) {
            return FBXMesh.class;
        } else if ("Model".equals(elementName)) {
            // Scene Graph Node
            // Determine Specific subclass (e.g. Mesh, LimbNode)
            if ("LimbNode".equals(subclassName)) {
                return FBXLimbNode.class;
            } else {
                return FBXNode.class;
            }
        } else if ("Pose".equals(elementName) && "BindPose".equals(subclassName)) {
            // Bind Pose Information
            return FBXBindPose.class;
        } else if ("Material".equals(elementName)) {
            return FBXMaterial.class;
        } else if ("Deformer".equals(elementName)) {
            if (subclassName.equals("Skin")) {
                // FBXSkinDeformer (Mapping between FBXMesh & FBXClusters)
                return FBXSkinDeformer.class;
            } else if ("Cluster".equals(subclassName)) {
                // Cluster (Mapping between FBXMesh vertices & weights for bone)
                return FBXCluster.class;
            }
        } else if ("Video".equals(elementName) && "Clip".equals(subclassName)) {
            return FBXImage.class;
        } else if ("Texture".equals(elementName)) {
            return FBXTexture.class;
        } else if ("AnimationStack".equals(elementName)) {
            return FBXAnimStack.class;
        } else if ("AnimationLayer".equals(elementName)) {
            // Blended animations
            return FBXAnimLayer.class;
        } else if ("SceneInfo".equals(elementName)) {
            // Old-style FBX 6.1 uses this. Nothing useful here.
            return FBXUnknownObject.class;
        }
        System.err
                .println("Unknown object class or subclass: " + elementName + " : " + subclassName);
        return FBXUnknownObject.class;
    }
}
