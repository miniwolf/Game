package mini.animation;

import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.Spatial;
import mini.utils.TempVars;

public class SpatialTrack {
    private final float[] times;
    private Vector3f[] translations;
    private Quaternion[] rotations;
    private Vector3f[] scales;
    private Spatial spatial;

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

    public void setTime(float time, AnimationControl control) {
        Spatial spatial = this.spatial;
        if (spatial == null) {
            spatial = control.getSpatial();
        }

        int lastFrame = times.length - 1;
        TempVars vars = TempVars.get();
        Vector3f tempT = vars.vect1;
        Vector3f tempT2 = vars.vect2;
        Quaternion tempQ = vars.quat1;
        Quaternion tempQ2 = vars.quat2;

        if (time < 0 || lastFrame == 0) {
            if (rotations != null) {
                tempQ.set(rotations[0]);
            }
            if (translations != null) {
                tempT.set(translations[0]);
            }
        } else if (time >= times[lastFrame]) {
            if (rotations != null) {
                tempQ.set(rotations[lastFrame]);
            }
            if (translations != null) {
                tempT.set(translations[lastFrame]);
            }
        } else {
            int startFrame = 0;
            int endFrame = 1;
            // use lastFrame so we never overflow the array
            for (int i = 0; i < lastFrame && times[i] < time; ++i) {
                startFrame = i;
                endFrame = i + 1;
            }

            float blend = (time - times[startFrame]) / (times[endFrame] - times[startFrame]);

            if (rotations != null)
                tempQ = rotations[startFrame];
            if (translations != null)
                tempT = translations[startFrame];

            if (rotations != null)
                tempQ2 = rotations[endFrame];
            if (translations != null)
                tempT2 = translations[endFrame];

            tempQ.nlerp(tempQ2, blend);
            tempT.interpolateLocal(tempT2, blend);
        }

        if (translations != null) {
            spatial.setLocalTranslation(tempT);
        }
        if (rotations != null) {
            spatial.setLocalRotation(tempQ);
        }
        vars.release();
    }
}
