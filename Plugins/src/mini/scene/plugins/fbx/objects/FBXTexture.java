package mini.scene.plugins.fbx.objects;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.asset.TextureKey;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.material.FBXImage;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.Texture2D;

public class FBXTexture extends FBXObject<Texture> {
    private String bindType;
    private String fileName;
    private Texture texture;
    private FBXImage media;
    private String uvSet;
    private Integer wrapModeU = 0, wrapModeV = 0;

    public FBXTexture(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);
        element.getChildren().stream().filter(fbxElement -> "Type".equals(fbxElement.getName()))
               .forEach(fbxElement -> bindType = (String) fbxElement.getProperties().get(0));

        for (FBXElement property : element.getFBXProperties()) {
            String propertyName = (String) property.getProperties().get(0);
            switch (propertyName) {
                case "UVSet":
                    uvSet = (String) property.getProperties().get(4);
                    break;
                case "WrapModeU":
                    wrapModeU = (Integer) property.getProperties().get(4);
                    break;
                case "WrapModeV":
                    wrapModeV = (Integer) property.getProperties().get(4);
                    break;
            }
        }
    }

    @Override
    protected Texture toImplObject() {
        Image image = null;
        TextureKey key = null;

        if (media != null) {
            image = (Image) media.getImplObject();
            key = media.getTextureKey();
        }

        if (image == null) {
            // TODO: create placeholder image
        }

        Texture2D texture = new Texture2D(image);
        if (key != null) {
            texture.setKey(key);
            texture.setName(key.getName());
            texture.setAnisotropicFilter(key.getAnisotropy());
        }

        texture.setMinFilter(Texture.MinFilter.Trilinear);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        if (wrapModeU == 0) {
            texture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
        }
        if (wrapModeV == 0) {
            texture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
        }

        return texture;
    }

    @Override
    public void link(FBXObject obj) {
        if (obj instanceof FBXImage) {
            this.media = (FBXImage) obj;
        } else {
            unsupportedConnectObject(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        if (this.media == obj) {
            return;
        }

        unsupportedConnectObjectProperty(obj, propertyName);
    }
}
