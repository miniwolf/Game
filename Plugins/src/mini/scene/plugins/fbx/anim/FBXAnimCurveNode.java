package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.FastMath;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.AsciiObject;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXAnimCurveNode extends FBXObject implements AsciiObject {
    private FBXAnimCurve xCurve;
    private FBXAnimCurve yCurve;
    private FBXAnimCurve zCurve;
    private String property;

    protected FBXAnimCurveNode(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement fbxElement) {
        System.out.println("Take this");
    }

    @Override
    protected Object toImplObject() {
        return null;
    }

    @Override
    public void link(FBXObject obj) {
        System.err.println(obj.getName());
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        System.err.println(obj.getName());
        System.err.println(propertyName);
    }

    public void fromElementAscii(FBXElement fbxElement) {
        for (FBXElement element : fbxElement.getChildren()) {
            if (element.name.equals("Channel")) {
                switch ((String) element.getProperties().get(0)) {
                    case "X":
                        xCurve = new FBXAnimCurve(assetManager, key);
                        xCurve.fromElementAscii(element);
                        break;
                    case "Y":
                        yCurve = new FBXAnimCurve(assetManager, key);
                        yCurve.fromElementAscii(element);
                        break;
                    case "Z":
                        zCurve = new FBXAnimCurve(assetManager, key);
                        zCurve.fromElementAscii(element);
                        break;
                }
            } else if (element.name.equals("LayerType")) {
                // TODO: Look this up?
            }
        }
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public FBXAnimCurve getxCurve() {
        return xCurve;
    }

    public FBXAnimCurve getyCurve() {
        return yCurve;
    }

    public FBXAnimCurve getzCurve() {
        return zCurve;
    }

    public Vector3f getVector3fValue(long time) {
        var value = new Vector3f();
        value.x = xCurve.getValueAtTime(time);
        value.y = yCurve.getValueAtTime(time);
        value.z = zCurve.getValueAtTime(time);
        return value;
    }

    public Quaternion getQuaternionValue(long time) {
        Vector3f eulerAngles = getVector3fValue(time);
        Quaternion q = new Quaternion();
        q.fromAngles(eulerAngles.x * FastMath.DEG_TO_RAD,
                     eulerAngles.y * FastMath.DEG_TO_RAD,
                     eulerAngles.z * FastMath.DEG_TO_RAD);
        return q;
    }
}
