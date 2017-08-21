package mini.textures;

import mini.textures.image.ColorSpace;

/**
 * @author Joshua Slack
 */
public class Texture2D extends Texture {
    private WrapMode wrapS = WrapMode.EdgeClamp;
    private WrapMode wrapT = WrapMode.EdgeClamp;

    /**
     * Creates a new two-dimensional texture with default attributes.
     */
    public Texture2D() {
        super();
    }

    /**
     * Creates a new two-dimensional texture using the given image.
     *
     * @param img The image to use.
     */
    public Texture2D(Image img) {
        super();
        setImage(img);
        if (img.getData(0) == null) {
            setMagFilter(MagFilter.Nearest);
            setMinFilter(MinFilter.NearestNoMipMaps);
        }
    }

    /**
     * Creates a new two-dimensional texture for the purpose of offscreen
     * rendering.
     *
     * @param width
     * @param height
     * @param format
     * @see mini.textures.FrameBuffer
     */
    public Texture2D(int width, int height, Image.Format format) {
        this(new Image(format, width, height, null, ColorSpace.Linear));
    }

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for a
     * particular axis.
     *
     * @param axis the texture axis to define a wrapmode on.
     * @param mode the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException if axis or mode are null
     */
    public void setWrap(WrapAxis axis, WrapMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode can not be null.");
        } else if (axis == null) {
            throw new IllegalArgumentException("axis can not be null.");
        }
        switch (axis) {
            case S:
                this.wrapS = mode;
                break;
            case T:
                this.wrapT = mode;
                break;
            default:
                throw new IllegalArgumentException("Not applicable for 2D textures");
        }
    }

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for all axis.
     *
     * @param mode the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException if mode is null
     */
    @Override
    public void setWrap(WrapMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode can not be null.");
        }
        this.wrapS = mode;
        this.wrapT = mode;
    }

    /**
     * <code>getWrap</code> returns the wrap mode for a given coordinate axis
     * on this texture.
     *
     * @param axis the axis to return for
     * @return the wrap mode of the texture.
     * @throws IllegalArgumentException if axis is null
     */
    @Override
    public WrapMode getWrap(WrapAxis axis) {
        switch (axis) {
            case S:
                return wrapS;
            case T:
                return wrapT;
            default:
                throw new IllegalArgumentException("invalid WrapAxis: " + axis);
        }
    }

    @Override
    public Type getType() {
        return Type.TwoDimensional;
    }
}
