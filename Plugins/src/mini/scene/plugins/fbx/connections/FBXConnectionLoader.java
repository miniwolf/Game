package mini.scene.plugins.fbx.connections;

import mini.scene.plugins.fbx.FBXElementLoader;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Having separate map per class or make data class that is outputted containing the different formats
public class FBXConnectionLoader implements FBXElementLoader<Void> {
    private Map<FBXId, FBXObject> objects;

    public FBXConnectionLoader(Map<FBXId, FBXObject> objects) {
        this.objects = objects;
    }

    @Override
    public Void load(FBXElement element) {
        List<FBXElement> OOConnections =
                element.getChildren().stream()
                       .filter(fbxElement -> fbxElement.getProperties().get(0).equals("OO"))
                       .collect(Collectors.toList());
        List<FBXElement> OPConnections =
                element.getChildren().stream()
                       .filter(fbxElement -> fbxElement.getProperties().get(0).equals("OP"))
                       .collect(Collectors.toList());
        LinkOOConnections(OOConnections);
        LinkOPConnections(OPConnections);
        return null;
    }

    private void LinkOPConnections(List<FBXElement> opConnections) {
        for (FBXElement fbxElement : opConnections) {
            FBXId childId = FBXId.create(fbxElement.getProperties().get(1));
            FBXId parentId = FBXId.create(fbxElement.getProperties().get(2));
            String propertyName = (String) fbxElement.getProperties().get(3);
            FBXObject child = objects.get(childId);
            FBXObject parent = objects.get(parentId);
            if (parent != null) {
                parent.link(child, propertyName);
            }
        }
    }

    private void LinkOOConnections(List<FBXElement> ooConnections) {
        for (FBXElement fbxElement : ooConnections) {
            FBXId childId = FBXId.create(fbxElement.getProperties().get(1));
            FBXId parentId = FBXId.create(fbxElement.getProperties().get(2));
            FBXObject child = objects.get(childId);
            if (child == null) {
                continue;
            }

            FBXObject parent = parentId.isNull() ? objects.get(FBXId.ROOT) : objects.get(parentId);

            if (parent == null) {
                System.err.println("Cheating and attaching to root element");
                parent = objects.get(FBXId.ROOT);
//                System.err.println("Cannot find parent object ID \"" + parentId + "\"");
//                continue;
            }
            parent.link(child);
        }
    }
}
