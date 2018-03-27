package mini.scene.plugins.fbx.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FBXElement {
    public String name;
    private List<Object> properties = new ArrayList<>();
    private char[] propertyTypes;
    private List<FBXElement> children = new ArrayList<>();
    private boolean legacy;

    public FBXElement(int numProperties) {
        propertyTypes = new char[numProperties];
    }

    public Optional<FBXElement> getChildByName(String name) {
        return children.stream().filter(child -> child.name.equals(name)).findFirst();
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

        return legacy ? getLegacyProperties(propertyElement.get())
                      : getNewStyleProperties(propertyElement.get());
    }

    private List<FBXElement> getNewStyleProperties(FBXElement propertyElement) {
        return propertyElement.children.stream().peek(child -> {
            if (!child.name.equals("P") && !child.name.equals("Property")) {
                throw new UnsupportedOperationException(
                        "Unexpected property name: " + child.name);
            }
        }).collect(Collectors.toList());
    }

    private List<FBXElement> getLegacyProperties(FBXElement propertyElement) {
        return propertyElement.children.stream().peek(child -> {
            if (!child.name.equals("P") && !child.name.equals("Property")) {
                throw new UnsupportedOperationException(
                        "Unexpected property name: " + child.name);
            }
        }).map(child -> {
            char[] types = new char[child.propertyTypes.length + 1];
            types[0] = child.propertyTypes[0];
            types[1] = child.propertyTypes[0];
            System.arraycopy(child.propertyTypes, 1, types, 2, types.length - 2);

            List<Object> values = new ArrayList<>(child.properties);
            values.add(1, values.get(0));

            FBXElement dummyProperty = new FBXElement(types.length);
            dummyProperty.children = child.children;
            dummyProperty.name = child.name;
            dummyProperty.propertyTypes = types;
            dummyProperty.properties = values;
            return dummyProperty;
        }).collect(Collectors.toList());
    }

    private Optional<FBXElement> getPropertyElement() {
        for (FBXElement child : children) {
            if (child.name.equals("Properties70")) {
                return Optional.of(child);
            } else if (child.name.equals("Properties60")) {
                legacy = true;
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }
}
