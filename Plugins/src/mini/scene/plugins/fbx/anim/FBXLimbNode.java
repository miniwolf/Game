package mini.scene.plugins.fbx.anim;

import mini.animation.Bone;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.node.FBXNode;

public class FBXLimbNode extends FBXNode {
    private FBXNode skeletonHolder;
    private Bone bone;

    public FBXLimbNode(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    public Bone getBone() {
        if (bone == null) {
            bone = new Bone(name);
            bone.setBindTransforms(miniLocalBindPose.getTranslation(),
                                   miniLocalBindPose.getRotation(), miniLocalBindPose.getScale());
        }
        return bone;
    }

    public FBXNode getSkeletonHolder() {
        return skeletonHolder;
    }

    public void setSkeletonHolder(FBXNode node) {
        skeletonHolder = node;
    }
}
