package mini.scene.plugins.fbx.material;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.asset.TextureKey;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.textures.Image;
import mini.textures.Texture;

import java.io.File;

public class FBXImage extends FBXObject {
    private final AssetManager assetManager;
    private final AssetKey key;
    private String relativeFilename; // = "../blah/texture.png
    private Image image;
    private String type;
    private TextureKey textureKey;

    public FBXImage(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
        this.assetManager = assetManager;
        this.key = key;
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
        if (element.getPropertyTypes().length == 3) {
            type = (String) element.getProperties().get(2);
        } else {
            type = (String) element.getProperties().get(1);
        }

        if (!type.equals("Clip")) {
            return;
        }

        for (FBXElement fbxElement : element.getChildren()) {
            switch (fbxElement.name) {
                case "RelativeFilename":
                    relativeFilename = (String) fbxElement.getProperties().get(0);
                    break;
            }
        }
        image = createImage();
    }

    private static String getFileName(String filePath) {
        // NOTE: new File().getParent() will not strip forward slashes on all platforms.
        int fwdSlashIdx = filePath.lastIndexOf("/");
        int bkSlashIdx = filePath.lastIndexOf("\\");

        if (fwdSlashIdx != -1) {
            filePath = filePath.substring(fwdSlashIdx + 1);
        } else if (bkSlashIdx != -1) {
            filePath = filePath.substring(bkSlashIdx + 1);
        }

        return filePath;
    }

    @Override
    protected Object toImplObject() {
        String fileName = getFileName(relativeFilename);

        if (fileName == null) {
            System.err.println("Cannot locate image for texture " + name);
            // TODO: Setup placeholder image
            return null;
        }

        // Try to load filename relative to FBX folder
        textureKey = new TextureKey(key.getFolder() + fileName);
        textureKey.setGenerateMips(true);
        return assetManager.loadTexture(textureKey).getImage();
    }

    @Override
    public void link(FBXObject obj) {
        unsupportedConnectObject(obj);
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
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

    public TextureKey getTextureKey() {
        return textureKey;
    }
}
