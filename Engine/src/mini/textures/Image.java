package mini.textures;

import mini.math.FastMath;
import mini.textures.image.ColorSpace;
import mini.textures.image.LastTextureState;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miniwolf on 22-04-2017.
 */
public class Image {
    public enum Format {
        /**
         * 8-bit alpha
         */
        Alpha8(8),

        /**
         * 8-bit grayscale/luminance.
         */
        Luminance8(8),

        /**
         * half-precision floating-point grayscale/luminance.
         * <p>
         * Requires {@link Caps#FloatTexture}.
         */
        Luminance16F(16, true),

        /**
         * single-precision floating-point grayscale/luminance.
         * <p>
         * Requires {@link Caps#FloatTexture}.
         */
        Luminance32F(32, true),

        /**
         * 8-bit luminance/grayscale and 8-bit alpha.
         */
        Luminance8Alpha8(16),

        /**
         * half-precision floating-point grayscale/luminance and alpha.
         * <p>
         * Requires {@link Caps#FloatTexture}.
         */
        Luminance16FAlpha16F(32, true),

        /**
         * 8-bit blue, green, and red.
         */
        BGR8(24), // BGR and ABGR formats are often used on windows systems

        /**
         * 8-bit red, green, and blue.
         */
        RGB8(24),

        /**
         * 5-bit red, 6-bit green, and 5-bit blue.
         */
        RGB565(16),

        /**
         * 5-bit red, green, and blue with 1-bit alpha.
         */
        RGB5A1(16),

        /**
         * 8-bit red, green, blue, and alpha.
         */
        RGBA8(32),

        /**
         * 8-bit alpha, blue, green, and red.
         */
        ABGR8(32),

        /**
         * 8-bit alpha, red, blue and green
         */
        ARGB8(32),

        /**
         * 8-bit blue, green, red and alpha.
         */
        BGRA8(32),

        /**
         * Arbitrary depth format. The precision is chosen by the video
         * hardware.
         */
        Depth(0, true, false, false),

        /**
         * 16-bit depth.
         */
        Depth16(16, true, false, false),

        /**
         * 24-bit depth.
         */
        Depth24(24, true, false, false),

        /**
         * 32-bit depth.
         */
        Depth32(32, true, false, false),

        /**
         * half-precision floating point red, green, and blue.
         * <p>
         * Requires {@link Caps#FloatTexture}.
         */
        RGB16F(48, true),

        /**
         * half-precision floating point red, green, blue, and alpha.
         * <p>
         * Requires {@link Caps#FloatTexture}.
         */
        RGBA16F(64, true),

        /**
         * single-precision floating point red, green, and blue.
         * <p>
         * Requires {@link Caps#FloatTexture}.
         */
        RGB32F(96, true),

        /**
         * single-precision floating point red, green, blue and alpha.
         * <p>
         * Requires {@link Caps#FloatTexture}.
         */
        RGBA32F(128, true);

        private int bpp;
        private boolean isDepth;
        private boolean isCompressed;
        private boolean isFloatingPoint;

        Format(int bpp) {
            this.bpp = bpp;
        }

        Format(int bpp, boolean isFP) {
            this(bpp);
            this.isFloatingPoint = isFP;
        }

        Format(int bpp, boolean isDepth, boolean isCompressed, boolean isFP) {
            this(bpp, isFP);
            this.isDepth = isDepth;
            this.isCompressed = isCompressed;
        }

        /**
         * @return bits per pixel.
         */
        public int getBitsPerPixel() {
            return bpp;
        }

        /**
         * @return True if this format is a depth format, false otherwise.
         */
        public boolean isDepthFormat() {
            return isDepth;
        }

        /**
         * @return True if this is a compressed image format, false if
         * uncompressed.
         */
        public boolean isCompressed() {
            return isCompressed;
        }

        /**
         * @return True if this image format is in floating point,
         * false if it is an integer format.
         */
        public boolean isFloatingPont() {
            return isFloatingPoint;
        }
    }

    // image attributes
    protected Format format;
    protected int width, height, depth;
    private int id = -1;
    protected int multiSamples = 1;
    protected int[] mipMapSizes;
    protected List<ByteBuffer> data;
    protected ColorSpace colorSpace = null;
    private boolean updateNeeded;

    // attributes relating to GL object
    protected boolean needGeneratedMips = false;
    protected boolean mipsWereGenerated = false;
    protected LastTextureState lastTextureState = new LastTextureState();

    /**
     * Constructor instantiates a new <code>Image</code> object. All values
     * are undefined.
     */
    public Image() {
        super();
        data = new ArrayList<>(1);
    }

    /**
     * Constructor instantiates a new <code>Image</code> object. The
     * attributes of the image are defined during construction.
     *
     * @param format      the data format of the image.
     * @param width       the width of the image.
     * @param height      the height of the image.
     * @param data        the image data.
     * @param mipMapSizes the array of mipmap sizes, or null for no mipmaps.
     * @param colorSpace
     * @see ColorSpace the colorSpace of the image
     */
    public Image(Format format, int width, int height, int depth, List<ByteBuffer> data,
                 int[] mipMapSizes, ColorSpace colorSpace) {

        this();

        if (mipMapSizes != null) {
            if (mipMapSizes.length <= 1) {
                mipMapSizes = null;
            } else {
                needGeneratedMips = false;
                mipsWereGenerated = true;
            }
        }

        setFormat(format);
        this.width = width;
        this.height = height;
        this.data = data;
        this.depth = depth;
        this.mipMapSizes = mipMapSizes;
        this.colorSpace = colorSpace;
    }

    /**
     * Constructor instantiates a new <code>Image</code> object. The
     * attributes of the image are defined during construction.
     *
     * @param format      the data format of the image.
     * @param width       the width of the image.
     * @param height      the height of the image.
     * @param data        the image data.
     * @param mipMapSizes the array of mipmap sizes, or null for no mipmaps.
     * @param colorSpace
     * @see ColorSpace the colorSpace of the image
     */
    public Image(Format format, int width, int height, ByteBuffer data,
                 int[] mipMapSizes, ColorSpace colorSpace) {

        this();

        if (mipMapSizes != null && mipMapSizes.length <= 1) {
            mipMapSizes = null;
        } else {
            needGeneratedMips = false;
            mipsWereGenerated = true;
        }

        setFormat(format);
        this.width = width;
        this.height = height;
        if (data != null) {
            this.data = new ArrayList<>(1);
            this.data.add(data);
        }
        this.mipMapSizes = mipMapSizes;
        this.colorSpace = colorSpace;
    }

    /**
     * Constructor instantiates a new <code>Image</code> object. The
     * attributes of the image are defined during construction.
     *
     * @param format     the data format of the image.
     * @param width      the width of the image.
     * @param height     the height of the image.
     * @param data       the image data.
     * @param colorSpace
     * @see ColorSpace the colorSpace of the image
     */
    public Image(Format format, int width, int height, int depth, List<ByteBuffer> data,
                 ColorSpace colorSpace) {
        this(format, width, height, depth, data, null, colorSpace);
    }

    /**
     * Constructor instantiates a new <code>Image</code> object. The
     * attributes of the image are defined during construction.
     *
     * @param format     the data format of the image.
     * @param width      the width of the image.
     * @param height     the height of the image.
     * @param data       the image data.
     * @param colorSpace
     * @see ColorSpace the colorSpace of the image
     */
    public Image(Format format, int width, int height, ByteBuffer data, ColorSpace colorSpace) {
        this(format, width, height, data, null, colorSpace);
    }

    /**
     * Internal use only.
     * The renderer stores the texture state set from the last texture
     * so it doesn't have to change it unless necessary.
     *
     * @return The image parameter state.
     */
    public LastTextureState getLastTextureState() {
        return lastTextureState;
    }

    /**
     * Internal use only.
     * The renderer marks which images have generated mipmaps in VRAM
     * and which do not, so it can generate them as needed.
     *
     * @param generated If mipmaps were generated or not.
     */
    public void setMipmapsGenerated(boolean generated) {
        this.mipsWereGenerated = generated;
    }

    /**
     * Internal use only.
     * Check if the renderer has generated mipmaps for this image in VRAM
     * or not.
     *
     * @return If mipmaps were generated already.
     */
    public boolean isMipmapsGenerated() {
        return mipsWereGenerated;
    }

    /**
     * (Package private) Called by {@link Texture} when
     * {@link #isMipmapsGenerated() } is false in order to generate
     * mipmaps for this image.
     */
    void setNeedGeneratedMipmaps() {
        needGeneratedMips = true;
    }

    /**
     * Returns whether the image data contains mipmaps.
     *
     * @return true if the image data contains mipmaps, false if not.
     */
    public boolean hasMipmaps() {
        return mipMapSizes != null;
    }

    /**
     * @return True if the image needs to have mipmaps generated
     * for it (as requested by the texture). This stays true even
     * after mipmaps have been generated.
     */
    public boolean isGeneratedMipmapsRequired() {
        return needGeneratedMips;
    }

    /**
     * @return The number of samples (for multisampled textures).
     * @see Image#setMultiSamples(int)
     */
    public int getMultiSamples() {
        return multiSamples;
    }

    /**
     * @param multiSamples Set the number of samples to use for this image,
     * setting this to a value higher than 1 turns this image/texture
     * into a multisample texture (on OpenGL3.1 and higher).
     */
    public void setMultiSamples(int multiSamples) {
        if (multiSamples <= 0) {
            throw new IllegalArgumentException("multiSamples must be > 0");
        }

        if (getData(0) != null) {
            throw new IllegalArgumentException("Cannot upload data as multisample texture");
        }

        if (hasMipmaps()) {
            throw new IllegalArgumentException("Multisample textures do not support mipmaps");
        }

        this.multiSamples = multiSamples;
    }

    public void addData(ByteBuffer data) {
        if (this.data == null) {
            this.data = new ArrayList<>(1);
        }
        this.data.add(data);
        setUpdateNeeded();
    }

    /**
     * <code>getData</code> returns the data for this image. If the data is
     * undefined, null will be returned.
     *
     * @return the data for this image.
     */
    public List<ByteBuffer> getData() {
        return data;
    }

    /**
     * <code>getData</code> returns the data for this image. If the data is
     * undefined, null will be returned.
     *
     * @return the data for this image.
     */
    public ByteBuffer getData(int index) {
        return data.size() > index ? data.get(index) : null;
    }

    /**
     * Returns the mipmap sizes for this image.
     *
     * @return the mipmap sizes for this image.
     */
    public int[] getMipMapSizes() {
        return mipMapSizes;
    }

    /**
     * image loader is responsible for setting this attribute based on the color
     * space in which the image has been encoded with. In the majority of cases,
     * this flag will be set to sRGB by default since many image formats do not
     * contain any color space information and the most frequently used colors
     * space is sRGB
     *
     * The material loader may override this attribute to Lineat if it determines that
     * such conversion must not be performed, for example, when loading normal
     * maps.
     *
     * @param colorSpace @see ColorSpace. Set to sRGB to enable srgb -&gt; linear
     * conversion, Linear otherwise.
     *
     * @seealso Renderer#setLinearizeSrgbImages(boolean)
     *
     */
    public void setColorSpace(ColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }

    /**
     * Specifies that this image is an SRGB image and therefore must undergo an
     * sRGB -&gt; linear RGB color conversion prior to being read by a shader and
     * with the {@link Renderer#setLinearizeSrgbImages(boolean)} option is
     * enabled.
     *
     * This option is only supported for the 8-bit color and grayscale image
     * formats. Determines if the image is in SRGB color space or not.
     *
     * @return True, if the image is an SRGB image, false if it is linear RGB.
     *
     * @seealso Renderer#setLinearizeSrgbImages(boolean)
     */
    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    /**
     * <code>setFormat</code> sets the image format for this image.
     *
     * @param format the image format.
     * @throws NullPointerException if format is null
     * @see Format
     */
    public void setFormat(Format format) {
        if (format == null) {
            throw new NullPointerException("format may not be null.");
        }

        this.format = format;
        setUpdateNeeded();
    }

    /**
     * <code>getFormat</code> returns the image format for this image.
     *
     * @return the image format.
     * @see Format
     */
    public Format getFormat() {
        return format;
    }

    /**
     * <code>getWidth</code> returns the width of this image.
     *
     * @return the width of this image.
     */
    public int getWidth() {
        return width;
    }

    /**
     * <code>getHeight</code> returns the height of this image.
     *
     * @return the height of this image.
     */
    public int getHeight() {
        return height;
    }

    /**
     * <code>getDepth</code> returns the depth of this image (for 3d images).
     *
     * @return the depth of this image.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Determine if the image is NPOT.
     *
     * @return if the image is a non-power-of-2 image, e.g. having dimensions
     * that are not powers of 2.
     */
    public boolean isNPOT() {
        return width != 0 && height != 0
               && (!FastMath.isPowerOfTwo(width) || !FastMath.isPowerOfTwo(height));
    }

    /**
     * Sets the update needed flag, while also checking if mipmaps
     * need to be regenerated.
     */
    public void setUpdateNeeded() {
        updateNeeded = true;
        if (isGeneratedMipmapsRequired() && !hasMipmaps()) {
            // Mipmaps are no longer valid, since the image was changed.
            setMipmapsGenerated(false);
        }
    }

    public void clearUpdateNeeded() {
        updateNeeded = false;
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
