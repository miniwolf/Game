package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.FastMath;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.AsciiObject;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXAnimCurve extends FBXObject implements AsciiObject {
    private float defaultValue;
    private long[] keyTimes;
    private double[] keyValues;

    protected FBXAnimCurve(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement element) {

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

    public void fromElementAscii(FBXElement fbxElement) {
        for (FBXElement element : fbxElement.getChildren()) {
            switch (element.name) {
                case "Default":
                    defaultValue = (float) (double) element.getProperties().get(0);
                    break;
                case "Array":
                    keyTimes = (long[]) element.getProperties().get(0);
                    keyValues = (double[]) element.getProperties().get(1);
                    break;
            }
        }
    }

    public long[] getKeyTimes() {
        return keyTimes;
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    /**
     * Retrieve the curve value at the given time.
     * If the curve has no data, 0 is returned.
     * If the time is outside the curve, then the closest value is returned.
     * If the time isn't on an exact keyframe, linear interpolation is used
     * to determine the value between the keyframes at the given time.
     *
     * @param time The time to get the curve value at (in FBX time units).
     * @return The value at the given time.
     */
    public float getValueAtTime(long time) {
        if (keyTimes.length == 0) {
            return 0;
        }

        // If the time is outside the range,
        // we just return the closest value. (No extrapolation)
        if (time <= keyTimes[0]) {
            return (float) keyValues[0];
        } else if (time >= keyTimes[keyTimes.length - 1]) {
            return (float) keyValues[keyValues.length - 1];
        }

        int startFrame = 0;
        int endFrame = 1;
        int lastFrame = keyTimes.length - 1;

        for (int i = 0; i < lastFrame && keyTimes[i] < time; ++i) {
            startFrame = i;
            endFrame = i + 1;
        }

        long keyTime1 = keyTimes[startFrame];
        float keyValue1 = (float) keyValues[startFrame];
        long keyTime2 = keyTimes[endFrame];
        float keyValue2 = (float) keyValues[endFrame];

        if (keyTime2 == time) {
            return keyValue2;
        }

        long prevToNextDelta = keyTime2 - keyTime1;
        long prevToCurrentDelta = time - keyTime1;
        float lerpAmount = (float) prevToCurrentDelta / prevToNextDelta;

        return FastMath.interpolateLinear(lerpAmount, keyValue1, keyValue2);
    }
}
