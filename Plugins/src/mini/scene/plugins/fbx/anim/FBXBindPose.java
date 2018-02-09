package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.Matrix4f;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.Map;

public class FBXBindPose extends FBXObject<Map<FBXId, Matrix4f>> {
    public FBXBindPose(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected Map<FBXId, Matrix4f> toImplObject() {
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
