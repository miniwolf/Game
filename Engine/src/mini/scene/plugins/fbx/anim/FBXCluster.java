package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXCluster extends FBXObject {
    private FBXLimbNode limb;

    public FBXCluster(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected Object toImplObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(FBXObject obj) {
        if (obj instanceof FBXLimbNode) {
            limb = (FBXLimbNode) obj;
        } else {
            unsupportedConnectObject(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }
}
