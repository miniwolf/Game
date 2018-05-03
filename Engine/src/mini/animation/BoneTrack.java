package mini.animation;

import mini.animation.presets.HumanoidPreset;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.utils.TempVars;

import java.util.BitSet;

/**
 * Contains a list of transforms and times for each keyframe.
 *
 * @author Kirill Vainer
 */
public final class BoneTrack implements Track {

    /**
     * Bone index in the skeleton which this track effects.
     */
    private int targetBoneIndex;
    private HumanoidPreset.HumanoidJoints jointName;

    /**
     * Transforms and times for track.
     */
    private CompactVector3fArray translations;
    private CompactQuaternionArray rotations;
    private CompactVector3fArray scales;
    private float[] times;

    /**
     * Serialization-only. Do not use.
     */
    public BoneTrack() {
    }

    /**
     * Creates a bone track for the given bone index
     *
     * @param targetBoneIndex the bone index
     * @param times           a float array with the time of each frame
     * @param translations    the translation of the bone for each frame
     * @param rotations       the rotation of the bone for each frame
     */
    public BoneTrack(int targetBoneIndex, HumanoidPreset.HumanoidJoints jointName, float[] times,
                     Vector3f[] translations, Quaternion[] rotations) {
        this.targetBoneIndex = targetBoneIndex;
        this.jointName = jointName;
        this.setKeyframes(times, translations, rotations);
    }

    /**
     * Creates a bone track for the given bone index
     *
     * @param targetBoneIndex the bone index
     * @param times           a float array with the time of each frame
     * @param translations    the translation of the bone for each frame
     * @param rotations       the rotation of the bone for each frame
     * @param scales          the scale of the bone for each frame
     */
    public BoneTrack(int targetBoneIndex, HumanoidPreset.HumanoidJoints jointName, float[] times,
                     Vector3f[] translations, Quaternion[] rotations, Vector3f[] scales) {
        this.targetBoneIndex = targetBoneIndex;
        this.jointName = jointName;
        this.setKeyframes(times, translations, rotations, scales);
    }

    /**
     * Creates a bone track for the given bone index
     *
     * @param targetBoneIndex the bone's index
     */
    public BoneTrack(int targetBoneIndex) {
        this.targetBoneIndex = targetBoneIndex;
    }

    /**
     * @return the bone index of this bone track.
     */
    public int getTargetBoneIndex() {
        return targetBoneIndex;
    }

    /**
     * return the array of rotations of this track
     *
     * @return
     */
    public Quaternion[] getRotations() {
        return rotations.toObjectArray();
    }

    /**
     * returns the array of scales for this track
     *
     * @return
     */
    public Vector3f[] getScales() {
        return scales == null ? null : scales.toObjectArray();
    }

    /**
     * returns the arrays of time for this track
     *
     * @return
     */
    public float[] getTimes() {
        return times;
    }

    /**
     * returns the array of translations of this track
     *
     * @return
     */
    public Vector3f[] getTranslations() {
        return translations.toObjectArray();
    }

    /**
     * Set the translations and rotations for this bone track
     *
     * @param times        a float array with the time of each frame
     * @param translations the translation of the bone for each frame
     * @param rotations    the rotation of the bone for each frame
     */
    public void setKeyframes(float[] times, Vector3f[] translations, Quaternion[] rotations) {
        if (times.length == 0) {
            throw new RuntimeException("BoneTrack with no keyframes!");
        }

        assert times.length == translations.length && times.length == rotations.length;

        this.times = times;
        this.translations = new CompactVector3fArray();
        this.translations.add(translations);
        this.translations.freeze();
        this.rotations = new CompactQuaternionArray();
        this.rotations.add(rotations);
        this.rotations.freeze();
    }

    /**
     * Set the translations, rotations and scales for this bone track
     *
     * @param times        a float array with the time of each frame
     * @param translations the translation of the bone for each frame
     * @param rotations    the rotation of the bone for each frame
     * @param scales       the scale of the bone for each frame
     */
    public void setKeyframes(float[] times, Vector3f[] translations, Quaternion[] rotations,
                             Vector3f[] scales) {
        this.setKeyframes(times, translations, rotations);
        assert times.length == scales.length;
        if (scales != null) {
            this.scales = new CompactVector3fArray();
            this.scales.add(scales);
            this.scales.freeze();
        }
    }

    /**
     * Modify the bone which this track modifies in the skeleton to contain
     * the correct animation transforms for a given time.
     * The transforms can be interpolated in some method from the keyframes.
     *
     * @param time    the current time of the animation
     * @param weight  the weight of the animation
     * @param control
     * @param channel
     * @param vars
     */
    public void setTime(float time, float weight, AnimationControl control,
                        AnimationChannel channel, TempVars vars) {
        BitSet affectedBones = channel.getAffectedBones();
        if (affectedBones != null && !affectedBones.get(targetBoneIndex)) {
            return;
        }

        if (jointName == null) {
            return; // TODO: This could be a missing set in the humanoid preset
        }
        Bone target = control.getSkeleton().getBoneFromJointName(jointName);
        if (target == null) {
            throw new IllegalStateException("Mapping is not complete");
        }

        Vector3f tempV = vars.vect1;
        Vector3f tempS = vars.vect2;
        Quaternion tempQ = vars.quat1;
        Vector3f tempV2 = vars.vect3;
        Vector3f tempS2 = vars.vect4;
        Quaternion tempQ2 = vars.quat2;

        int lastFrame = times.length - 1;
        if (time < 0 || lastFrame == 0) {
            rotations.get(0, tempQ);
            translations.get(0, tempV);
            if (scales != null) {
                scales.get(0, tempS);
            }
        } else if (time >= times[lastFrame]) {
            rotations.get(lastFrame, tempQ);
            translations.get(lastFrame, tempV);
            if (scales != null) {
                scales.get(lastFrame, tempS);
            }
        } else {
            int startFrame = 0;
            int endFrame = 1;
            // use lastFrame so we never overflow the array
            int i;
            for (i = 0; i < lastFrame && times[i] < time; i++) {
                startFrame = i;
                endFrame = i + 1;
            }

            float blend = (time - times[startFrame]) / (times[endFrame] - times[startFrame]);

            rotations.get(startFrame, tempQ);
            translations.get(startFrame, tempV);
            if (scales != null) {
                scales.get(startFrame, tempS);
            }
            rotations.get(endFrame, tempQ2);
            translations.get(endFrame, tempV2);
            if (scales != null) {
                scales.get(endFrame, tempS2);
            }
            tempQ.nlerp(tempQ2, blend);
            tempV.interpolateLocal(tempV2, blend);
            tempS.interpolateLocal(tempS2, blend);
        }

//        if (weight != 1f) {
        target.blendAnimTransforms(tempV, tempQ, scales != null ? tempS : null, weight);
//        } else {
//            target.setAnimTransforms(tempV, tempQ, scales != null ? tempS : null);
//        }
    }

    /**
     * @return the length of the track
     */
    public float getLength() {
        return times == null ? 0 : times[times.length - 1] - times[0];
    }

    public float[] getKeyFrameTimes() {
        return times;
    }

    /**
     * This method creates a clone of the current object.
     *
     * @return a clone of the current object
     */
    @Override
    public BoneTrack clone() {
        int tablesLength = times.length;

        float[] times = this.times.clone();
        Vector3f[] sourceTranslations = this.getTranslations();
        Quaternion[] sourceRotations = this.getRotations();
        Vector3f[] sourceScales = this.getScales();

        Vector3f[] translations = new Vector3f[tablesLength];
        Quaternion[] rotations = new Quaternion[tablesLength];
        Vector3f[] scales = new Vector3f[tablesLength];
        for (int i = 0; i < tablesLength; ++i) {
            translations[i] = sourceTranslations[i].clone();
            rotations[i] = sourceRotations[i].clone();
            scales[i] = sourceScales != null ? sourceScales[i].clone() :
                        new Vector3f(1.0f, 1.0f, 1.0f);
        }

        // Need to use the constructor here because of the final fields used in this class
        return new BoneTrack(targetBoneIndex, jointName, times, translations, rotations, scales);
    }

    public void setTime(float time, float weight, AnimationControl control,
                        AnimationChannel channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
