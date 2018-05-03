package mini.animation;

import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.Spatial;
import mini.utils.TempVars;

public class SpatialTrack implements Track {
    /**
     * Translations of the track.
     */
    private CompactVector3fArray translations;

    /**
     * Rotations of the track.
     */
    private CompactQuaternionArray rotations;

    /**
     * Scales of the track.
     */
    private CompactVector3fArray scales;
    private Spatial spatial;
    /**
     * The times of the animations frames.
     */
    private float[] times;
    private String modelName;
    private boolean FBXTime;

    /**
     * @param times        a float array with the time of each frame
     * @param translations the translation of the bone for each frame
     * @param rotations    the rotation of the bone for each frame
     * @param scales       the scale of the bone for each frame
     */
    public SpatialTrack(float[] times, Vector3f[] translations, Quaternion[] rotations,
                        Vector3f[] scales) {
        setKeyFrames(times, translations, rotations, scales);
    }

    private void setKeyFrames(float[] times, Vector3f[] translations,
                              Quaternion[] rotations, Vector3f[] scales) {
        if (times.length == 0) {
            throw new RuntimeException("BoneTrack with no keyframes!");
        }

        this.times = times;
        if (translations != null) {
            assert times.length == translations.length;
            this.translations = new CompactVector3fArray();
            this.translations.add(translations);
            this.translations.freeze();
        }
        if (rotations != null) {
            assert times.length == rotations.length;
            this.rotations = new CompactQuaternionArray();
            this.rotations.add(rotations);
            this.rotations.freeze();
        }
        if (scales != null) {
            assert times.length == scales.length;
            this.scales = new CompactVector3fArray();
            this.scales.add(scales);
            this.scales.freeze();
        }
    }

    /**
     * Modify the spatial which this track modifies.
     *
     * @param time the current time of the animation
     */
    public void setTime(float time, float weight, AnimationControl control,
                        AnimationChannel channel, TempVars vars) {
        Spatial spatial = control.getSpatial();

        Vector3f tempV = vars.vect1;
        Vector3f tempS = vars.vect2;
        Quaternion tempQ = vars.quat1;
        Vector3f tempV2 = vars.vect3;
        Vector3f tempS2 = vars.vect4;
        Quaternion tempQ2 = vars.quat2;

        int lastFrame = times.length - 1;
        if (time < 0 || lastFrame == 0) {
            if (rotations != null) {
                rotations.get(0, tempQ);
            }
            if (translations != null) {
                translations.get(0, tempV);
            }
            if (scales != null) {
                scales.get(0, tempS);
            }
        } else if (time >= times[lastFrame]) {
            if (rotations != null) {
                rotations.get(lastFrame, tempQ);
            }
            if (translations != null) {
                translations.get(lastFrame, tempV);
            }
            if (scales != null) {
                scales.get(lastFrame, tempS);
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

            if (rotations != null) {
                rotations.get(startFrame, tempQ);
            }
            if (translations != null) {
                translations.get(startFrame, tempV);
            }
            if (scales != null) {
                scales.get(startFrame, tempS);
            }
            if (rotations != null) {
                rotations.get(endFrame, tempQ2);
            }
            if (translations != null) {
                translations.get(endFrame, tempV2);
            }
            if (scales != null) {
                scales.get(endFrame, tempS2);
            }
            tempQ.nlerp(tempQ2, blend);
            tempV.interpolateLocal(tempV2, blend);
            tempS.interpolateLocal(tempS2, blend);
        }

        if (translations != null) {
            spatial.setLocalTranslation(tempV);
        }
        if (rotations != null)
            spatial.setLocalRotation(tempQ);
        if (scales != null) {
            spatial.setLocalScale(tempS);
        }
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    public String getModelName() {
        return modelName;
    }
}
