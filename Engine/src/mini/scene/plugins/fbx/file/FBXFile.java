package mini.scene.plugins.fbx.file;

import java.util.ArrayList;
import java.util.List;

public class FBXFile {
    private long version;
    private List<FBXElement> elements = new ArrayList<>();

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<FBXElement> getElements() {
        return elements;
    }

    public void addElement(FBXElement fbxElement) {
        elements.add(fbxElement);
    }
}
