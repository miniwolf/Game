package mini.scene.plugins.fbx;

import mini.animation.Bone;
import mini.animation.Skeleton;
import mini.scene.plugins.fbx.anim.FBXLimbNode;
import mini.scene.plugins.fbx.node.FBXNode;

import java.util.ArrayList;
import java.util.List;

public class FBXSkeletonCreator {
    public static void createSkeletons(FBXNode node) {
        boolean createSkeleton = false;
        for (FBXNode child : node.getChildren()) {
            if (child instanceof FBXLimbNode) {
                createSkeleton = true;
            } else {
                createSkeletons(child);
            }
        }
        if (createSkeleton) {
            if (node.getSkeleton() != null) {
                throw new UnsupportedOperationException();
            }
            node.setSkeleton(createSkeletonFromLimbNode(node));
        }
    }

    private static Skeleton createSkeletonFromLimbNode(FBXNode node) {
        if (node instanceof FBXLimbNode) {
            throw new UnsupportedOperationException("Limb nodes cannot be skeleton holders");
        }

        var bones = new ArrayList<Bone>();
        for (FBXNode child : node.getChildren()) {
            if (child instanceof FBXLimbNode) {
                createBones(node, (FBXLimbNode) child, bones);
            }
        }

        return new Skeleton(bones.toArray(new Bone[0]));
    }

    private static void createBones(FBXNode node, FBXLimbNode limb, List<Bone> bones) {
        limb.setSkeletonHolder(node);

        Bone parentBone = limb.getBone();
        bones.add(parentBone);

        for (FBXNode child : limb.getChildren()) {
            if (child instanceof FBXLimbNode) {
                var childLimb = (FBXLimbNode) child;
                createBones(node, childLimb, bones);
                parentBone.addChild(childLimb.getBone());
            }
        }
    }
}
