package mini.scene.plugins.fbx;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.anim.FBXAnimStack;
import mini.scene.plugins.fbx.file.FBXElement;

import java.util.ArrayList;
import java.util.List;

public class FBXTakeLoader implements FBXElementLoader<List<FBXAnimStack>> {
    private final AssetManager assetManager;
    private final AssetKey key;
    private String takeName;

    public FBXTakeLoader(AssetManager assetManager, AssetKey key) {
        this.assetManager = assetManager;
        this.key = key;
    }

    public String getTakeName() {
        return takeName;
    }

    @Override
    public List<FBXAnimStack> load(FBXElement fbxElement) {
        List<FBXAnimStack> stacks = new ArrayList<>();
        for (FBXElement element : fbxElement.getChildren()) {
            if (element.name.equals("Current")) {
                // Not supported
            } else if (element.name.equals("Take")) {
                var stack = new FBXAnimStack(assetManager, key);
                stack.fromElementAscii(element);
                stacks.add(stack);
                var affectedNode = (String) element.getProperties().get(0);
            }
        }
        return stacks;
    }
}
