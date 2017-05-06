package mini.renderEngine.opengl;

import mini.textures.Image;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

/**
 * Created by miniwolf on 25-04-2017.
 */
public class TextureUtil {
    private GLImageFormat[][] formats;

    public void uploadTexture(Image image, int target, int index) {

        Image.Format imageFormat = image.getFormat();

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

        for (int i = 0; i < mipSizes.length; i++) {
            int mipWidth = Math.max(1, width >> i);
            int mipHeight = Math.max(1, height >> i);
            int mipDepth = Math.max(1, depth >> i);

            if (data != null) {
                data.position(pos);
                data.limit(pos + mipSizes[i]);
            }

            uploadTextureLevel(target, i, index, sliceCount, mipWidth, mipHeight, mipDepth, samples,
                               data);

            pos += mipSizes[i];
        }
    }

    private void uploadTextureLevel(int target, int level, int slice, int sliceCount, int width,
                                    int height, int depth, int samples, ByteBuffer data) {
        // (Non-compressed OR allocating texture storage for FBO)
        if (target == GL12.GL_TEXTURE_3D) {
            GL12.glTexImage3D(target,
                              level,
                              GL11.GL_RGBA,
                              width,
                              height,
                              depth,
                              0,
                              GL11.GL_RGBA,
                              GL11.GL_FLOAT,
                              data);
        } else {
            // 2D multisampled image.
            if (samples > 1) {
                GL32.glTexImage2DMultisample(target,
                                             samples,
                                             GL11.GL_RGBA,
                                             width,
                                             height,
                                             true);
            } else {
                // Regular 2D image
                GL11.glTexImage2D(target,
                                  level,
                                  GL11.GL_RGBA8,
                                  width,
                                  height,
                                  0,
                                  GL11.GL_RGBA8,
                                  GL11.GL_FLOAT,
                                  data);
            }
        }
    }

}
