package mini.scene.plugins.fbx.objects;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.FBXElementLoader;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.node.FBXRootNode;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.scene.plugins.fbx.obj.FBXObjectFactory;

import java.util.HashMap;
import java.util.Map;

public class FBXObjectLoader implements FBXElementLoader<Map<FBXId, FBXObject>> {
    private Map<FBXId, FBXObject> objectMap = new HashMap<>();
    private AssetManager assetManager;
    private AssetKey key;

    public FBXObjectLoader(AssetManager assetManager, AssetKey key) {
        this.assetManager = assetManager;
        this.key = key;
    }

    @Override
    public Map<FBXId, FBXObject> load(FBXElement element) {
        objectMap.put(FBXId.ROOT, new FBXRootNode(assetManager, key));

        for (FBXElement fbxElement : element.getChildren()) {
            FBXObject object = FBXObjectFactory.createObject(fbxElement, assetManager, key);
            if (objectMap.containsKey(object.getId())) {
                System.err.println("An object with ID \"" + object.getId() + "\" has already been "
                                   + "defined. Overwriting previous");
            }

            objectMap.put(object.getId(), object);
            // Todo: animation stuff here
        }

        return objectMap;
    }
}
