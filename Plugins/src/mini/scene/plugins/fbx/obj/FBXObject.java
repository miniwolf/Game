package mini.scene.plugins.fbx.obj;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.Vector3f;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXId;

public abstract class FBXObject<T> {
    protected final AssetManager assetManager;
    protected final AssetKey key;
    protected FBXId id;
    protected String subclassName;
    protected String className;
    protected String name;
    private T implObject;

    protected FBXObject(AssetManager assetManager, AssetKey key) {
        this.assetManager = assetManager;
        this.key = key;
    }

    protected static Vector3f readVectorFromProperty(FBXElement propertyElement) {
        float x = ((Double) propertyElement.getProperties().get(4)).floatValue();
        float y = ((Double) propertyElement.getProperties().get(5)).floatValue();
        float z = ((Double) propertyElement.getProperties().get(6)).floatValue();
        return new Vector3f(x, y, z);
    }

    public void fromElement(FBXElement element) {
        id = FBXId.getObjectId(element);
        String nameAndClass;
        if (element.getPropertyTypes().length == 3) {
            nameAndClass = (String) element.getProperties().get(1);
            subclassName = (String) element.getProperties().get(2);
        } else if (element.getPropertyTypes().length == 2) {
            nameAndClass = (String) element.getProperties().get(0);
            subclassName = (String) element.getProperties().get(1);
        } else {
            throw new UnsupportedOperationException(
                    "This is not an FBX Object: " + element.getName());
        }

        int splitter = nameAndClass.indexOf("\u0000\u0001");
        int otherPossibility = nameAndClass.indexOf("::");

        if (splitter != -1) {
            name = nameAndClass.substring(0, splitter);
            className = nameAndClass.substring(splitter + 2);
        } else if (otherPossibility != -1) {
            name = nameAndClass.substring(0, otherPossibility);
            className = nameAndClass.substring(otherPossibility + 2);
        } else {
            name = nameAndClass;
            className = null;
        }
    }

    public FBXId getId() {
        return id;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public String getFullClassName() {
        if (subclassName.equals("")) {
            return className;
        } else {
            return subclassName + ":" + className;
        }
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public final T getImplObject() {
        if (implObject == null) {
            implObject = toImplObject();
            if (implObject == null) {
                throw new UnsupportedOperationException("FBX object subclass failed to resolve to "
                                                        + "an object");
            }
        }
        return implObject;
    }

    protected abstract T toImplObject();

    public abstract void link(FBXObject obj);

    public abstract void link(FBXObject obj, String propertyName);

    protected final void unsupportedConnectObject(FBXObject object) {
        throw new IllegalArgumentException("Cannot attach objects of this class ("
                                           + object.getFullClassName() + ") to " + object
                                                   .getName());
    }

    protected final void unsupportedConnectObjectProperty(FBXObject object, String property) {
        throw new IllegalArgumentException("Cannot attach object of this class ("
                                           + object.getFullClassName() + ") to property "
                                           + getClass().getSimpleName() + "[\"" + property + "\"]");
    }
}
