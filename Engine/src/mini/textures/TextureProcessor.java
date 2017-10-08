package mini.textures;

import mini.asset.TextureKey;

import java.nio.ByteBuffer;

public class TextureProcessor {

    public static Object postProcess(TextureKey key, Object obj) {
        Image img = (Image) obj;
        if (img == null) {
            return null;
        }

        Texture tex;
        if (key.getTextureTypeHint() == Texture.Type.CubeMap) {
            if (key.isFlipY()) {
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
        if (img.hasMipmaps() || key.isGenerateMips()) {
            tex.setMinFilter(Texture.MinFilter.Trilinear);
        }

        tex.setAnisotropicFilter(key.getAnisotropy());
        tex.setName(key.getName());
        tex.setImage(img);
        return tex;
    }

    public Object createClone(Object obj) {
        Texture tex = (Texture) obj;
        return tex.clone();
    }

}
