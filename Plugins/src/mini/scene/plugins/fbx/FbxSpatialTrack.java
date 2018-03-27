package mini.scene.plugins.fbx;

import mini.animation.SpatialTrack;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.node.FBXNodeAttribute;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FbxSpatialTrack extends FBXNodeAttribute<SpatialTrack> {
    private static long FBX_TC_MILLISECOND = 46186158;
    private static long FBX_TC_SECOND = FBX_TC_MILLISECOND * 1000;
    private static long FBX_TC_MINUTE = FBX_TC_SECOND * 60;
    private static long FBX_TC_HOUR = FBX_TC_MINUTE * 60;
    private static long FBX_TC_DAY = FBX_TC_HOUR * 24;
    private String modelName;
    private Vector3f[] translations;
    private Quaternion[] rotations;
    private Vector3f[] scales;
    private long[] times;

    public FbxSpatialTrack(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
        modelName = (String) element.getProperties().get(0);

        times = (long[]) element.getChildren().get(1).getChildren().get(0).getChildren().get(0)
                                .getChildren().get(1).getProperties().get(0);
        var transformElements = element.getChildren().get(1).getChildren();
        FBXElement translationElement = transformElements.get(0);
        translations = readVectorElement(translationElement).values().toArray(new Vector3f[0]);

        FBXElement rotationElement = transformElements.get(1);
        rotations = readQuaternionElement(rotationElement).values().toArray(new Quaternion[0]);

        FBXElement scalingElement = transformElements.get(2);
        scales = readVectorElement(scalingElement).values().toArray(new Vector3f[0]);

        assert translations.length == rotations.length;
        // TODO: If the timestamps conflict we have to abandon unknown.
    }

    private Map<Long, Vector3f> readVectorElement(FBXElement transformElements) {
        Map<Long, Vector3f> timeToVector = new HashMap<>();
        FBXElement xElement = transformElements.getChildren().get(0);
        setValue(timeToVector, xElement, 0);
        FBXElement yElement = transformElements.getChildren().get(1);
        setValue(timeToVector, yElement, 1);
        FBXElement zElement = transformElements.getChildren().get(2);
        setValue(timeToVector, zElement, 2);
        return timeToVector;
    }

    private void setValue(Map<Long, Vector3f> timeToVector, FBXElement transformElement,
                          int index) {
        List<Object> properties = transformElement.getChildren().get(1).getProperties();
        long[] timeStamps = (long[]) properties.get(0);
        double[] values = (double[]) properties.get(1);
        for (int i = 0; i < timeStamps.length; i++) {
            long timeStamp = timeStamps[i];
            double value = values[i];
            Vector3f translation = timeToVector.getOrDefault(timeStamp, new Vector3f());
            translation.set(index, (float) value);
        }
    }

    private Map<Long, Quaternion> readQuaternionElement(FBXElement transformElements) {
        Map<Long, Quaternion> timeToQuaternion = new HashMap<>();
        FBXElement xElement = transformElements.getChildren().get(0);
        setValueQuaternion(timeToQuaternion, xElement, 0);
        FBXElement yElement = transformElements.getChildren().get(1);
        setValueQuaternion(timeToQuaternion, yElement, 1);
        FBXElement zElement = transformElements.getChildren().get(2);
        setValueQuaternion(timeToQuaternion, zElement, 2);
        FBXElement wElement = transformElements.getChildren().get(2);
        setValueQuaternion(timeToQuaternion, wElement, 3);
        return timeToQuaternion;
    }

    private void setValueQuaternion(Map<Long, Quaternion> timeToQuaternion,
                                    FBXElement transformElement, int index) {
        List<Object> properties = transformElement.getChildren().get(1).getProperties();
        long[] timeStamps = (long[]) properties.get(0);
        double[] values = (double[]) properties.get(1);
        for (int i = 0; i < timeStamps.length; i++) {
            long timeStamp = timeStamps[i];
            double value = values[i];
            Quaternion translation = timeToQuaternion.getOrDefault(timeStamp, new Quaternion());
            translation.set(index, (float) value);
        }
    }

    protected SpatialTrack toImplObject() {
        var convertedTimes = new float[times.length];
        for (int i = 0; i < times.length; i++) {
            convertedTimes[i] = (float) times[i] / FBX_TC_SECOND;
        }
        return new SpatialTrack(convertedTimes, translations, rotations, scales);
    }

    public void link(FBXObject obj) {

    }

    public void link(FBXObject obj, String propertyName) {

    }
}
