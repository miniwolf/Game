package mini.scene;

import mini.asset.ModelKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetLinkNode extends Node {
    private List<ModelKey> assetLoaderKeys = new ArrayList<>();
    private Map<ModelKey, Spatial> assetChildren = new HashMap<>();

    public AssetLinkNode(ModelKey key) {
        this(key.getName(), key);
    }

    public AssetLinkNode(String name, ModelKey key) {
        super(name);
        assetLoaderKeys.add(key);
    }

    public void attachLinkedChild(Spatial spatial, ModelKey key) {
        addLinkedChild(key);
        assetChildren.put(key, spatial);
        attachChild(spatial);
    }

    /**
     * Add a "linked" child. These are loaded from the assetManager when the <code>AssetLInkNode</code> is loaded from a
     * binary file.
     */
    private void addLinkedChild(ModelKey key) {
        if (assetLoaderKeys.contains(key)) {
            return;
        }

        assetLoaderKeys.add(key);
    }
    // TODO: Override maybes?
}
