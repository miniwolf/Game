package mini.scene.plugins.fbx.node;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.obj.FBXObject;

public abstract class FBXNodeAttribute<T> extends FBXObject<T> {
    public FBXNodeAttribute(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }
}
