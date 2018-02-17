package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXCluster extends FBXObject {
    private FBXLimbNode limb;
    private int[] indexes;
    private double[] weights;

    public FBXCluster(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
        for (FBXElement fbxElement : element.getChildren()) {
            if (fbxElement.getName().equals("Indexes")) {
                indexes = (int[]) fbxElement.getProperties().get(0);
            } else if (fbxElement.getName().equals("Weights")) {
                weights = (double[]) fbxElement.getProperties().get(0);
            }
        }
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
        if (obj == limb) {
            return;
        }

        unsupportedConnectObjectProperty(obj, propertyName);
    }
}
