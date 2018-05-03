package mini.scene.plugins.fbx;

import mini.animation.BoneTrack;
import mini.animation.SpatialTrack;
import mini.animation.Track;
import mini.animation.presets.HumanoidPreset;
import mini.math.Quaternion;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.scene.plugins.fbx.anim.FBXAnimCurveNode;
import mini.scene.plugins.fbx.anim.FBXAnimLayer;
import mini.scene.plugins.fbx.anim.FBXAnimStack;
import mini.scene.plugins.fbx.node.FBXNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FBXTrack {
    public FBXAnimStack animStack;
    public FBXAnimLayer animLayer;
    public FBXNode node;
    public List<FBXAnimCurveNode> animCurves = new ArrayList<>();

    public BoneTrack toBoneTrack(HumanoidPreset.HumanoidJoints jointName, int boneIndex,
                                 Transform inverseBindPose) {
        return (BoneTrack) toTrackInternal(jointName, boneIndex, inverseBindPose);
    }

    public long[] getKeyTimes() {
        Set<Long> keyFrameTimesSet = new HashSet<Long>();
        for (FBXAnimCurveNode curveNode : animCurves) {
            for (long keyTime : curveNode.getxCurve().getKeyTimes()) {
                keyFrameTimesSet.add(keyTime);
            }
        }
        long[] keyFrameTimes = new long[keyFrameTimesSet.size()];
        int i = 0;
        for (Long keyFrameTime : keyFrameTimesSet) {
            keyFrameTimes[i++] = keyFrameTime;
        }
        Arrays.sort(keyFrameTimes);
        return keyFrameTimes;
    }

    private void applyInverse(Vector3f translation, Quaternion rotation, Vector3f scale,
                              Transform inverseBindPose) {
        Transform t = new Transform();
        t.setTranslation(translation);
        t.setRotation(rotation);
        if (scale != null) {
            t.setScale(scale);
        }
        t.combineWithParent(inverseBindPose);

        t.getTranslation(translation);
        t.getRotation(rotation);
        if (scale != null) {
            t.getScale(scale);
        }
    }

    private Track toTrackInternal(HumanoidPreset.HumanoidJoints jointName, int boneIndex,
                                  Transform inverseBindPose) {
        float duration = animStack.getDuration();

        Optional<FBXAnimCurveNode> translationCurve = animCurves.stream()
                                                                .filter(node -> node.getProperty()
                                                                                    .equals("T"))
                                                                .findFirst();
        Optional<FBXAnimCurveNode> rotationCurve = animCurves.stream()
                                                             .filter(node -> node.getProperty()
                                                                                 .equals("R"))
                                                             .findFirst();
        Optional<FBXAnimCurveNode> scalingCurve = animCurves.stream()
                                                            .filter(node -> node.getProperty()
                                                                                .equals("S"))
                                                            .findFirst();

        long[] fbxTimes = getKeyTimes();
        float[] times = new float[fbxTimes.length];

        // Translations / Rotations must be set on all tracks.
        Vector3f[] translations = new Vector3f[fbxTimes.length];
        Quaternion[] rotations = new Quaternion[fbxTimes.length];

        Vector3f[] scales = null;
        if (scalingCurve.isPresent()) {
            scales = new Vector3f[fbxTimes.length];
        }

        for (int i = 0; i < fbxTimes.length; i++) {
            long fbxTime = fbxTimes[i];
            float time = (float) (fbxTime * FBXAnimUtil.SECONDS_PER_UNIT);

            if (time > duration) {
                // Expand animation duration to fit the curve.
                duration = time;
                System.out.println("actual duration: " + duration);
            }

            times[i] = time;
            if (translationCurve.isPresent()) {
                translations[i] = translationCurve.get().getVector3fValue(fbxTime);
            } else {
                translations[i] = new Vector3f();
            }
            if (rotationCurve.isPresent()) {
                rotations[i] = rotationCurve.get().getQuaternionValue(fbxTime);
//                if (i > 0) {
//                    if (rotations[i - 1].dot(rotations[i]) < 0) {
//                        System.out.println("rotation will go the long way, oh noes");
//                        rotations[i - 1].negate();
//                    }
//                }
            } else {
                rotations[i] = new Quaternion();
            }
            if (scalingCurve.isPresent()) {
                scales[i] = scalingCurve.get().getVector3fValue(fbxTime);
            }

            if (inverseBindPose != null) {
                applyInverse(translations[i], rotations[i], scales != null ? scales[i] : null,
                             inverseBindPose);
            }
        }

        if (boneIndex == -1) {
            return new SpatialTrack(times, translations, rotations, scales);
        } else {
            if (scales != null) {
                return new BoneTrack(boneIndex, jointName, times, translations, rotations, scales);
            } else {
                return new BoneTrack(boneIndex, jointName, times, translations, rotations);
            }
        }
    }

    public SpatialTrack toSpatialTrack() {
        return (SpatialTrack) toTrackInternal(null, -1, null);
    }
}
