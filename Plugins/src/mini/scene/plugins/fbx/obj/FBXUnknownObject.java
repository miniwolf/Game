package mini.scene.plugins.fbx.obj;

import mini.asset.AssetKey;
import mini.asset.AssetManager;

public class FBXUnknownObject extends FBXObject<Void> {

    public FBXUnknownObject(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected Void toImplObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(FBXObject obj) {
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
    }
}
