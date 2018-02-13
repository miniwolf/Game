package mini.animation;

import mini.math.Quaternion;
import mini.math.Vector3f;

public class SpatialTrack {
    private final float[] times;
    private Vector3f[] translations;
    private Quaternion[] rotations;
    private Vector3f[] scales;

    /**
     * @param times        a float array with the time of each frame
     * @param translations the translation of the bone for each frame
     * @param rotations    the rotation of the bone for each frame
     * @param scales       the scale of the bone for each frame
     */
    public SpatialTrack(float[] times, Vector3f[] translations, Quaternion[] rotations,
                        Vector3f[] scales) {
        if (times.length == 0) {
            throw new IllegalArgumentException("BoneTrack with no keyframes!");
        }
        this.times = times;
        setKeyFrames(translations, rotations, scales);
    }

    private void setKeyFrames(Vector3f[] translations, Quaternion[] rotations, Vector3f[] scales) {
        if (translations != null) {
            assert times.length == translations.length;
            this.translations = translations;
        }
        if (rotations != null) {
            assert times.length == rotations.length;
            this.rotations = rotations;
        }
        if (scales != null) {
            assert times.length == scales.length;
            this.scales = scales;
        }
    }

    public void setTime(float time, AnimationControl control, AnimationChannel channel) {

    }
}
