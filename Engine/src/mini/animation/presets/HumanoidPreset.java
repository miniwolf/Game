package mini.animation.presets;

import mini.animation.Bone;
import mini.animation.Skeleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HumanoidPreset extends Presets {
    protected Map<HumanoidJoints, Bone> transformMap = new HashMap<>();
    protected Map<Bone, HumanoidJoints> boneHumanoidJointsMap = new HashMap<>();

    public HumanoidJoints getJointName(Bone bone) {
        return boneHumanoidJointsMap.get(bone);
    }

    public Map<HumanoidJoints, Bone> getJointsBoneMap() {
        return transformMap;
    }

    private void addBone(Bone upperLeg, HumanoidJoints lUpperLeg) {
        transformMap.put(lUpperLeg, upperLeg);
        boneHumanoidJointsMap.put(upperLeg, lUpperLeg);
    }

    public void setupBonesForPreset(Skeleton skeleton) {
        for (Bone bone : skeleton.getRoots()) {
            recurseOnReference(bone.getChildren().get(0));
        }
    }

    private void recurseOnReference(Bone rootBone) {
        addBone(rootBone, HumanoidJoints.Hips);
        recurseOnSpine(rootBone.getChildren().get(0));
        recurseOnRightLeg(rootBone.getChildren().get(1));
        recurseOnLeftLeg(rootBone.getChildren().get(2));
    }

    private void recurseOnLeftLeg(Bone upperLeg) {
        addBone(upperLeg, HumanoidJoints.LUpperLeg);
        var lowerLeg = upperLeg.getChildren().get(0);
        addBone(lowerLeg, HumanoidJoints.LLowerLeg);
        var foot = lowerLeg.getChildren().get(0);
        addBone(foot, HumanoidJoints.LFoot);
        var toes = foot.getChildren().get(0); // TODO:
    }

    private void recurseOnRightLeg(Bone upperLeg) {
        addBone(upperLeg, HumanoidJoints.RUpperLeg);
        var lowerLeg = upperLeg.getChildren().get(0);
        addBone(lowerLeg, HumanoidJoints.RLowerLeg);
        var foot = lowerLeg.getChildren().get(0);
        addBone(foot, HumanoidJoints.RFoot);
        var toes = foot.getChildren().get(0); // TODO:
    }

    private void recurseOnSpine(Bone bone) {
        addBone(bone, HumanoidJoints.Spine);
        recurseOnChest(bone.getChildren().get(0));
    }

    private void recurseOnChest(Bone bone) {
        addBone(bone, HumanoidJoints.Chest);
        var neck = bone.getChildren().get(0);
        addBone(neck, HumanoidJoints.Neck);
        recurseOnHead(neck.getChildren().get(0));
        recurseRightArm(bone.getChildren().get(1));
        recurseLeftArm(bone.getChildren().get(2));
    }

    private void recurseLeftArm(Bone shoulder) {
        addBone(shoulder, HumanoidJoints.LShoulder);
        var upperArm = shoulder.getChildren().get(0);
        addBone(upperArm, HumanoidJoints.LUpperArm);
        var lowerArm = upperArm.getChildren().get(0);
        addBone(lowerArm, HumanoidJoints.LLowerArm);
        var hand = lowerArm.getChildren().get(0);
        addBone(hand, HumanoidJoints.LHand);
        recurseLeftHand(hand.getChildren());
    }

    private void recurseRightArm(Bone shoulder) {
        addBone(shoulder, HumanoidJoints.RShoulder);
        var upperArm = shoulder.getChildren().get(0);
        addBone(upperArm, HumanoidJoints.RUpperArm);
        var lowerArm = upperArm.getChildren().get(0);
        addBone(lowerArm, HumanoidJoints.RLowerArm);
        var hand = lowerArm.getChildren().get(0);
        addBone(hand, HumanoidJoints.RHand);
        recurseRightHand(hand.getChildren());
    }

    private void recurseRightHand(List<Bone> children) {
        var thumbProx = children.get(0);
        addBone(thumbProx, HumanoidJoints.RThumbProximal);
        var thumbInterm = thumbProx.getChildren().get(0);
        addBone(thumbInterm, HumanoidJoints.RThumbIntermediate);
        if (!thumbInterm.getChildren().isEmpty()) {
            var thumbDist = thumbInterm.getChildren().get(0);
            addBone(thumbDist, HumanoidJoints.RThumbDistal);
        }

        var indexProx = children.get(1);
        addBone(indexProx, HumanoidJoints.RIndexProximal);
        var indexInterm = indexProx.getChildren().get(0);
        addBone(indexInterm, HumanoidJoints.RIndexIntermediate);
        if (!indexInterm.getChildren().isEmpty()) {
            var indexDist = indexInterm.getChildren().get(0);
            addBone(indexDist, HumanoidJoints.RIndexDistal);
        }

        var middleProx = children.get(1);
        addBone(middleProx, HumanoidJoints.RMiddleProximal);
        var middleInterm = middleProx.getChildren().get(0);
        addBone(middleInterm, HumanoidJoints.RMiddleIntermediate);
        if (!middleInterm.getChildren().isEmpty()) {
            var middleDist = middleInterm.getChildren().get(0);
            addBone(middleDist, HumanoidJoints.RMiddleDistal);
        }

        var ringProx = children.get(1);
        addBone(ringProx, HumanoidJoints.RRingProximal);
        var ringInterm = ringProx.getChildren().get(0);
        addBone(ringInterm, HumanoidJoints.RRingIntermediate);
        if (!ringInterm.getChildren().isEmpty()) {
            var ringDist = ringInterm.getChildren().get(0);
            addBone(ringDist, HumanoidJoints.RRingDistal);
        }

        var littleProx = children.get(1);
        addBone(littleProx, HumanoidJoints.RLittleProximal);
        var littleInterm = littleProx.getChildren().get(0);
        addBone(littleInterm, HumanoidJoints.RLittleIntermediate);
        if (!littleInterm.getChildren().isEmpty()) {
            var littleDist = littleInterm.getChildren().get(0);
            addBone(littleDist, HumanoidJoints.RLittleDistal);
        }
    }

    private void recurseLeftHand(List<Bone> children) {
        var thumbProx = children.get(0);
        addBone(thumbProx, HumanoidJoints.LThumbProximal);
        var thumbInterm = thumbProx.getChildren().get(0);
        addBone(thumbInterm, HumanoidJoints.LThumbIntermediate);
        if (!thumbInterm.getChildren().isEmpty()) {
            var thumbDist = thumbInterm.getChildren().get(0);
            addBone(thumbDist, HumanoidJoints.LThumbDistal);
        }

        var indexProx = children.get(1);
        addBone(indexProx, HumanoidJoints.LIndexProximal);
        var indexInterm = indexProx.getChildren().get(0);
        addBone(indexInterm, HumanoidJoints.LIndexIntermediate);
        if (!indexInterm.getChildren().isEmpty()) {
            var indexDist = indexInterm.getChildren().get(0);
            addBone(indexDist, HumanoidJoints.LIndexDistal);
        }

        var middleProx = children.get(1);
        addBone(middleProx, HumanoidJoints.LMiddleProximal);
        var middleInterm = middleProx.getChildren().get(0);
        addBone(middleInterm, HumanoidJoints.LMiddleIntermediate);
        if (!middleInterm.getChildren().isEmpty()) {
            var middleDist = middleInterm.getChildren().get(0);
            addBone(middleDist, HumanoidJoints.LMiddleDistal);
        }

        var ringProx = children.get(1);
        addBone(ringProx, HumanoidJoints.LRingProximal);
        var ringInterm = ringProx.getChildren().get(0);
        addBone(ringInterm, HumanoidJoints.LRingIntermediate);
        if (!ringInterm.getChildren().isEmpty()) {
            var ringDist = ringInterm.getChildren().get(0);
            addBone(ringDist, HumanoidJoints.LRingDistal);
        }

        var littleProx = children.get(1);
        addBone(littleProx, HumanoidJoints.LLittleProximal);
        var littleInterm = littleProx.getChildren().get(0);
        addBone(littleInterm, HumanoidJoints.LLittleIntermediate);
        if (!littleInterm.getChildren().isEmpty()) {
            var littleDist = littleInterm.getChildren().get(0);
            addBone(littleDist, HumanoidJoints.LLittleDistal);
        }
    }

    private void recurseOnHead(Bone bone) {
        addBone(bone, HumanoidJoints.Head);
        List<Bone> children = bone.getChildren();
        addBone(children.get(0), HumanoidJoints.LEye);
        addBone(children.get(1), HumanoidJoints.REye);
        if (children.size() > 2) { addBone(children.get(2), HumanoidJoints.Jaw); }
    }

    public enum HumanoidJoints {
        Hips, Spine, Chest, // Body
        LShoulder, LUpperArm, LLowerArm, LHand, // Left Arm
        RShoulder, RUpperArm, RLowerArm, RHand, // Right arm
        LUpperLeg, LLowerLeg, LFoot, // Left leg
        RUpperLeg, RLowerLeg, RFoot, // Right leg
        Neck, Head, LEye, REye, Jaw, // Head
        LThumbProximal, LThumbIntermediate, LThumbDistal,
        LIndexProximal, LIndexIntermediate, LIndexDistal,
        LMiddleProximal, LMiddleIntermediate, LMiddleDistal,
        LRingProximal, LRingIntermediate, LRingDistal,
        LLittleProximal, LLittleIntermediate, LLittleDistal, // Left hand
        RThumbProximal, RThumbIntermediate, RThumbDistal,
        RIndexProximal, RIndexIntermediate, RIndexDistal,
        RMiddleProximal, RMiddleIntermediate, RMiddleDistal,
        RRingProximal, RRingIntermediate, RRingDistal,
        RLittleProximal, RLittleIntermediate, RLittleDistal, // Right hand
    }
}
