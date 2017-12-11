package mini.renderer.opengl;

import mini.renderer.Caps;
import mini.textures.Image;
import mini.textures.image.ColorSpace;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Created by miniwolf on 25-04-2017.
 */
public class TextureUtil {
    private GLImageFormat[][] formats;

    public void uploadTexture(Image image,
                              int target,
                              int index,
                              boolean linearizeSrgb) {

        boolean getSrgbFormat = image.getColorSpace() == ColorSpace.sRGB && linearizeSrgb;
        Image.Format imageFormat = image.getFormat();
        GLImageFormat oglFormat = getImageFormatWithError(imageFormat, getSrgbFormat);

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
                mipSizes = new int[]{width * height * imageFormat.getBitsPerPixel() / 8};
            }
        }

        int samples = image.getMultiSamples();

        // For OGL3 core: setup texture swizzle.
        if (oglFormat.swizzleRequired) {
            setupTextureSwizzle(target, imageFormat);
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

    private void setupTextureSwizzle(int target, Image.Format format) {
        // Needed for OpenGL 3.3 to support luminance / alpha formats
        switch (format) {
            case Alpha8:
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_R, GL11.GL_ZERO);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_G, GL11.GL_ZERO);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_B, GL11.GL_ZERO);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_A, GL11.GL_RED);
                break;
            case Luminance8:
            case Luminance16F:
            case Luminance32F:
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_R, GL11.GL_RED);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_G, GL11.GL_RED);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_B, GL11.GL_RED);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_A, GL11.GL_ONE);
                break;
            case Luminance8Alpha8:
            case Luminance16FAlpha16F:
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_R, GL11.GL_RED);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_G, GL11.GL_RED);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_B, GL11.GL_RED);
                GL11.glTexParameteri(target, GL33.GL_TEXTURE_SWIZZLE_A, GL11.GL_GREEN);
                break;
        }
    }

    private void uploadTextureLevel(GLImageFormat format, int target, int level, int slice,
                                    int sliceCount, int width, int height, int depth, int samples,
                                    ByteBuffer data) {
        if (format.compressed && data != null) {
            if (target == GL12.GL_TEXTURE_3D) {
                // For 3D textures, we upload the entire mipmap level.
                GL13.glCompressedTexImage3D(target,
                                            level,
                                            format.internalFormat,
                                            width,
                                            height,
                                            depth,
                                            0,
                                            data);
            } else if (target == GL30.GL_TEXTURE_2D_ARRAY) {
                // For texture arrays, only upload 1 slice at a time.
                // zoffset specifies slice index, and depth is 1 to indicate
                // a single texture in the array.
                GL13.glCompressedTexSubImage3D(target,
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
                GL13.glCompressedTexImage2D(target,
                                            level,
                                            format.internalFormat,
                                            width,
                                            height,
                                            0,
                                            data);
            }
        } else {
            // (Non-compressed OR allocating texture storage for FBO)
            if (target == GL12.GL_TEXTURE_3D) {
                GL12.glTexImage3D(target,
                                  level,
                                  format.internalFormat,
                                  width,
                                  height,
                                  depth,
                                  0,
                                  format.format,
                                  format.dataType,
                                  data);
            } else if (target == GL30.GL_TEXTURE_2D_ARRAY) {
                if (slice == -1) {
                    // Allocate texture storage (data is NULL)
                    GL12.glTexImage3D(target,
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
                    GL12.glTexSubImage3D(target,
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
                    GL32.glTexImage2DMultisample(target,
                                                 samples,
                                                 format.internalFormat,
                                                 width,
                                                 height,
                                                 true);
                } else {
                    // Regular 2D image
                    GL11.glTexImage2D(target,
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

    public void initialize(Set<Caps> caps) {
        this.formats = GLImageFormats.getFormatsForCaps(caps);
        StringBuilder sb = new StringBuilder();
        sb.append("Supported texture formats: \n");
        for (int i = 0; i < Image.Format.values().length; i++) {
            Image.Format format = Image.Format.values()[i];
            if (formats[0][i] != null) {
                boolean srgb = formats[1][i] != null;
                sb.append("\t").append(format.toString());
                sb.append(" (Linear");
                if (srgb) {
                    sb.append("/sRGB");
                }
                sb.append(")\n");
            }
        }
        System.out.println(sb.toString());
    }

    public GLImageFormat getImageFormat(Image.Format fmt, boolean isSrgb) {
        return formats[isSrgb ? 1 : 0][fmt.ordinal()];
    }

    public GLImageFormat getImageFormatWithError(Image.Format fmt, boolean isSrgb) {
        //if the passed format is one kind of depth there isno point in getting the srgb format;
        isSrgb = isSrgb && fmt != Image.Format.Depth && fmt != Image.Format.Depth16
                 && fmt != Image.Format.Depth24 && fmt != Image.Format.Depth32;
        GLImageFormat glFmt = getImageFormat(fmt, isSrgb);
        if (glFmt == null && isSrgb) {
            glFmt = getImageFormat(fmt, false);
            System.err.println("Warning: No sRGB format available for ''" + fmt
                               + "''. Failling back to linear.");
        }
        if (glFmt == null) {
            throw new RuntimeException(
                    "Image format '" + fmt + "' is unsupported by the video hardware.");
        }
        return glFmt;
    }

    public void uploadSubTexture(Image image, int target, int index, int x, int y,
                                 boolean linearizeSrgb) {
        if (target != GL11.GL_TEXTURE_2D || image.getDepth() > 1) {
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

        GL11.glTexSubImage2D(target, 0, x, y, image.getWidth(), image.getHeight(),
                             oglFormat.format, oglFormat.dataType, data);
    }

}
