package mini.scene.plugins.fbx.objects;

import mini.math.Vector3f;
import mini.scene.plugins.fbx.file.FBXElement;

public class FBXObject {
    protected final long id;
    protected final String name;
    protected final String type;
    protected FBXElement element;

    public FBXObject(FBXElement element) {
        this.element = element;
        this.id = (long) element.getProperties().get(0);
        this.name = (String) element.getProperties().get(1);
        this.type = (String) element.getProperties().get(2);
    }

    protected static Vector3f readVectorFromProperty(FBXElement propertyElement) {
        float x = ((Double) propertyElement.getProperties().get(4)).floatValue();
        float y = ((Double) propertyElement.getProperties().get(5)).floatValue();
        float z = ((Double) propertyElement.getProperties().get(6)).floatValue();
        return new Vector3f(x, y, z);
    }

    public FBXElement getElement() {
        return element;
    }
}
