package mini.renderer.opengl;

import mini.renderer.Caps;
import mini.textures.Image;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL41;

import java.util.EnumSet;

public class GLImageFormats {
    private GLImageFormats() { }

    private static void format(GLImageFormat[][] formatToGL, Image.Format format,
                               int glInternalFormat,
                               int glFormat,
                               int glDataType){
        formatToGL[0][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType);
    }

    private static void formatSwiz(GLImageFormat[][] formatToGL, Image.Format format,
                                   int glInternalFormat,
                                   int glFormat,
                                   int glDataType){
        formatToGL[0][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType, false, true);
    }

    private static void formatSrgb(GLImageFormat[][] formatToGL, Image.Format format,
                                   int glInternalFormat,
                                   int glFormat,
                                   int glDataType)
    {
        formatToGL[1][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType);
    }

    private static void formatSrgbSwiz(GLImageFormat[][] formatToGL, Image.Format format,
                                       int glInternalFormat,
                                       int glFormat,
                                       int glDataType)
    {
        formatToGL[1][format.ordinal()] = new GLImageFormat(glInternalFormat, glFormat, glDataType, false, true);
    }

    private static void formatComp(GLImageFormat[][] formatToGL, Image.Format format,
                                   int glCompressedFormat,
                                   int glFormat,
                                   int glDataType){
        formatToGL[0][format.ordinal()] = new GLImageFormat(glCompressedFormat, glFormat, glDataType, true);
    }

    private static void formatCompSrgb(GLImageFormat[][] formatToGL, Image.Format format,
                                       int glCompressedFormat,
                                       int glFormat,
                                       int glDataType)
    {
        formatToGL[1][format.ordinal()] = new GLImageFormat(glCompressedFormat, glFormat, glDataType, true);
    }

    /**
     * Generates a list of supported texture formats.
     *
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
            format(formatToGL, Image.Format.RGB16F,  GL30.GL_RGB16F,            GL11.GL_RGB,           GL30.GL_HALF_FLOAT);
            format(formatToGL, Image.Format.RGB32F,  GL30.GL_RGB32F,            GL11.GL_RGB,           GL11.GL_FLOAT);
            format(formatToGL, Image.Format.RGBA16F, GL30.GL_RGBA16F,           GL11.GL_RGBA,          GL30.GL_HALF_FLOAT);
            format(formatToGL, Image.Format.RGBA32F, GL30.GL_RGBA32F,           GL11.GL_RGBA,          GL11.GL_FLOAT);
        }

        // Need to check if Caps.DepthTexture is supported prior to using for textures
        // But for renderbuffers its OK.
        format(formatToGL, Image.Format.Depth,   GL11.GL_DEPTH_COMPONENT,    GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE);
        format(formatToGL, Image.Format.Depth16, GL14.GL_DEPTH_COMPONENT16,  GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT);

        if (caps.contains(Caps.OpenGL20)) {
            format(formatToGL, Image.Format.Depth24, GL14.GL_DEPTH_COMPONENT24,  GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT);
        }

        return formatToGL;
    }
}
