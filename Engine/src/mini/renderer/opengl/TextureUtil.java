package mini.renderer.opengl;

import mini.renderer.Caps;
import mini.renderer.RendererException;
import mini.textures.Image;
import mini.textures.image.ColorSpace;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import static mini.textures.Image.Format;

/**
 * Internal utility class used by {@link GLRenderer} to manage textures.
 *
 * @author miniwolf
 */
final class TextureUtil {

    private final GL gl;
    private final GL2 gl2;
    private final GLExt glext;
    private GLImageFormat[][] formats;

    public TextureUtil(GL gl, GL2 gl2, GLExt glext) {
        this.gl = gl;
        this.gl2 = gl2;
        this.glext = glext;
    }

    public void initialize(EnumSet<Caps> caps) {
        this.formats = GLImageFormats.getFormatsForCaps(caps);
    }

    public GLImageFormat getImageFormat(Format fmt, boolean isSrgb) {
        return formats[isSrgb ? 1 : 0][fmt.ordinal()];
    }

    public GLImageFormat getImageFormatWithError(Format fmt, boolean isSrgb) {
        //if the passed format is one kind of depth there isno point in getting the srgb format;
        isSrgb = isSrgb && fmt != Format.Depth && fmt != Format.Depth16 && fmt != Format.Depth24
                 && fmt != Format.Depth24Stencil8 && fmt != Format.Depth32
                 && fmt != Format.Depth32F;
        GLImageFormat glFmt = getImageFormat(fmt, isSrgb);
        if (glFmt == null && isSrgb) {
            glFmt = getImageFormat(fmt, false);
            System.err.println("Warning: No sRGB format available for ''" + fmt
                               + "''. Failling back to linear.");
        }
        if (glFmt == null) {
            throw new RendererException(
                    "Image format '" + fmt + "' is unsupported by the video hardware.");
        }
        return glFmt;
    }

    private void setupTextureSwizzle(int target, Format format) {
        // Needed for OpenGL 3.3 to support luminance / alpha formats
        switch (format) {
            case Alpha8:
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_R, GL.GL_ZERO);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_G, GL.GL_ZERO);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_B, GL.GL_ZERO);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_A, GL.GL_RED);
                break;
            case Luminance8:
            case Luminance16F:
            case Luminance32F:
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_R, GL.GL_RED);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_G, GL.GL_RED);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_B, GL.GL_RED);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_A, GL.GL_ONE);
                break;
            case Luminance8Alpha8:
            case Luminance16FAlpha16F:
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_R, GL.GL_RED);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_G, GL.GL_RED);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_B, GL.GL_RED);
                gl.glTexParameteri(target, GL3.GL_TEXTURE_SWIZZLE_A, GL.GL_GREEN);
                break;
        }
    }

    private void uploadTextureLevel(GLImageFormat format, int target, int level, int slice,
                                    int sliceCount, int width, int height, int depth, int samples,
                                    ByteBuffer data) {
        if (format.compressed && data != null) {
            if (target == GL2.GL_TEXTURE_3D) {
                // For 3D textures, we upload the entire mipmap level.
                gl2.glCompressedTexImage3D(target,
                                           level,
                                           format.internalFormat,
                                           width,
                                           height,
                                           depth,
                                           0,
                                           data);
            } else if (target == GLExt.GL_TEXTURE_2D_ARRAY_EXT) {
                // For texture arrays, only upload 1 slice at a time.
                // zoffset specifies slice index, and depth is 1 to indicate
                // a single texture in the array.
                gl2.glCompressedTexSubImage3D(target,
                                              level,
                                              0,
                                              0,
                                              slice,
                                              width,
                                              height,
                                              1,
                                              format.internalFormat,
                                              data);
            } else {
                // Cubemaps also use 2D upload.
                gl2.glCompressedTexImage2D(target,
                                           level,
                                           format.internalFormat,
                                           width,
                                           height,
                                           0,
                                           data);
            }
        } else {
            // (Non-compressed OR allocating texture storage for FBO)
            if (target == GL2.GL_TEXTURE_3D) {
                gl2.glTexImage3D(target,
                                 level,
                                 format.internalFormat,
                                 width,
                                 height,
                                 depth,
                                 0,
                                 format.format,
                                 format.dataType,
                                 data);
            } else if (target == GLExt.GL_TEXTURE_2D_ARRAY_EXT) {
                if (slice == -1) {
                    // Allocate texture storage (data is NULL)
                    gl2.glTexImage3D(target,
                                     level,
                                     format.internalFormat,
                                     width,
                                     height,
                                     sliceCount, //# of slices
                                     0,
                                     format.format,
                                     format.dataType,
                                     data);
                } else {
                    // For texture arrays, only upload 1 slice at a time.
                    // zoffset specifies slice index, and depth is 1 to indicate
                    // a single texture in the array.
                    gl2.glTexSubImage3D(target,
                                        level,          // level
                                        0,              // xoffset
                                        0,              // yoffset
                                        slice,          // zoffset
                                        width,          // width
                                        height,         // height
                                        1,              // depth
                                        format.format,
                                        format.dataType,
                                        data);
                }
            } else {
                // 2D multisampled image.
                if (samples > 1) {
                    glext.glTexImage2DMultisample(target,
                                                  samples,
                                                  format.internalFormat,
                                                  width,
                                                  height,
                                                  true);
                } else {
                    // Regular 2D image
                    gl.glTexImage2D(target,
                                    level,
                                    format.internalFormat,
                                    width,
                                    height,
                                    0,
                                    format.format,
                                    format.dataType,
                                    data);
                }
            }
        }
    }

    public void uploadTexture(Image image, int target, int index, boolean linearizeSrgb) {

        boolean getSrgbFormat = image.getColorSpace() == ColorSpace.sRGB && linearizeSrgb;
        Image.Format jmeFormat = image.getFormat();
        GLImageFormat oglFormat = getImageFormatWithError(jmeFormat, getSrgbFormat);

        ByteBuffer data = null;
        int sliceCount = 1;

        if (index >= 0) {
            data = image.getData(index);
        }

        if (image.getData() != null && image.getData().size() > 0) {
            sliceCount = image.getData().size();
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int depth = image.getDepth();

        int[] mipSizes = image.getMipMapSizes();
        int pos = 0;
        // TODO: Remove unneccessary allocation
        if (mipSizes == null) {
            if (data != null) {
                mipSizes = new int[]{data.capacity()};
            } else {
                mipSizes = new int[]{width * height * jmeFormat.getBitsPerPixel() / 8};
            }
        }

        int samples = image.getMultiSamples();

        // For OGL3 core: setup texture swizzle.
        if (oglFormat.swizzleRequired) {
            setupTextureSwizzle(target, jmeFormat);
        }

        for (int i = 0; i < mipSizes.length; i++) {
            int mipWidth = Math.max(1, width >> i);
            int mipHeight = Math.max(1, height >> i);
            int mipDepth = Math.max(1, depth >> i);

            if (data != null) {
                data.position(pos);
                data.limit(pos + mipSizes[i]);
            }

            uploadTextureLevel(oglFormat, target, i, index, sliceCount, mipWidth, mipHeight,
                               mipDepth, samples, data);

            pos += mipSizes[i];
        }
    }

    public void uploadSubTexture(Image image, int target, int index, int x, int y,
                                 boolean linearizeSrgb) {
        if (target != GL.GL_TEXTURE_2D || image.getDepth() > 1) {
            throw new UnsupportedOperationException("Updating non-2D texture is not supported");
        }

        if (image.getMipMapSizes() != null) {
            throw new UnsupportedOperationException("Updating mip-mappped images is not supported");
        }

        if (image.getMultiSamples() > 1) {
            throw new UnsupportedOperationException(
                    "Updating multisampled images is not supported");
        }

        Image.Format imageFormat = image.getFormat();

        if (imageFormat.isCompressed()) {
            throw new UnsupportedOperationException("Updating compressed images is not supported");
        } else if (imageFormat.isDepthFormat()) {
            throw new UnsupportedOperationException("Updating depth images is not supported");
        }

        boolean getSrgbFormat = image.getColorSpace() == ColorSpace.sRGB && linearizeSrgb;
        GLImageFormat oglFormat = getImageFormatWithError(imageFormat, getSrgbFormat);

        ByteBuffer data = null;

        if (index >= 0) {
            data = image.getData(index);
        }

        if (data == null) {
            throw new IndexOutOfBoundsException(
                    "The image index " + index + " is not valid for the given image");
        }

        data.position(0);
        data.limit(data.capacity());

        gl.glTexSubImage2D(target, 0, x, y, image.getWidth(), image.getHeight(), oglFormat.format,
                           oglFormat.dataType, data);
    }
}
