package mini.scene.plugins.fbx.node;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXId;

public class FBXRootNode extends FBXNode {
    public FBXRootNode(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
        this.id = FBXId.ROOT;
        this.className = "Model";
        this.name = "Scene";
        this.subclassName = "";
    }
}
