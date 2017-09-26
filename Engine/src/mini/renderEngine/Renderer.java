package mini.renderEngine;

import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.shaders.ShaderProgram;
import mini.shaders.ShaderSource;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture;
import mini.utils.NativeObject;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

/**
 * The <code>Renderer</code> is responsible for taking rendering commands and
 * executing them on the underlying video hardware.
 */
public interface Renderer {

    /**
     * Detects available capabilities of the GPU.
     *
     * Must be called prior to any other Renderer methods.
     */
    void initialize();

    /**
     * Get the capabilities of the renderer.
     * @return The capabilities of the renderer.
     */
    Set<Caps> getCaps();

    /**
     * Get the limits of the renderer.
     *
     * @return The limits of the renderer.
     */
    Map<Limits, Integer> getLimits();

    /**
     * Invalidates the current rendering state. Should be called after
     * the GL state was changed manually or through an external library.
     */
    void invalidateState();

    /**
     * Clears certain channels of the currently bound framebuffer.
     *
     * @param color True if to clear colors (RGBA)
     * @param depth True if to clear depth/z
     * @param stencil True if to clear stencil buffer (if available, otherwise
     * ignored)
     */
    void clearBuffers(boolean color, boolean depth, boolean stencil);

    /**
     * Sets the background (aka clear) color.
     *
     * @param color The background color to set
     */
    void setBackgroundColor(ColorRGBA color);

    /**
     * Applies the given {@link RenderState}, making the necessary
     * GL calls so that the state is applied.
     */
    void applyRenderState(RenderState state);

    /**
     * Set the range of the depth values for objects. All rendered
     * objects will have their depth clamped to this range.
     *
     * @param start The range start
     * @param end The range end
     */
    void setDepthRange(float start, float end);

    /**
     * Called when a new frame has been rendered.
     *
     * Currently, this will simply delete any OpenGL objects from the GPU
     * which have been garbage collected by the GC.
     */
    void postFrame();

    /**
     * Set the viewport location and resolution on the screen.
     *
     * @param x The x coordinate of the viewport
     * @param y The y coordinate of the viewport
     * @param width Width of the viewport
     * @param height Height of the viewport
     */
    void setViewPort(int x, int y, int width, int height);

    /**
     * Specifies a clipping rectangle.
     * For all future rendering commands, no pixels will be allowed
     * to be rendered outside of the clip rectangle.
     *
     * @param x The x coordinate of the clip rect
     * @param y The y coordinate of the clip rect
     * @param width Width of the clip rect
     * @param height Height of the clip rect
     */
    void setClipRect(int x, int y, int width, int height);

    /**
     * Clears the clipping rectangle set with
     * {@link #setClipRect(int, int, int, int) }.
     */
    void clearClipRect();

    /**
     * Sets the shader to use for rendering.
     * If the shader has not been uploaded yet, it is compiled
     * and linked. If it has been uploaded, then the
     * uniform data is updated and the shader is set.
     *
     * @param shader The shader to use for rendering.
     */
    void setShader(ShaderProgram shader);

    /**
     * Deletes a shader. This method also deletes
     * the attached shader sources.
     *
     * @param shader Shader to delete.
     * @see #deleteShaderSource(ShaderSource)
     */
    void deleteShader(ShaderProgram shader);

    /**
     * Deletes the provided shader source.
     *
     * @param source The ShaderSource to delete.
     */
    void deleteShaderSource(ShaderSource source);

    /**
     * Copies contents from src to dst, scaling if necessary.
     * set copyDepth to false to only copy the color buffers.
     */
    void copyFrameBuffer(FrameBuffer src, FrameBuffer dst, boolean copyDepth);

    /**
     * Sets the framebuffer that will be drawn to.
     *
     * If the framebuffer has not been initialized yet, it will be created
     * and its render surfaces and attached textures will be allocated.
     *
     * @param fb The framebuffer to set
     */
    void setFrameBuffer(FrameBuffer fb);

    /**
     * Set the framebuffer that will be set instead of the main framebuffer
     * when a call to setFrameBuffer(null) is made.
     *
     * @param fb The framebuffer to override the main framebuffer.
     */
    void setMainFrameBufferOverride(FrameBuffer fb);

    /**
     * Reads the pixels currently stored in the specified framebuffer
     * into the given ByteBuffer object.
     * Only color pixels are transferred, the format is RGBA with 8 bits
     * per component. The given byte buffer should have at least
     * fb.getWidth() * fb.getHeight() * 4 bytes remaining.
     *
     * @param fb The framebuffer to read from
     * @param byteBuf The bytebuffer to transfer color data to
     */
    void readFrameBuffer(FrameBuffer fb, ByteBuffer byteBuf);

    /**
     * Reads the pixels currently stored in the specified framebuffer
     * into the given ByteBuffer object.
     * Only color pixels are transferred, witht hte given format.
     * The given byte buffer should have at least
     * fb.getWidth() * fb.getHeight() * 4 bytes remaining.
     *
     * @param fb The framebuffer to read from
     * @param byteBuf The bytebuffer to transfer color data to
     * @param format the image format to use when reading the frameBuffer.
     */
    void readFrameBufferWithFormat(FrameBuffer fb, ByteBuffer byteBuf, Image.Format format);

    /**
     * Deletes a framebuffer and all attached renderbuffers
     */
    void deleteFrameBuffer(FrameBuffer fb);

    /**
     * Sets the texture to use for the given texture unit.
     */
    void setTexture(int unit, Texture tex);

    /**
     * Modify the given Texture with the given Image.
     * The image will be put at x and y into the texture.
     *
     * NOTE: this is only supported for uncompressed 2D images without mipmaps.
     *
     * @param tex the Texture that will be modified
     * @param pixels the source Image data to copy data from
     * @param x the x position to put the image into the texture
     * @param y the y position to put the image into the texture
     */
    void modifyTexture(Texture tex, Image pixels, int x, int y);

    /**
     * Deletes a texture from the GPU.
     */
    void deleteImage(Image image);

    /**
     * Uploads a vertex buffer to the GPU.
     *
     * @param vb The vertex buffer to upload
     */
    void updateBufferData(VertexBuffer vb);

    /**
     * Deletes a vertex buffer from the GPU.
     * @param vb The vertex buffer to delete
     */
    void deleteBuffer(VertexBuffer vb);

    /**
     * Renders <code>count</code> meshes, with the geometry data supplied and
     * per-instance data supplied.
     * The shader which is currently set with <code>setShader</code> is
     * responsible for transforming the input vertices into clip space
     * and shading it based on the given vertex attributes.
     * The integer variable gl_InstanceID can be used to access the current
     * instance of the mesh being rendered inside the vertex shader.
     * If the instance data is non-null, then it is submitted as a
     * per-instance vertex attribute to the shader.
     *
     * @param mesh The mesh to render
     * @param lod The LOD level to use, see {@link Mesh#setLodLevels(mini.scene.VertexBuffer[]) }.
     * @param count Number of mesh instances to render
     * @param instanceData When count is greater than 1, these buffers provide
     *                     the per-instance attributes.
     */
    void renderMesh(Mesh mesh, int lod, int count, VertexBuffer[] instanceData);

    /**
     * Resets all previously used {@link NativeObject Native Objects} on this Renderer.
     * The state of the native objects is reset in such way, that using
     * them again will cause the renderer to reupload them.
     * Call this method when you know the GL context is going to shutdown.
     *
     * @see NativeObject#resetObject()
     */
    void resetGLObjects();

    /**
     * Deletes all previously used {@link NativeObject Native Objects} on this Renderer, and
     * then resets the native objects.
     *
     * @see #resetGLObjects()
     * @see NativeObject#deleteObject(java.lang.Object)
     */
    void cleanup();

    /**
     * Set the default anisotropic filter level for textures.
     *
     * If the
     * {@link Texture#setAnisotropicFilter(int) texture anisotropic filter} is
     * set to 0, then the default level is used. Otherwise if the texture level
     * is 1 or greater, then the texture's value overrides the default value.
     *
     * @param level The default anisotropic filter level to use. Default: 1.
     *
     * @throws IllegalArgumentException If level is less than 1.
     */
    void setDefaultAnisotropicFilter(int level);

    /**
     * Sets the alpha to coverage state.
     * <p>
     * When alpha coverage and multi-sampling is enabled,
     * each pixel will contain alpha coverage in all
     * of its subsamples, which is then combined when
     * other future alpha-blended objects are rendered.
     * </p>
     * <p>
     * Alpha-to-coverage is useful for rendering transparent objects
     * without having to worry about sorting them.
     * </p>
     */
    void setAlphaToCoverage(boolean value);

    /**
     * If enabled, color values rendered to the main framebuffer undergo
     * linear -&gt; sRGB conversion.
     *
     * This is identical to {@link FrameBuffer#setSrgb(boolean)} except it is toggled
     * for the main framebuffer instead of an offscreen buffer.
     *
     * This should be set together with {@link Renderer#setLinearizeSrgbImages(boolean)}
     *
     * As a shorthand, the user can set {@link AppSettings#setGammaCorrection(boolean)} to true
     * to toggle both {@link Renderer#setLinearizeSrgbImages(boolean)} and
     * {@link Renderer#setMainFrameBufferSrgb(boolean)} if the
     * {@link Caps#Srgb} is supported by the GPU.
     *
     * @throws RendererException If the GPU hardware does not support sRGB.
     *
     * @see FrameBuffer#setSrgb(boolean)
     * @see Caps#Srgb
     */
    void setMainFrameBufferSrgb(boolean srgb);

    /**
     * If enabled, all {@link Image images} with the {@link Image#setColorSpace(mini.textures.image.ColorSpace) sRGB flag}
     * set shall undergo an sRGB to linear RGB color conversion when read by a shader.
     *
     * The conversion is performed for the following formats:
     *  - {@link Image.Format#RGB8}
     *  - {@link Image.Format#RGBA8}
     *  - {@link Image.Format#Luminance8}
     *  - {@link Image.Format#Luminance8Alpha8}
     *
     * For all other formats, no conversion is performed.
     *
     * If this option is toggled at runtime, textures must be reloaded for the change to take effect.
     *
     * @throws RendererException If the GPU hardware does not support sRGB.
     *
     * @param linearize If sRGB images undergo sRGB -&gt; linear conversion prior to rendering.
     *
     * @see Caps#Srgb
     */
    void setLinearizeSrgbImages(boolean linearize);

}

