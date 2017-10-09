package mini.scene.plugins.fbx;

import mini.asset.AssetInfo;
import mini.asset.AssetLoader;
import mini.asset.AssetManager;
import mini.scene.Node;
import mini.scene.plugins.fbx.file.FBXReader;

import java.io.IOException;
import java.io.InputStream;

public class FBXLoader implements AssetLoader {

    private AssetManager assetManager;
    private FBXReader reader;

    @Override
    public Object load(AssetInfo info) throws IOException {
        this.assetManager = info.getManager();

        final Node sceneNode = new Node("-scene");
        InputStream in = info.openStream();
        loadScene(in);
        return sceneNode;
    }

    private void loadScene(InputStream in) throws IOException {
        reader.readFBX(in);
    }
}
