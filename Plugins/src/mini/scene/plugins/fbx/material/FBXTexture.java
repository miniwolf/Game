package mini.scene.plugins.fbx.material;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.asset.TextureKey;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.Texture2D;

public class FBXTexture extends FBXObject<Texture> {
    private FBXImage media;
    private String type;
    private String uvSet;
    private int wrapModeU;
    private int wrapModeV;

    public FBXTexture(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);
        if (!subclassName.equals("")) {
            return;
        }

        type = element.getChildren().stream().filter(fbxElement -> fbxElement.getName().equals("Type"))
                .map(child -> (String) child.getProperties().get(0)).findFirst().get();
//        for (FBXElement fbxElement : element.getChildren()) {
//            if (fbxElement.getName().equals("Type")) {
//                type = (String) fbxElement.getProperties().get(0);
//            }
//        } // TODO: Might perform better

        for (FBXElement property : element.getFBXProperties()) {
            String propertyName = (String) property.getProperties().get(0);
            if (propertyName.equals("UVSet")) {
                uvSet = (String) property.getProperties().get(4);
            } else if (propertyName.equals("WrapModeU")) {
                wrapModeU = (Integer) property.getProperties().get(4);
            } else if (propertyName.equals("WrapModeV")) {
                wrapModeV = (Integer) property.getProperties().get(4);
            }
        }
    }

    @Override
    protected Texture toImplObject() {
        Image image = null;
        TextureKey key = null;
        if (media != null) {
            image = (Image) media.toImplObject();
            key = media.getTextureKey();
        }
        if (image == null) {
            throw new IllegalStateException("Missing picture for texture...");
        }
        Texture2D tex = new Texture2D(image);
        if (key != null) {
            tex.setKey(key);
            tex.setName(key.getName());
            tex.setAnisotropicFilter(key.getAnisotropy());
        }
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        tex.setMagFilter(Texture.MagFilter.Bilinear);
        if (0 == wrapModeU) {
            tex.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
        }
        if (0 == wrapModeV) {
            tex.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
        }
        return tex;
    }

    @Override
    public void link(FBXObject obj) {
        if (!(obj instanceof FBXImage)) {
            unsupportedConnectObject(obj);
        }

        this.media = (FBXImage) obj;
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        if (propertyName.equals("VideoProperty")) {
            return;
        }
        throw new UnsupportedOperationException();
    }
}
