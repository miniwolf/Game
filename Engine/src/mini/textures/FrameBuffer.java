package mini.textures;

import mini.material.RenderState;
import mini.renderEngine.Caps;
import mini.renderEngine.opengl.GLRenderer;
import mini.utils.NativeObject;

import java.lang.annotation.Native;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <code>FrameBuffer</code>s are rendering surfaces allowing
 * off-screen rendering and render-to-texture functionality.
 * Instead of the scene rendering to the screen, it is rendered into the
 * FrameBuffer, the result can be either a texture or a buffer.
 * <p>
 * A <code>FrameBuffer</code> supports two methods of rendering,
 * using a {@link Texture} or using a buffer.
 * When using a texture, the result of the rendering will be rendered
 * onto the texture, after which the texture can be placed on an object
 * and rendered as if the texture was uploaded from disk.
 * When using a buffer, the result is rendered onto
 * a buffer located on the GPU, the data of this buffer is not accessible
 * to the user. buffers are useful if one
 * wishes to retrieve only the color content of the scene, but still desires
 * depth testing (which requires a depth buffer).
 * Buffers can be copied to other framebuffers
 * including the main screen, by using
 * {@link GLRenderer#copyFrameBuffer(FrameBuffer, FrameBuffer) }.
 * The content of a {@link RenderBuffer} can be retrieved by using
 * {@link GLRenderer#readFrameBuffer(FrameBuffer, java.nio.ByteBuffer) }.
 * <p>
 * <code>FrameBuffer</code>s have several attachment points, there are
 * several <em>color</em> attachment points and a single <em>depth</em>
 * attachment point.
 * The color attachment points support image formats such as
 * {@link Image.Format#RGBA8}, allowing rendering the color content of the scene.
 * The depth attachment point requires a depth image format.
 *
 * @author miniwolf
 * @see GLRenderer#setFrameBuffer(FrameBuffer)
 */
public class FrameBuffer extends NativeObject {
    public static final int SLOT_UNDEF = -1;
    public static final int SLOT_DEPTH = -100;
    public static final int SLOT_DEPTH_STENCIL = -101;

    boolean updateNeeded = false;
    private int id = -1;
    private int width = 0;
    private int height = 0;
    private int colorBufIndex = 0;
    private int samples = 1;
    private List<RenderBuffer> colorBufs = new ArrayList<>();
    private RenderBuffer depthBuf = null;
    private boolean srgb;

    /**
     * <code>RenderBuffer</code> represents either a texture or a
     * buffer that will be rendered to. <code>RenderBuffer</code>s
     * are attached to an attachment slot on a <code>FrameBuffer</code>.
     */
    public class RenderBuffer {
        Texture tex;
        Image.Format format;
        int id = -1;
        int slot = SLOT_UNDEF;
        int face = -1;
        int layer = -1;

        /**
         * @return The image format of the render buffer.
         */
        public Image.Format getFormat() {
            return format;
        }

        /**
         * @return The texture to render to for this <code>RenderBuffer</code>
         * or null if content should be rendered into a buffer.
         */
        public Texture getTexture() {
            return tex;
        }

        /**
         * Do not use.
         */
        public int getId() {
            return id;
        }

        /**
         * Do not use.
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * Do not use.
         */
        public int getSlot() {
            return slot;
        }

        public int getFace() {
            return face;
        }

        public void resetObject() {
            id = -1;
        }

        public RenderBuffer createDestructableClone() {
            if (tex != null) {
                return null;
            } else {
                RenderBuffer destructClone = new RenderBuffer();
                destructClone.id = id;
                return destructClone;
            }
        }

        @Override
        public String toString() {
            return (tex != null ? "TextureTarget" : "BufferTarget") + "[format=" + format + "]";
        }

        public int getLayer() {
            return this.layer;
        }
    }

    protected FrameBuffer(FrameBuffer src){
        super(src.id);
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    public void setUpdateNeeded() {
        updateNeeded = true;
    }

    public void clearUpdateNeeded() {
        updateNeeded = false;
    }

    /**
     * If enabled, any shaders rendering into this <code>FrameBuffer</code>
     * will be able to write several results into the renderbuffers
     * by using the <code>gl_FragData</code> array. Every slot in that
     * array maps into a color buffer attached to this framebuffer.
     *
     * @param enabled True to enable MRT (multiple rendering targets).
     */
    public void setMultiTarget(boolean enabled) {
        if (enabled) {
            colorBufIndex = -1;
        } else {
            colorBufIndex = 0;
        }
    }

    /**
     * @return True if MRT (multiple rendering targets) is enabled.
     * @see FrameBuffer#setMultiTarget(boolean)
     */
    public boolean isMultiTarget() {
        return colorBufIndex == -1;
    }

    /**
     * If MRT is not enabled ({@link FrameBuffer#setMultiTarget(boolean) } is false)
     * then this specifies the color target to which the scene should be rendered.
     * <p>
     * By default the value is 0.
     *
     * @param index The color attachment index.
     * @throws IllegalArgumentException If index is negative or doesn't map
     *                                  to any attachment on this framebuffer.
     */
    public void setTargetIndex(int index) {
        if (index < 0 || index >= 16) {
            throw new IllegalArgumentException("Target index must be between 0 and 16");
        }

        if (colorBufs.size() < index) {
            throw new IllegalArgumentException("The target at " + index + " is not set!");
        }

        colorBufIndex = index;
        setUpdateNeeded();
    }

    /**
     * @return The color target to which the scene should be rendered.
     * @see FrameBuffer#setTargetIndex(int)
     */
    public int getTargetIndex() {
        return colorBufIndex;
    }

    /**
     * @return The number of color buffers attached to this texture.
     */
    public int getNumColorBuffers() {
        return colorBufs.size();
    }

    /**
     * @param index
     * @return The color buffer at the given index.
     */
    public RenderBuffer getColorBuffer(int index) {
        return colorBufs.get(index);
    }

    /**
     * @return The color buffer with the index set by {@link #setTargetIndex(int), or null
     * if no color buffers are attached.
     * If MRT is disabled, the first color buffer is returned.
     */
    public RenderBuffer getColorBuffer() {
        if (colorBufs.isEmpty())
            return null;
        if (colorBufIndex < 0 || colorBufIndex >= colorBufs.size()) {
            return colorBufs.get(0);
        }
        return colorBufs.get(colorBufIndex);
    }

    /**
     * @return The depth buffer attached to this FrameBuffer, or null
     * if no depth buffer is attached
     */
    public RenderBuffer getDepthBuffer() {
        return depthBuf;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return The height in pixels of this framebuffer.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return The width in pixels of this framebuffer.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The number of samples when using a multisample framebuffer, or
     * 1 if this is a singlesampled framebuffer.
     */
    public int getSamples() {
        return samples;
    }

    /**
     * Specifies that the color values stored in this framebuffer are in SRGB
     * format.
     * <p>
     * The FrameBuffer must have a texture attached with the flag
     * {@link Image#isSrgb()} set to true.
     * <p>
     * The Renderer must expose the {@link Caps#Srgb sRGB pipeline} capability
     * for this option to take any effect.
     * <p>
     * Rendering operations performed on this framebuffer shall undergo a linear
     * -&gt; sRGB color space conversion when this flag is enabled. If
     * {@link RenderState#getBlendMode() blending} is enabled, it will be
     * performed in linear space by first decoding the stored sRGB pixel values
     * into linear, combining with the shader result, and then converted back to
     * sRGB upon being written into the framebuffer.
     *
     * @param srgb If the framebuffer color values should be stored in sRGB
     *             color space.
     */
    public void setSrgb(boolean srgb) {
        this.srgb = srgb;
    }

    /**
     * Determines if this framebuffer contains SRGB data.
     *
     * @return True if the framebuffer color values are in SRGB space, false if
     * in linear space.
     */
    public boolean isSrgb() {
        return srgb;
    }

    @Override
    public void resetObject() {
        this.id = -1;

        for (RenderBuffer colorBuf : colorBufs) {
            colorBuf.resetObject();
        }

        if (depthBuf != null)
            depthBuf.resetObject();

        setUpdateNeeded();
    }

    @Override
    public long getUniqueId() {
        return ((long)OBJTYPE_TEXTURE << 32) | ((long)id);
    }

    @Override
    public void deleteObject(Object rendererObject) {
        ((GLRenderer)rendererObject).deleteFrameBuffer(this);
    }

    @Override
    public NativeObject createDestructableClone() {
        return new FrameBuffer(this);
    }
}
