package mini.scene.plugins.fbx.objects;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.textures.Image;
import mini.textures.Texture;

import java.io.File;

public class FBXImage extends FBXObject {
    private final AssetManager assetManager;
    private final AssetKey key;
    private String relativeFilename;
    private String filename;
    private Image image;
    private String type;

    public FBXImage(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
        this.assetManager = assetManager;
        this.key = key;
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);
        if (element.getPropertyTypes().length == 3) {
            type = (String) element.getProperties().get(2);
        } else {
            type = (String) element.getProperties().get(1);
        }

        if (!type.equals("Clip")) {
            return;
        }

        for (FBXElement fbxElement : element.getChildren()) {
            switch (fbxElement.getName()) {
                case "RelativeFilename":
                    relativeFilename = (String) fbxElement.getProperties().get(0);
                    break;
            }
        }
        image = createImage();
    }

    private Image createImage() {
        if (relativeFilename == null) {
            throw new UnsupportedOperationException();
        }

        File dir = new File(key.getFullPathFolder());
        assetManager.registerLocator(mini.asset.plugins.FileLocator.class, dir.getAbsolutePath());
        Texture texture = assetManager.loadTexture(relativeFilename);
        assetManager.unregisterLocator(mini.asset.plugins.FileLocator.class, dir.getAbsolutePath());
        return texture.getImage();
    }

    public Image getImage() {
        return image;
    }
}
