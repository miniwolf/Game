package mini.textures;

import mini.asset.AssetKey;
import mini.asset.TextureKey;

/**
 * <code>Texture</code> defines a texture object to be used to display an
 * image on a piece of geometry. The image to be displayed is defined by the
 * <code>Image</code> class. All attributes required for texture mapping are
 * contained within this class. This includes mipmapping if desired,
 * magnificationFilter options, apply options and correction options. Default
 * values are as follows: minificationFilter - NearestNeighborNoMipMaps,
 * magnificationFilter - NearestNeighbor, wrap - EdgeClamp on S,T and R, apply -
 * Modulate, environment - None.
 *
 * @see mini.textures.Image
 */
public abstract class Texture implements Cloneable {

    public enum Type {

        /**
         * Two dimensional texture (default). A rectangle.
         */
        TwoDimensional,

        /**
         * An array of two dimensional textures.
         */
        TwoDimensionalArray,

        /**
         * Three dimensional texture. (A cube)
         */
        ThreeDimensional,

        /**
         * A set of 6 TwoDimensional textures arranged as faces of a cube facing
         * inwards.
         */
        CubeMap;
    }

    public enum MinFilter {

        /**
         * Nearest neighbor interpolation is the fastest and crudest filtering
         * method - it simply uses the color of the texel closest to the pixel
         * center for the pixel color. While fast, this results in aliasing and
         * shimmering during minification. (GL equivalent: GL_NEAREST)
         */
        NearestNoMipMaps(false),

        /**
         * In this method the four nearest texels to the pixel center are
         * sampled (at texture level 0), and their colors are combined by
         * weighted averages. Though smoother, without mipmaps it suffers the
         * same aliasing and shimmering problems as nearest
         * NearestNeighborNoMipMaps. (GL equivalent: GL_LINEAR)
         */
        BilinearNoMipMaps(false),

        /**
         * Same as NearestNeighborNoMipMaps except that instead of using samples
         * from texture level 0, the closest mipmap level is chosen based on
         * distance. This reduces the aliasing and shimmering significantly, but
         * does not help with blockiness. (GL equivalent:
         * GL_NEAREST_MIPMAP_NEAREST)
         */
        NearestNearestMipMap(true),

        /**
         * Same as BilinearNoMipMaps except that instead of using samples from
         * texture level 0, the closest mipmap level is chosen based on
         * distance. By using mipmapping we avoid the aliasing and shimmering
         * problems of BilinearNoMipMaps. (GL equivalent:
         * GL_LINEAR_MIPMAP_NEAREST)
         */
        BilinearNearestMipMap(true),

        /**
         * Similar to NearestNeighborNoMipMaps except that instead of using
         * samples from texture level 0, a sample is chosen from each of the
         * closest (by distance) two mipmap levels. A weighted average of these
         * two samples is returned. (GL equivalent: GL_NEAREST_MIPMAP_LINEAR)
         */
        NearestLinearMipMap(true),

        /**
         * Trilinear filtering is a remedy to a common artifact seen in
         * mipmapped bilinearly filtered images: an abrupt and very noticeable
         * change in quality at boundaries where the renderer switches from one
         * mipmap level to the next. Trilinear filtering solves this by doing a
         * texture lookup and bilinear filtering on the two closest mipmap
         * levels (one higher and one lower quality), and then linearly
         * interpolating the results. This results in a smooth degradation of
         * texture quality as distance from the viewer increases, rather than a
         * series of sudden drops. Of course, closer than Level 0 there is only
         * one mipmap level available, and the algorithm reverts to bilinear
         * filtering (GL equivalent: GL_LINEAR_MIPMAP_LINEAR)
         */
        Trilinear(true);

        private final boolean usesMipMapLevels;

        private MinFilter(boolean usesMipMapLevels) {
            this.usesMipMapLevels = usesMipMapLevels;
        }

        public boolean usesMipMapLevels() {
            return usesMipMapLevels;
        }
    }

    public enum MagFilter {

        /**
         * Nearest neighbor interpolation is the fastest and crudest filtering
         * mode - it simply uses the color of the texel closest to the pixel
         * center for the pixel color. While fast, this results in texture
         * 'blockiness' during magnification. (GL equivalent: GL_NEAREST)
         */
        Nearest,

        /**
         * In this mode the four nearest texels to the pixel center are sampled
         * (at the closest mipmap level), and their colors are combined by
         * weighted average according to distance. This removes the 'blockiness'
         * seen during magnification, as there is now a smooth gradient of color
         * change from one texel to the next, instead of an abrupt jump as the
         * pixel center crosses the texel boundary. (GL equivalent: GL_LINEAR)
         */
        Bilinear;

    }

    public enum WrapMode {
        /**
         * Only the fractional portion of the coordinate is considered.
         */
        Repeat,

        /**
         * Only the fractional portion of the coordinate is considered, but if
         * the integer portion is odd, we'll use 1 - the fractional portion.
         * (Introduced around OpenGL1.4) Falls back on Repeat if not supported.
         */
        MirroredRepeat,

        /**
         * coordinate will be clamped to [0,1]
         *
         * @deprecated Not supported by OpenGL 3
         */
        @Deprecated
        Clamp,
        /**
         * mirrors and clamps the texture coordinate, where mirroring and
         * clamping a value f computes:
         * <code>mirrorClamp(f) = min(1, max(1/(2*N),
         * abs(f)))</code> where N
         * is the size of the one-, two-, or three-dimensional texture image in
         * the direction of wrapping. (Introduced after OpenGL1.4) Falls back on
         * Clamp if not supported.
         *
         * @deprecated Not supported by OpenGL 3
         */
        @Deprecated
        MirrorClamp,

        /**
         * coordinate will be clamped to the range [-1/(2N), 1 + 1/(2N)] where N
         * is the size of the texture in the direction of clamping. Falls back
         * on Clamp if not supported.
         *
         * @deprecated Not supported by OpenGL 3 or OpenGL ES 2
         */
        @Deprecated
        BorderClamp,
        /**
         * Wrap mode MIRROR_CLAMP_TO_BORDER_EXT mirrors and clamps to border the
         * texture coordinate, where mirroring and clamping to border a value f
         * computes:
         * <code>mirrorClampToBorder(f) = min(1+1/(2*N), max(1/(2*N), abs(f)))</code>
         * where N is the size of the one-, two-, or three-dimensional texture
         * image in the direction of wrapping." (Introduced after OpenGL1.4)
         * Falls back on BorderClamp if not supported.
         *
         * @deprecated Not supported by OpenGL 3
         */
        @Deprecated
        MirrorBorderClamp,
        /**
         * coordinate will be clamped to the range [1/(2N), 1 - 1/(2N)] where N
         * is the size of the texture in the direction of clamping. Falls back
         * on Clamp if not supported.
         */
        EdgeClamp,

        /**
         * mirrors and clamps to edge the texture coordinate, where mirroring
         * and clamping to edge a value f computes:
         * <code>mirrorClampToEdge(f) = min(1-1/(2*N), max(1/(2*N), abs(f)))</code>
         * where N is the size of the one-, two-, or three-dimensional texture
         * image in the direction of wrapping. (Introduced after OpenGL1.4)
         * Falls back on EdgeClamp if not supported.
         *
         * @deprecated Not supported by OpenGL 3
         */
        MirrorEdgeClamp;
    }

    public enum WrapAxis {
        /**
         * S wrapping (u or "horizontal" wrap)
         */
        S,
        /**
         * T wrapping (v or "vertical" wrap)
         */
        T,
        /**
         * R wrapping (w or "depth" wrap)
         */
        R;
    }

    /**
     * If this texture is a depth texture (the format is Depth*) then
     * this value may be used to compare the texture depth to the R texture
     * coordinate.
     */
    public enum ShadowCompareMode {
        /**
         * Shadow comparison mode is disabled.
         * Texturing is done normally.
         */
        Off,

        /**
         * Compares the 3rd texture coordinate R to the value
         * in this depth texture. If R <= texture value then result is 1.0,
         * otherwise, result is 0.0. If filtering is set to bilinear or trilinear
         * the implementation may sample the texture multiple times to provide
         * smoother results in the range [0, 1].
         */
        LessOrEqual,

        /**
         * Compares the 3rd texture coordinate R to the value
         * in this depth texture. If R >= texture value then result is 1.0,
         * otherwise, result is 0.0. If filtering is set to bilinear or trilinear
         * the implementation may sample the texture multiple times to provide
         * smoother results in the range [0, 1].
         */
        GreaterOrEqual
    }

    /**
     * The name of the texture (if loaded as a resource).
     */
    private String name = null;

    /**
     * The image stored in the texture
     */
    private Image image = null;

    /**
     * The texture key allows to reload a texture from a file
     * if needed.
     */
    private TextureKey key = null;

    private MinFilter minificationFilter = MinFilter.BilinearNoMipMaps;
    private MagFilter magnificationFilter = MagFilter.Bilinear;
    private ShadowCompareMode shadowCompareMode = ShadowCompareMode.Off;
    private int anisotropicFilter;

    /**
     * @return A cloned Texture object.
     */
    @Override
    public Texture clone(){
        try {
            return (Texture) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     * Constructor instantiates a new <code>Texture</code> object with default
     * attributes.
     */
    public Texture() {
    }

    /**
     * @return the MinificationFilterMode of this texture.
     */
    public MinFilter getMinFilter() {
        return minificationFilter;
    }

    /**
     * @param minificationFilter
     *            the new MinificationFilterMode for this texture.
     * @throws IllegalArgumentException
     *             if minificationFilter is null
     */
    public void setMinFilter(MinFilter minificationFilter) {
        if (minificationFilter == null) {
            throw new IllegalArgumentException(
                    "minificationFilter can not be null.");
        }
        this.minificationFilter = minificationFilter;
        if (minificationFilter.usesMipMapLevels() && image != null && !image.isGeneratedMipmapsRequired() && !image.hasMipmaps()) {
            image.setNeedGeneratedMipmaps();
        }
    }

    /**
     * @return the MagnificationFilterMode of this texture.
     */
    public MagFilter getMagFilter() {
        return magnificationFilter;
    }

    /**
     * @param magnificationFilter
     *            the new MagnificationFilter for this texture.
     * @throws IllegalArgumentException
     *             if magnificationFilter is null
     */
    public void setMagFilter(MagFilter magnificationFilter) {
        if (magnificationFilter == null) {
            throw new IllegalArgumentException(
                    "magnificationFilter can not be null.");
        }
        this.magnificationFilter = magnificationFilter;
    }

    /**
     * @return The ShadowCompareMode of this texture.
     * @see ShadowCompareMode
     */
    public ShadowCompareMode getShadowCompareMode(){
        return shadowCompareMode;
    }

    /**
     * @param compareMode
     *            the new ShadowCompareMode for this texture.
     * @throws IllegalArgumentException
     *             if compareMode is null
     * @see ShadowCompareMode
     */
    public void setShadowCompareMode(ShadowCompareMode compareMode){
        if (compareMode == null){
            throw new IllegalArgumentException(
                    "compareMode can not be null.");
        }
        this.shadowCompareMode = compareMode;
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
     * @param key The texture key that was used to load this texture
     */
    public void setKey(AssetKey key){
        this.key = (TextureKey) key;
    }

    public AssetKey getKey(){
        return this.key;
    }

    /**
     * <code>getImage</code> returns the image data that makes up this
     * texture. If no image data has been set, this will return null.
     *
     * @return the image data that makes up the texture.
     */
    public Image getImage() {
        return image;
    }

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for a
     * particular axis.
     *
     * @param axis
     *            the texture axis to define a wrapmode on.
     * @param mode
     *            the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException
     *             if axis or mode are null or invalid for this type of texture
     */
    public abstract void setWrap(WrapAxis axis, WrapMode mode);

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for all axis.
     *
     * @param mode
     *            the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException
     *             if mode is null or invalid for this type of texture
     */
    public abstract void setWrap(WrapMode mode);

    /**
     * <code>getWrap</code> returns the wrap mode for a given coordinate axis
     * on this texture.
     *
     * @param axis
     *            the axis to return for
     * @return the wrap mode of the texture.
     * @throws IllegalArgumentException
     *             if axis is null or invalid for this type of texture
     */
    public abstract WrapMode getWrap(WrapAxis axis);

    public abstract Type getType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the anisotropic filtering level for this texture. Default value
     * is 0 (use value from config),
     * 1 means 1x (no anisotropy), 2 means x2, 4 is x4, etc.
     */
    public int getAnisotropicFilter() {
        return anisotropicFilter;
    }

    /**
     * @param level
     *            the anisotropic filtering level for this texture.
     */
    public void setAnisotropicFilter(int level) {
        anisotropicFilter = Math.max(0, level);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[name=").append(name);
        if (image != null) {
            sb.append(", image=").append(image.toString());
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Texture other = (Texture) obj;

        // NOTE: Since images are generally considered unique assets in jME3,
        // using the image's equals() implementation is not neccessary here
        // (would be too slow)
        return this.image == other.image && this.minificationFilter == other.minificationFilter
                && this.magnificationFilter == other.magnificationFilter
                && this.shadowCompareMode == other.shadowCompareMode
                && this.anisotropicFilter == other.anisotropicFilter;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        // NOTE: Since images are generally considered unique assets in jME3,
        // using the image's hashCode() implementation is not neccessary here
        // (would be too slow)
        hash = 67 * hash + (this.image != null ? System.identityHashCode(this.image) : 0);
        hash = 67 * hash + (this.minificationFilter != null ? this.minificationFilter.hashCode() : 0);
        hash = 67 * hash + (this.magnificationFilter != null ? this.magnificationFilter.hashCode() : 0);
        hash = 67 * hash + (this.shadowCompareMode != null ? this.shadowCompareMode.hashCode() : 0);
        hash = 67 * hash + this.anisotropicFilter;
        return hash;
    }
}
