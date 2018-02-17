package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.ArrayList;
import java.util.List;

public class FBXSkinDeformer extends FBXObject<List<FBXCluster>> {
    private final List<FBXCluster> clusters = new ArrayList<>();

    public FBXSkinDeformer(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    public void fromElementOverride(FBXElement element) {
    }

    @Override
    protected List<FBXCluster> toImplObject() {
        return clusters;
    }

    @Override
    public void link(FBXObject obj) {
        if (obj instanceof FBXCluster) {
            clusters.add((FBXCluster) obj);
        } else {
            unsupportedConnectObject(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }
}
