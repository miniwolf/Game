package mini.scene.plugins.fbx.connections;

import mini.scene.plugins.fbx.material.FBXImage;
import mini.scene.plugins.fbx.material.FBXMaterial;
import mini.scene.plugins.fbx.material.FBXTexture;
import mini.scene.plugins.fbx.mesh.FBXMesh;
import mini.scene.plugins.fbx.node.FBXNode;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.List;
import java.util.Map;

public class FBXConnector {
    private Map<Long, FBXObject> objects;

    public FBXConnector(Map<Long, FBXObject> objects) {
        this.objects = objects;
    }

    public void connectFBXElements(Map<Class, Map<Long, List<Long>>> load) {
        connectSpecificElements(load.get(FBXImage.class));
        connectSpecificElements(load.get(FBXTexture.class));
        connectSpecificElements(load.get(FBXMaterial.class));
        connectSpecificElements(load.get(FBXMesh.class));
        connectSpecificElements(load.get(FBXNode.class));
    }

    private void connectSpecificElements(Map<Long, List<Long>> refMap) {
        for (Long ref : refMap.keySet()) {
            connectRefToObjects(ref, refMap.get(ref));
        }
    }

    private void connectRefToObjects(Long refId, List<Long> objectIds) {
        FBXObject ref = objects.get(refId);
        for (Long objId : objectIds) {
            FBXObject obj = objects.get(objId);
            ref.link(obj);
        }
    }
}
