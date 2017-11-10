package mini.scene.plugins.fbx.node;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXNullAttribute extends FBXNodeAttribute {
    public FBXNullAttribute(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected Object toImplObject() {
        return new Object();
    }

    @Override
    public void link(FBXObject object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        throw new UnsupportedOperationException();
    }
}
