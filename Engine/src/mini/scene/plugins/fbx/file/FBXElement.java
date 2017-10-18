package mini.scene.plugins.fbx.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FBXElement {
    private String name;
    private List<Object> properties = new ArrayList<>();
    private final char[] propertyTypes;
    private List<FBXElement> children = new ArrayList<>();

    public FBXElement(int numProperties) {
        propertyTypes = new char[numProperties];
    }

    public Optional<FBXElement> getChildByName(String name) {
        return children.stream().filter(child -> child.name.equals(name)).findFirst();
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

    public List<FBXElement> getFBXProperties() {
        List<FBXElement> props = new ArrayList<>();

        Optional<FBXElement> propertyElement = getPropertyElement();
        if (!propertyElement.isPresent()) {
            return props;
        }

        return propertyElement.get().children.stream().peek(child -> {
            if (!child.getName().equals("P")) {
                throw new UnsupportedOperationException(
                        "Unexpected property name: " + child.getName());
            }
        }).collect(Collectors.toList());
    }

    private Optional<FBXElement> getPropertyElement() {
        return children.stream().peek(child -> {
            if (child.getName().startsWith("Properties") && !child.getName()
                                                                  .equals("Properties70")) {
                throw new UnsupportedOperationException(
                        "Unexpected PropertyType: " + child.getName());
            }
        }).filter(child -> "Properties70".equals(child.getName())).findFirst();
    }
}
