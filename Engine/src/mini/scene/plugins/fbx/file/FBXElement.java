package mini.scene.plugins.fbx.file;

import java.util.ArrayList;
import java.util.List;

public class FBXElement {
    private String name;
    private List<Object> properties = new ArrayList<>();

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
}
