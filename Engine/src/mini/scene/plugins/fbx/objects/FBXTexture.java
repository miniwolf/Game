package mini.scene.plugins.fbx.objects;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.textures.Texture;
import mini.textures.Texture2D;

public class FBXTexture extends FBXObject {
    private String bindType;
    private String fileName;
    private Texture texture;

    public FBXTexture(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);
        for (FBXElement fbxElement : element.getChildren()) {
            switch (fbxElement.getName()) {
                case "RelativeFilename":
                    fileName = (String) fbxElement.getProperties().get(0);
                    break;
                case "Type":
                    bindType = (String) fbxElement.getProperties().get(0);
                    break;
            }
        }

        texture = new Texture2D();
        texture.setName(name);
        texture.setWrap(Texture.WrapMode.Repeat);
    }

    @Override
    public void link(FBXObject obj) {
        if (obj instanceof FBXImage) {
            FBXImage other = (FBXImage) obj;
            if (other.getImage() == null) {
                return;
            }
            texture.setImage(other.getImage());
        } else {
            System.out.println(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        System.out.println(obj);
    }

    public Texture getTexture() {
        return texture;
    }
}
