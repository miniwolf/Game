package mini.material;

import mini.shaders.VarType;
import mini.textures.Texture;
import mini.textures.image.ColorSpace;

/**
 * Created by miniwolf on 16-04-2017.
 */
public class MatParamTexture extends MatParam {
    private ColorSpace colorSpace;
    private Texture texture;

    public MatParamTexture(VarType type, String name, Texture texture, ColorSpace colorSpace) {
        super(type, name, texture);
        this.texture = texture;
        this.colorSpace = colorSpace;
    }

    /**
     * @return the texture required by this texture param
     */
    public Texture getTextureValue() {
        return texture;
    }

    public void setTextureValue(Texture value) {
        this.value = value;
        this.texture = value;
    }

    /**
     * @return the color space required by this texture param
     */
    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    /**
     * Set to {@link ColorSpace#Linear} if the texture color space has to be forced to linear
     * instead of sRGB
     *
     * @param colorSpace @see ColorSpace
     */
    public void setColorSpace(ColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }
}
