package mini.animation;

import mini.animation.presets.HumanoidPreset;
import mini.math.Matrix4f;
import mini.utils.TempVars;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * <code>Skeleton</code> is a convenience class for managing a bone hierarchy.
 * Skeleton updates the world transforms to reflect the current local animated matrices.
 */
public final class Skeleton implements MiniCloneable {
    private final Bone[] rootBones;
    private Map<HumanoidPreset.HumanoidJoints, Bone> jointsBoneMap;
    private Bone[] bones;
    private Matrix4f[] skinningMatrices;

    public Skeleton(Bone[] bones) {
        this.bones = bones;

        var rootBones = new ArrayList<Bone>();

        for (int length = bones.length - 1; length >= 0; length--) {
            var bone = bones[length];
            if (bone.getParent() == null) {
                rootBones.add(bone);
            }
        }
        this.rootBones = rootBones.toArray(new Bone[0]);

        createSkinningMatrices();

        for (int length = this.rootBones.length - 1; length >= 0; length--) {
            var rootBone = this.rootBones[length];
            rootBone.update();
            rootBone.setBindingPose();
        }
    }

    private void createSkinningMatrices() {
        skinningMatrices = new Matrix4f[bones.length];
        Arrays.setAll(skinningMatrices, i -> new Matrix4f());
    }

    public void resetAndUpdate() {
        for (int index = rootBones.length - 1; index >= 0; index--) {
            Bone rootBone = rootBones[index];
            rootBone.reset();
            rootBone.update();
        }
    }

    public void reset() {
        for (int index = rootBones.length - 1; index >= 0; index--) {
            rootBones[index].reset();
        }
    }

    public void updateWorldVectors() {
        for (int index = rootBones.length - 1; index >= 0; index--) {
            rootBones[index].update();
        }
    }

    public Matrix4f[] computeSkinningMatrices() {
        try (var vars = TempVars.get()) {
            for (int i = 0; i < bones.length; i++) {
                bones[i].getOffsetTransform(skinningMatrices[i], vars.quat1, vars.vect1, vars.vect2,
                                            vars.tempMat3);
            }
        }
        return skinningMatrices;
    }

    @Override
    public Object miniClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the number of bones of this skeleton
     */
    public int getBoneCount() {
        return bones.length;
    }

    /**
     * @return the bone index of the given bone
     */
    public int getBoneIndex(Bone bone) {
        return IntStream.range(0, bones.length).filter(i -> bones[i] == bone).findFirst()
                        .orElse(-1);
    }

    /**
     * return a bone for the given index
     *
     * @param index
     * @return
     */
    public Bone getBone(int index) {
        return bones[index];
    }

    /**
     * returns the bone with the given name
     *
     * @param name
     * @return
     */
    public Bone getBone(String name) {
        for (int i = 0; i < bones.length; i++) {
            if (bones[i].getName().equals(name)) {
                return bones[i];
            }
        }
        return null;
    }

    public Bone[] getRoots() {
        return rootBones;
    }

    public Map<HumanoidPreset.HumanoidJoints, Bone> getJointsBoneMap() {
        return jointsBoneMap;
    }

    public void setJointsBoneMap(
            Map<HumanoidPreset.HumanoidJoints, Bone> jointsBoneMap) {
        this.jointsBoneMap = jointsBoneMap;
    }

    public Bone getBoneFromJointName(HumanoidPreset.HumanoidJoints jointName) {
        return jointsBoneMap.get(jointName);
    }
}
