package loaders;

import mini.scene.Skin;
import mini.textures.Texture;
import mini.utils.MyFile;

public class SkinLoader {
    protected Skin loadSkin(MyFile diffuseFile){
        Texture diffuse = Texture.newTexture(diffuseFile).anisotropic().create();
        return new Skin(diffuse, null);
    }

    protected Skin loadSkin(MyFile diffuseFile, MyFile extraMapFile){
        Texture diffuse = Texture.newTexture(diffuseFile).anisotropic().create();
        Texture extraMap = Texture.newTexture(extraMapFile).create();
        return new Skin(diffuse, extraMap);
    }
}
