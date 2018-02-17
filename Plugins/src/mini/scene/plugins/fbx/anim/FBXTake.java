package mini.scene.plugins.fbx.anim;

import mini.animation.SpatialTrack;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXTake extends FBXObject<SpatialTrack> {
    private String name;

    public FBXTake(AssetManager assetManager, AssetKey key, String name) {
        super(assetManager, key);
        this.name = name;
    }

    @Override
    protected void fromElementOverride(FBXElement element) {

    }

    @Override
    protected SpatialTrack toImplObject() {
        return null;
    }

    @Override
    public void link(FBXObject obj) {

    }

    @Override
    public void link(FBXObject obj, String propertyName) {

    }
}
