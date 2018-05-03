package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.AsciiObject;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.ArrayList;
import java.util.List;

public class FBXAnimLayer extends FBXObject implements AsciiObject {
    private final List<FBXAnimCurveNode> animCurveNodes = new ArrayList<>();

    public FBXAnimLayer(AssetManager assetManager, AssetKey key, String name) {
        super(assetManager, key);
        this.name = name;
    }

    public FBXAnimLayer(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
        name = "Look at this problem";
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
        System.out.println(element);
        // TODO: multilayer support
    }

    @Override
    protected Object toImplObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(FBXObject obj) {
        if (!(obj instanceof FBXAnimCurveNode)) {
            unsupportedConnectObject(obj);
        }
        animCurveNodes.add((FBXAnimCurveNode) obj);
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }

    @Override
    public void fromElementAscii(FBXElement fbxElement) {
        for (FBXElement element : fbxElement.getChildren()) {
            if (element.name.equals("Channel")) {
                for (FBXElement child : element.getChildren()) {
                    var curve = new FBXAnimCurveNode(assetManager, key);
                    curve.fromElementAscii(child);
                    curve.setProperty((String) child.getProperties().get(0));
                    animCurveNodes.add(curve);
                }
            }
        }
    }

    public List<FBXAnimCurveNode> getAnimationCurveNodes() {
        return animCurveNodes;
    }
}
