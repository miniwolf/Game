package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXAnimLayer extends FBXObject {
    public FBXAnimLayer(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected Object toImplObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(FBXObject obj) {
        unsupportedConnectObject(obj);
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }
}
