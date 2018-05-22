package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXCluster extends FBXObject {
    private FBXLimbNode limb;
    private Integer[] indexes;
    private Double[] weights;

    public FBXCluster(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
//        for (FBXElement fbxElement : element.getChildren()) {
//            if (fbxElement.name.equals("Indexes")) {
//                if (fbxElement.getProperties().size() > 0) {
//                    indexes = fbxElement.getProperties().toArray(new Integer[0]);
//                }
//            } else if (fbxElement.name.equals("Weights")) {
//                if (fbxElement.getProperties().size() > 0) {
//                    weights = fbxElement.getProperties().toArray(new Double[0]);
//                }
//            }
//        }
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

    public FBXLimbNode getLimb() {
        return limb;
    }

    public Integer[] getIndexes() {
        return indexes;
    }

    public Double[] getWeights() {
        return weights;
    }
}
