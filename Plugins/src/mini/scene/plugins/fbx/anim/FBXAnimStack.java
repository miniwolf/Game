package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXAnimStack extends FBXObject {
    private float duration;
    private FBXAnimLayer layer0;

    public FBXAnimStack(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    public void fromElementOverride(FBXElement element) {
        for (FBXElement fbxElement : element.getFBXProperties()) {
            String propName = (String) fbxElement.getProperties().get(0);
            if (propName.equals("LocalStop")) {
                long durationLong = (long) fbxElement.getProperties().get(4);
                duration = (float) (durationLong * FBXAnimUtil.SECONDS_PER_UNIT);
            }
        }
    }

    @Override
    protected Object toImplObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(FBXObject obj) {
        if (obj instanceof FBXAnimLayer) {
            layer0 = (FBXAnimLayer) obj;
        } else {
            unsupportedConnectObject(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }
}
