package mini.textures;

import mini.textures.image.ColorSpace;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miniwolf on 22-04-2017.
 */
public class TextureCubeMap extends Texture {
    private WrapMode wrapS = WrapMode.EdgeClamp;
    private WrapMode wrapT = WrapMode.EdgeClamp;
    private WrapMode wrapR = WrapMode.EdgeClamp;
    /**
     * The image stored in the texture
     */
    private Image image = null;

    public TextureCubeMap(Image img){
        super();
        setImage(img);
    }

    public TextureCubeMap(int width, int height, Image.Format format){
        this(createEmptyLayeredImage(width, height, format));
    }

    private static Image createEmptyLayeredImage(int width, int height, Image.Format format) {
        List<ByteBuffer> layers = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            layers.add(null);
        }
        return new Image(format, width, height, 0, layers, ColorSpace.Linear);
    }

    /**
     * <code>setImage</code> sets the image object that defines the texture.
     *
     * @param image
     *            the image that defines the texture.
     */
    public void setImage(Image image) {
        this.image = image;

        // Test if mipmap generation required.
        setMinFilter(getMinFilter());
    }

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for all axis.
     *
     * @param mode
     *            the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException
     *             if mode is null
     */
    @Override
    public void setWrap(WrapMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode can not be null.");
        }
        this.wrapS = mode;
        this.wrapT = mode;
        this.wrapR = mode;
    }

    /**
     * <code>getWrap</code> returns the wrap mode for a given coordinate axis
     * on this texture.
     *
     * @param axis
     *            the axis to return for
     * @return the wrap mode of the texture.
     * @throws IllegalArgumentException
     *             if axis is null
     */
    @Override
    public WrapMode getWrap(WrapAxis axis) {
        switch (axis) {
            case S:
                return wrapS;
            case T:
                return wrapT;
            case R:
                return wrapR;
        }
        throw new IllegalArgumentException("invalid WrapAxis: " + axis);
    }

    @Override
    public Type getType() {
        return Type.CubeMap;
    }
}
