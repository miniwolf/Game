package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.ArrayList;
import java.util.List;

public class FBXAnimStack extends FBXObject {
    private float duration;
    private List<FBXAnimLayer> layers = new ArrayList<>();

    public FBXAnimStack(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
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
            layers.add((FBXAnimLayer) obj);
        } else {
            unsupportedConnectObject(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }

    public List<FBXAnimLayer> getLayer() {
        return layers;
    }

    public float getDuration() {
        return duration;
    }

    public void fromElementAscii(FBXElement element) {
        name = (String) element.getProperties().get(0);
        for (FBXElement child : element.getChildren()) {
            if (child.name.equals("LocalTime")) {
                duration = (float) (((Long) child.getProperties().get(1))
                                    * FBXAnimUtil.SECONDS_PER_UNIT);
            } else if (child.name.equals("Model")) {
                var animLayer = new FBXAnimLayer(assetManager, key,
                                                 (String) child.getProperties().get(0));
                animLayer.fromElementAscii(child);
                layers.add(animLayer);
            }
        }
    }
}
