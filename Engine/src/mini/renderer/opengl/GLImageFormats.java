package mini.renderer.opengl;

import mini.renderer.Caps;
import mini.textures.Image;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.EXTPackedFloat;
import org.lwjgl.opengl.EXTTextureCompressionS3TC;
import org.lwjgl.opengl.EXTTextureSRGB;
import org.lwjgl.opengl.EXTTextureSharedExponent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL43;

import java.util.EnumSet;

/**
 * Generates a table of supported image format for a given renderer caps.
 */
public final class GLImageFormats {
    private GLImageFormats() {
    }

    private static void format(GLImageFormat[][] formatToGL, Image.Format format,
                               int glInternalFormat, int glFormat, int glDataType) {
        formatToGL[0][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType);
    }

    private static void formatSwiz(GLImageFormat[][] formatToGL, Image.Format format,
                                   int glInternalFormat, int glFormat, int glDataType) {
        formatToGL[0][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType,
                                                            false, true);
    }

    private static void formatSrgb(GLImageFormat[][] formatToGL, Image.Format format,
                                   int glInternalFormat, int glFormat, int glDataType) {
        formatToGL[1][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType);
    }

    private static void formatSrgbSwiz(GLImageFormat[][] formatToGL, Image.Format format,
                                       int glInternalFormat, int glFormat, int glDataType) {
        formatToGL[1][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType,
                                                            false, true);
    }

    private static void formatComp(GLImageFormat[][] formatToGL, Image.Format format,
                                   int glCompressedFormat, int glFormat, int glDataType) {
        formatToGL[0][format.ordinal()] = new GLImageFormat(glCompressedFormat, glFormat,
                                                            glDataType, true);
    }

    private static void formatCompSrgb(GLImageFormat[][] formatToGL, Image.Format format,
                                       int glCompressedFormat, int glFormat, int glDataType) {
        formatToGL[1][format.ordinal()] = new GLImageFormat(glCompressedFormat, glFormat,
                                                            glDataType, true);
    }

    /**
     * Generates a list of supported texture formats.
     * <p>
     * The first dimension of the array specifies the colorspace,
     * currently 0 means linear and 1 means sRGB. The second dimension
     * is the ordinal in the {@link Image.Format image format}.
     *
     * @param caps The capabilities for which to determine supported formats.
     * @return An 2D array containing supported texture formats.
     */
    public static GLImageFormat[][] getFormatsForCaps(EnumSet<Caps> caps) {
        GLImageFormat[][] formatToGL = new GLImageFormat[2][Image.Format.values().length];

        // Core Profile Formats (supported by both OpenGL Core 3.3 and OpenGL ES 3.0+)
        if (caps.contains(Caps.CoreProfile)) {
            formatSwiz(formatToGL,     Image.Format.Alpha8,               GL30.GL_R8,           GL11.GL_RED, GL11.GL_UNSIGNED_BYTE);
            formatSwiz(formatToGL,     Image.Format.Luminance8,           GL30.GL_R8,           GL11.GL_RED, GL11.GL_UNSIGNED_BYTE);
            formatSwiz(formatToGL,     Image.Format.Luminance8Alpha8,     GL30.GL_RG8,          GL30.GL_RG,  GL11.GL_UNSIGNED_BYTE);
            formatSwiz(formatToGL,     Image.Format.Luminance16F,         GL30.GL_R16F,         GL11.GL_RED, GL30.GL_HALF_FLOAT);
            formatSwiz(formatToGL,     Image.Format.Luminance32F,         GL30.GL_R32F,         GL11.GL_RED, GL11.GL_FLOAT);
            formatSwiz(formatToGL,     Image.Format.Luminance16FAlpha16F, GL30.GL_RG16F,        GL30.GL_RG,  GL30.GL_HALF_FLOAT);

            formatSrgbSwiz(formatToGL, Image.Format.Luminance8,           GL21.GL_SRGB8,        GL11.GL_RED, GL11.GL_UNSIGNED_BYTE);
            formatSrgbSwiz(formatToGL, Image.Format.Luminance8Alpha8,     GL21.GL_SRGB8_ALPHA8, GL30.GL_RG,  GL11.GL_UNSIGNED_BYTE);
        }

        if (caps.contains(Caps.OpenGL20)) {
            if (!caps.contains(Caps.CoreProfile)) {
                format(formatToGL, Image.Format.Alpha8,           GL11.GL_ALPHA8,            GL11.GL_ALPHA,           GL11.GL_UNSIGNED_BYTE);
                format(formatToGL, Image.Format.Luminance8,       GL11.GL_LUMINANCE8,        GL11.GL_LUMINANCE,       GL11.GL_UNSIGNED_BYTE);
                format(formatToGL, Image.Format.Luminance8Alpha8, GL11.GL_LUMINANCE8_ALPHA8, GL11.GL_LUMINANCE_ALPHA, GL11.GL_UNSIGNED_BYTE);
            }
            format(formatToGL, Image.Format.RGB8,   GL11.GL_RGB8,  GL11.GL_RGB,  GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.RGBA8,  GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.RGB565, GL11.GL_RGB8,  GL11.GL_RGB,  GL12.GL_UNSIGNED_SHORT_5_6_5);

            // Additional desktop-specific formats:
            format(formatToGL, Image.Format.BGR8,   GL11.GL_RGB8,  GL12.GL_BGR,  GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.ARGB8,  GL11.GL_RGBA8, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8);
            format(formatToGL, Image.Format.BGRA8,  GL11.GL_RGBA8, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.ABGR8,  GL11.GL_RGBA8, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8);

            // sRGB formats
            if (caps.contains(Caps.Srgb)) {
                formatSrgb(formatToGL, Image.Format.RGB8,   GL21.GL_SRGB8,        GL11.GL_RGB,  GL11.GL_UNSIGNED_BYTE);
                formatSrgb(formatToGL, Image.Format.RGB565, GL21.GL_SRGB8,        GL11.GL_RGB,  GL12.GL_UNSIGNED_SHORT_5_6_5);
                formatSrgb(formatToGL, Image.Format.RGB5A1, GL21.GL_SRGB8_ALPHA8, GL11.GL_RGBA, GL12.GL_UNSIGNED_SHORT_5_5_5_1);
                formatSrgb(formatToGL, Image.Format.RGBA8,  GL21.GL_SRGB8_ALPHA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
                if (!caps.contains(Caps.CoreProfile)) {
                    formatSrgb(formatToGL, Image.Format.Luminance8,       GL21.GL_SLUMINANCE8,        GL11.GL_LUMINANCE,       GL11.GL_UNSIGNED_BYTE);
                    formatSrgb(formatToGL, Image.Format.Luminance8Alpha8, GL21.GL_SLUMINANCE8_ALPHA8, GL11.GL_LUMINANCE_ALPHA, GL11.GL_UNSIGNED_BYTE);
                }
                formatSrgb(formatToGL, Image.Format.BGR8,   GL21.GL_SRGB8,        GL12.GL_BGR,  GL11.GL_UNSIGNED_BYTE);
                formatSrgb(formatToGL, Image.Format.ABGR8,  GL21.GL_SRGB8_ALPHA8, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8);
                formatSrgb(formatToGL, Image.Format.ARGB8,  GL21.GL_SRGB8_ALPHA8, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8);
                formatSrgb(formatToGL, Image.Format.BGRA8,  GL21.GL_SRGB8_ALPHA8, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);

                if (caps.contains(Caps.TextureCompressionS3TC)) {
                    formatCompSrgb(formatToGL, Image.Format.DXT1,
                                   EXTTextureSRGB.GL_COMPRESSED_SRGB_S3TC_DXT1_EXT, GL11.GL_RGB,
                                   GL11.GL_UNSIGNED_BYTE);
                    formatCompSrgb(formatToGL, Image.Format.DXT1A,
                                   EXTTextureSRGB.GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT,
                                   GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
                    formatCompSrgb(formatToGL, Image.Format.DXT3,
                                   EXTTextureSRGB.GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT,
                                   GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
                    formatCompSrgb(formatToGL, Image.Format.DXT5,
                                   EXTTextureSRGB.GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT,
                                   GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
                }
            }
        } else if (caps.contains(Caps.Rgba8)) {
            // A more limited form of 32-bit RGBA. Only GL_RGBA8 is available.
            if (!caps.contains(Caps.CoreProfile)) {
                format(formatToGL, Image.Format.Alpha8,           GL11.GL_RGBA8,  GL11.GL_ALPHA,           GL11.GL_UNSIGNED_BYTE);
                format(formatToGL, Image.Format.Luminance8,       GL11.GL_RGBA8,  GL11.GL_LUMINANCE,       GL11.GL_UNSIGNED_BYTE);
                format(formatToGL, Image.Format.Luminance8Alpha8, GL11.GL_RGBA8,  GL11.GL_LUMINANCE_ALPHA, GL11.GL_UNSIGNED_BYTE);
            }
            format(formatToGL, Image.Format.RGB8,  GL11.GL_RGBA8,  GL11.GL_RGB,  GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.RGBA8, GL11.GL_RGBA8,  GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        } else {
            // Actually, the internal format isn't used for OpenGL ES 2! This is the same as the above..
            if (!caps.contains(Caps.CoreProfile)) {
                format(formatToGL, Image.Format.Alpha8,           GL11.GL_RGBA4,  GL11.GL_ALPHA,           GL11.GL_UNSIGNED_BYTE);
                format(formatToGL, Image.Format.Luminance8,       GL41.GL_RGB565, GL11.GL_LUMINANCE,       GL11.GL_UNSIGNED_BYTE);
                format(formatToGL, Image.Format.Luminance8Alpha8, GL11.GL_RGBA4,  GL11.GL_LUMINANCE_ALPHA, GL11.GL_UNSIGNED_BYTE);
            }
            format(formatToGL, Image.Format.RGB8,  GL41.GL_RGB565, GL11.GL_RGB,  GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.RGBA8, GL11.GL_RGBA4,  GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        }

        if (caps.contains(Caps.OpenGLES20)) {
            format(formatToGL, Image.Format.RGB565, GL41.GL_RGB565,  GL11.GL_RGB,    GL12.GL_UNSIGNED_SHORT_5_6_5);
        }

        format(formatToGL, Image.Format.RGB5A1, GL11.GL_RGB5_A1, GL11.GL_RGBA, GL12.GL_UNSIGNED_SHORT_5_5_5_1);

        if (caps.contains(Caps.FloatTexture)) {
            if (!caps.contains(Caps.CoreProfile)) {
                format(formatToGL, Image.Format.Luminance16F,         ARBTextureFloat.GL_LUMINANCE16F_ARB,       GL11.GL_LUMINANCE,       GL30.GL_HALF_FLOAT);
                format(formatToGL, Image.Format.Luminance32F,         ARBTextureFloat.GL_LUMINANCE32F_ARB,       GL11.GL_LUMINANCE,       GL11.GL_FLOAT);
                format(formatToGL, Image.Format.Luminance16FAlpha16F, ARBTextureFloat.GL_LUMINANCE_ALPHA16F_ARB, GL11.GL_LUMINANCE_ALPHA, GL30.GL_HALF_FLOAT);
            }
            format(formatToGL, Image.Format.RGB16F, GL30.GL_RGB16F, GL11.GL_RGB,
                   GL30.GL_HALF_FLOAT);
            format(formatToGL, Image.Format.RGB32F, GL30.GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT);
            format(formatToGL, Image.Format.RGBA16F, GL30.GL_RGBA16F, GL11.GL_RGBA,
                   GL30.GL_HALF_FLOAT);
            format(formatToGL, Image.Format.RGBA32F, GL30.GL_RGBA32F, GL11.GL_RGBA, GL11.GL_FLOAT);
        }

        if (caps.contains(Caps.PackedFloatTexture)) {
            format(formatToGL, Image.Format.RGB111110F, EXTPackedFloat.GL_R11F_G11F_B10F_EXT,
                   GL11.GL_RGB, EXTPackedFloat.GL_UNSIGNED_INT_10F_11F_11F_REV_EXT);
            if (caps.contains(Caps.FloatTexture)) {
                format(formatToGL, Image.Format.RGB16F_to_RGB111110F,
                       EXTPackedFloat.GL_R11F_G11F_B10F_EXT, GL11.GL_RGB, GL30.GL_HALF_FLOAT);
            }
        }

        if (caps.contains(Caps.SharedExponentTexture)) {
            format(formatToGL, Image.Format.RGB9E5, EXTTextureSharedExponent.GL_RGB9_E5_EXT,
                   GL11.GL_RGB, EXTTextureSharedExponent.GL_UNSIGNED_INT_5_9_9_9_REV_EXT);
            if (caps.contains(Caps.FloatTexture)) {
                format(formatToGL, Image.Format.RGB16F_to_RGB9E5,
                       EXTTextureSharedExponent.GL_RGB9_E5_EXT, GL11.GL_RGB, GL30.GL_HALF_FLOAT);
            }
        }

        // Need to check if Caps.DepthTexture is supported prior to using for textures
        // But for renderbuffers its OK.
        format(formatToGL, Image.Format.Depth,   GL11.GL_DEPTH_COMPONENT,    GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE);
        format(formatToGL, Image.Format.Depth16, GL14.GL_DEPTH_COMPONENT16,  GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT);

        if (caps.contains(Caps.OpenGL20)) {
            format(formatToGL, Image.Format.Depth24, GL14.GL_DEPTH_COMPONENT24,  GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT);
        }

        if (caps.contains(Caps.FloatDepthBuffer)) {
            format(formatToGL, Image.Format.Depth32, GL30.GL_DEPTH_COMPONENT32F,
                   GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);
        }

        if (caps.contains(Caps.PackedDepthStencilBuffer)) {
            format(formatToGL, Image.Format.Depth24Stencil8,
                   EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT,
                   EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
                   EXTPackedDepthStencil.GL_UNSIGNED_INT_24_8_EXT);
        }

        // Compressed formats
        if (caps.contains(Caps.TextureCompressionS3TC)) {
            formatComp(formatToGL, Image.Format.DXT1,
                       EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT, GL11.GL_RGB,
                       GL11.GL_UNSIGNED_BYTE);
            formatComp(formatToGL, Image.Format.DXT1A,
                       EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT, GL11.GL_RGBA,
                       GL11.GL_UNSIGNED_BYTE);
            formatComp(formatToGL, Image.Format.DXT3,
                       EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT, GL11.GL_RGBA,
                       GL11.GL_UNSIGNED_BYTE);
            formatComp(formatToGL, Image.Format.DXT5,
                       EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT, GL11.GL_RGBA,
                       GL11.GL_UNSIGNED_BYTE);
        }

        if (caps.contains(Caps.TextureCompressionETC2)) {
            formatComp(formatToGL, Image.Format.ETC1, GL43.GL_COMPRESSED_RGB8_ETC2, GL11.GL_RGB,
                       GL11.GL_UNSIGNED_BYTE);
        } else if (caps.contains(Caps.TextureCompressionETC1)) {
            throw new UnsupportedOperationException();
        }

        // Integer formats
        if (caps.contains(Caps.IntegerTexture)) {
            format(formatToGL, Image.Format.R8I, GL30.GL_R8I, GL30.GL_RED_INTEGER, GL11.GL_BYTE);
            format(formatToGL, Image.Format.R8UI, GL30.GL_R8UI, GL30.GL_RED_INTEGER,
                   GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.R16I, GL30.GL_R16I, GL30.GL_RED_INTEGER, GL11.GL_SHORT);
            format(formatToGL, Image.Format.R16UI, GL30.GL_R16UI, GL30.GL_RED_INTEGER,
                   GL11.GL_UNSIGNED_SHORT);
            format(formatToGL, Image.Format.R32I, GL30.GL_R32I, GL30.GL_RED_INTEGER, GL11.GL_INT);
            format(formatToGL, Image.Format.R32UI, GL30.GL_R32UI, GL30.GL_RED_INTEGER,
                   GL11.GL_UNSIGNED_INT);

            format(formatToGL, Image.Format.RG8I, GL30.GL_RG8I, GL30.GL_RG_INTEGER, GL11.GL_BYTE);
            format(formatToGL, Image.Format.RG8UI, GL30.GL_RG8UI, GL30.GL_RG_INTEGER,
                   GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.RG16I, GL30.GL_RG16I, GL30.GL_RG_INTEGER,
                   GL11.GL_SHORT);
            format(formatToGL, Image.Format.RG16UI, GL30.GL_RG16UI, GL30.GL_RG_INTEGER,
                   GL11.GL_UNSIGNED_SHORT);
            format(formatToGL, Image.Format.RG32I, GL30.GL_RG32I, GL30.GL_RG_INTEGER, GL11.GL_INT);
            format(formatToGL, Image.Format.RG32UI, GL30.GL_RG32UI, GL30.GL_RG_INTEGER,
                   GL11.GL_UNSIGNED_INT);

            format(formatToGL, Image.Format.RGB8I, GL30.GL_RGB8I, GL30.GL_RGB_INTEGER,
                   GL11.GL_BYTE);
            format(formatToGL, Image.Format.RGB8UI, GL30.GL_RGB8UI, GL30.GL_RGB_INTEGER,
                   GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.RGB16I, GL30.GL_RGB16I, GL30.GL_RGB_INTEGER,
                   GL11.GL_SHORT);
            format(formatToGL, Image.Format.RGB16UI, GL30.GL_RGB16UI, GL30.GL_RGB_INTEGER,
                   GL11.GL_UNSIGNED_SHORT);
            format(formatToGL, Image.Format.RGB32I, GL30.GL_RGB32I, GL30.GL_RGB_INTEGER,
                   GL11.GL_INT);
            format(formatToGL, Image.Format.RGB32UI, GL30.GL_RGB32UI, GL30.GL_RGB_INTEGER,
                   GL11.GL_UNSIGNED_INT);

            format(formatToGL, Image.Format.RGBA8I, GL30.GL_RGBA8I, GL30.GL_RGBA_INTEGER,
                   GL11.GL_BYTE);
            format(formatToGL, Image.Format.RGBA8UI, GL30.GL_RGBA8UI, GL30.GL_RGBA_INTEGER,
                   GL11.GL_UNSIGNED_BYTE);
            format(formatToGL, Image.Format.RGBA16I, GL30.GL_RGBA16I, GL30.GL_RGBA_INTEGER,
                   GL11.GL_SHORT);
            format(formatToGL, Image.Format.RGBA16UI, GL30.GL_RGBA16UI, GL30.GL_RGBA_INTEGER,
                   GL11.GL_UNSIGNED_SHORT);
            format(formatToGL, Image.Format.RGBA32I, GL30.GL_RGBA32I, GL30.GL_RGBA_INTEGER,
                   GL11.GL_INT);
            format(formatToGL, Image.Format.RGBA32UI, GL30.GL_RGBA32UI, GL30.GL_RGBA_INTEGER,
                   GL11.GL_UNSIGNED_INT);
        }

        return formatToGL;
    }
}
