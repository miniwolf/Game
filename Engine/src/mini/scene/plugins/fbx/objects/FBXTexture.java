package mini.scene.plugins.fbx.objects;

import mini.scene.plugins.fbx.file.FBXElement;
import mini.textures.Texture;
import mini.textures.Texture2D;

public class FBXTexture extends FBXObject {
    private String bindType;
    private String fileName;

    public FBXTexture(FBXElement element) {
        super(element);
        initializeElement();
    }

    protected void initializeElement() {
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

        Texture texture = new Texture2D();
        texture.setName(name);
        texture.setWrap(Texture.WrapMode.Repeat);
    }
}
