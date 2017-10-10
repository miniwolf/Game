package mini.scene.plugins.fbx.file;

import java.util.ArrayList;
import java.util.List;

public class FBXElement {
    private String name;
    private List<Object> properties = new ArrayList<>();
    private final char[] propertyTypes;
    private List<FBXElement> children = new ArrayList<>();

    public FBXElement(int numProperties) {
        propertyTypes = new char[numProperties];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addProperty(Object obj) {
        properties.add(obj);
    }

    public List<Object> getProperties() {
        return properties;
    }

    public void addChild(FBXElement element) {
        children.add(element);

    }

    public void addPropertyType(char dataType, int index) {
        propertyTypes[index] = dataType;
    }

    public List<FBXElement> getChildren() {
        return children;
    }

    public char[] getPropertyTypes() {
        return propertyTypes;
    }
}
