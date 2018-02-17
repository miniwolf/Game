package mini.scene.plugins.fbx;

import mini.animation.Animation;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXAnimation extends FBXObject<Animation> {
    private long startTime;
    private long endTime;

    protected FBXAnimation(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
    }

    @Override
    protected Animation toImplObject() {
        return null;
    }

    @Override
    public void link(FBXObject obj) {

    }

    @Override
    public void link(FBXObject obj, String propertyName) {

    }

    public void setTime(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
