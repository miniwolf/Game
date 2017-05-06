package mini.textures;

/**
 * @author Joshua Slack
 */
public class Texture2D extends Texture {
    private WrapMode wrapS = WrapMode.EdgeClamp;
    private WrapMode wrapT = WrapMode.EdgeClamp;

    /**
     * Creates a new two-dimensional texture with default attributes.
     */
    public Texture2D(){
        super();
    }

    /**
     * Creates a new two-dimensional texture using the given image.
     * @param img The image to use.
     */
    public Texture2D(Image img){
        super();
        setImage(img);
        if (img.getData(0) == null) {
            setMagFilter(MagFilter.Nearest);
            setMinFilter(MinFilter.NearestNoMipMaps);
        }
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
            default:
                throw new IllegalArgumentException("invalid WrapAxis: " + axis);
        }
    }

    @Override
    public Type getType() {
        return Type.TwoDimensional;
    }
}
