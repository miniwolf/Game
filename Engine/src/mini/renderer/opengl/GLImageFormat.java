package mini.renderer.opengl;

/**
 * Describes an OpenGL image format.
 *
 * @author miniwolf
 */
public class GLImageFormat {
    public final int internalFormat;
    public final int format;
    public final int dataType;
    public final boolean compressed;
    public final boolean swizzleRequired;

    /**
     * Constructor for formats.
     *
     * @param internalFormat OpenGL internal format
     * @param format         OpenGL format
     * @param dataType       OpenGL datatype
     */
    public GLImageFormat(int internalFormat, int format, int dataType) {
        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
        this.compressed = false;
        this.swizzleRequired = false;
    }

    /**
     * Constructor for formats.
     *
     * @param internalFormat OpenGL internal format
     * @param format         OpenGL format
     * @param dataType       OpenGL datatype
     * @param compressed     Format is compressed
     */
    public GLImageFormat(int internalFormat, int format, int dataType, boolean compressed) {
        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
        this.compressed = compressed;
        this.swizzleRequired = false;
    }

    /**
     * Constructor for formats.
     *
     * @param internalFormat  OpenGL internal format
     * @param format          OpenGL format
     * @param dataType        OpenGL datatype
     * @param compressed      Format is compressed
     * @param swizzleRequired Need to use texture swizzle to upload texture
     */
    public GLImageFormat(int internalFormat, int format, int dataType, boolean compressed, boolean swizzleRequired) {
        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
        this.compressed = compressed;
        this.swizzleRequired = swizzleRequired;
    }
}
