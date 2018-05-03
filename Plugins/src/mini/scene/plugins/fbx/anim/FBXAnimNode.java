package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXAnimNode extends FBXObject {
    private String name;
    private FBXAnimCurveNode translationCurve;
    private FBXAnimCurveNode rotationCurve;
    private FBXAnimCurveNode scaleCurve;

    public FBXAnimNode(AssetManager assetManager, AssetKey key, String name) {
        super(assetManager, key);
        this.name = name;
    }

    protected FBXAnimNode(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement fbxElement) {

    }

    @Override
    protected Object toImplObject() {
        return null;
    }

    @Override
    public void link(FBXObject obj) {

    }

    @Override
    public void link(FBXObject obj, String propertyName) {

    }
}
