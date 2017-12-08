package mini.textures;

import mini.asset.AssetKey;
import mini.asset.AssetProcessor;
import mini.asset.TextureKey;

import java.nio.ByteBuffer;

public class TextureProcessor implements AssetProcessor {
    @Override
    public <T> T postProcess(AssetKey<T> key, T obj) {
        Image img = (Image) obj;
        if (img == null) {
            return null;
        }

        TextureKey texKey = (TextureKey) key;
        Texture tex;
        if (texKey.getTextureTypeHint() == Texture.Type.CubeMap) {
            if (texKey.isFlipY()) {
                // also flip -y and +y image in cubemap
                ByteBuffer pos_y = img.getData(2);
                img.setData(2, img.getData(3));
                img.setData(3, pos_y);
            }
            tex = new TextureCubeMap();
//        } else if (key.getTextureTypeHint() == Texture.Type.ThreeDimensional) {
//            tex = new Texture3D();
        } else {
            tex = new Texture2D();
        }

        // enable mipmaps if image has them
        // or generate them if requested by user
        if (img.hasMipmaps() || texKey.isGenerateMips()) {
            tex.setMinFilter(Texture.MinFilter.Trilinear);
        }

        tex.setAnisotropicFilter(texKey.getAnisotropy());
        tex.setName(key.getName());
        tex.setImage(img);
        return (T) tex;
    }

    public Object createClone(Object obj) {
        Texture tex = (Texture) obj;
        return tex.clone();
    }

}
