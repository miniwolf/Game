package mini.renderEngine;

import mini.shaders.ShaderProgram;
import mini.shaders.ShaderSource;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture;

import java.util.Collection;

/**
 * <code>Caps</code> is an enum specifying a capability that the {@link Renderer}
 * supports.
 *
 * @author Kirill Vainer
 */
public enum Caps {

    /**
     * Supports {@link FrameBuffer FrameBuffers}.
     * <p>
     * OpenGL: Renderer exposes the GL_EXT_framebuffer_object extension.<br>
     * OpenGL ES: Renderer supports OpenGL ES 2.0.
     *//**
     * Supports {@link FrameBuffer FrameBuffers}.
     * <p>
     * OpenGL: Renderer exposes the GL_EXT_framebuffer_object extension.<br>
     * OpenGL ES: Renderer supports OpenGL ES 2.0.
     *//**
     * Supports {@link FrameBuffer FrameBuffers}.
     * <p>
     * OpenGL: Renderer exposes the GL_EXT_framebuffer_object extension.<br>
     * OpenGL ES: Renderer supports OpenGL ES 2.0.
     */
    /**
     * Supports {@link FrameBuffer FrameBuffers}.
     * <p>
     * OpenGL: Renderer exposes the GL_EXT_framebuffer_object extension.<br>
     * OpenGL ES: Renderer supports OpenGL ES 2.0.
     */
    FrameBuffer,

    /**
     * Supports framebuffer Multiple Render Targets (MRT)
     * <p>
     * OpenGL: Renderer exposes the GL_ARB_draw_buffers extension
     */
    FrameBufferMRT,

    /**
     * Supports framebuffer multi-sampling
     * <p>
     * OpenGL: Renderer exposes the GL EXT framebuffer multisample extension<br>
     * OpenGL ES: Renderer exposes GL_APPLE_framebuffer_multisample or
     * GL_ANGLE_framebuffer_multisample.
     */
    FrameBufferMultisample,

    /**
     * Supports texture multi-sampling
     * <p>
     * OpenGL: Renderer exposes the GL_ARB_texture_multisample extension<br>
     * OpenGL ES: Renderer exposes the GL_IMG_multisampled_render_to_texture
     * extension.
     */
    TextureMultisample,

    /**
     * Supports OpenGL 2.0 or OpenGL ES 2.0.
     */
    OpenGL20,

    /**
     * Supports OpenGL 2.1
     */
    OpenGL21,

    /**
     * Supports OpenGL 3.0
     */
    OpenGL30,

    /**
     * Supports OpenGL 3.1
     */
    OpenGL31,

    /**
     * Supports OpenGL 3.2
     */
    OpenGL32,
    /**
     * Supports OpenGL 3.3
     */
    OpenGL33,
    /**
     * Supports OpenGL 4.0
     */
    OpenGL40,
    /**
     * Supports OpenGL 4.1
     */
    OpenGL41,
    /**
     * Supports OpenGL 4.2
     */
    OpenGL42,
    /**
     * Supports OpenGL 4.3
     */
    OpenGL43,
    /**
     * Supports OpenGL 4.4
     */
    OpenGL44,
    /**
     * Supports OpenGL 4.5
     */
    OpenGL45,

    /**
     * Supports GLSL 1.0
     */
    GLSL100,

    /**
     * Supports GLSL 1.1
     */
    GLSL110,

    /**
     * Supports GLSL 1.2
     */
    GLSL120,

    /**
     * Supports GLSL 1.3
     */
    GLSL130,

    /**
     * Supports GLSL 1.4
     */
    GLSL140,

    /**
     * Supports GLSL 1.5
     */
    GLSL150,

    /**
     * Supports GLSL 3.3
     */
    GLSL330,
    /**
     * Supports GLSL 4.0
     */
    GLSL400,
    /**
     * Supports GLSL 4.1
     */
    GLSL410,
    /**
     * Supports GLSL 4.2
     */
    GLSL420,
    /**
     * Supports GLSL 4.3
     */
    GLSL430,
    /**
     * Supports GLSL 4.4
     */
    GLSL440,
    /**
     * Supports GLSL 4.5
     */
    GLSL450,
    /**
     * Supports reading from textures inside the vertex shader.
     */
    VertexTextureFetch,

    /**
     * Supports geometry shader.
     */
    GeometryShader,
    /**
     * Supports Tesselation shader
     */
    TesselationShader,
    /**
     * Supports texture arrays
     */
    TextureArray,

    /**
     * Supports texture buffers
     */
    TextureBuffer,

    /**
     * Supports floating point & half textures (Format.RGB16F)
     */
    FloatTexture,

    /**
     * Supports integer textures
     */
    IntegerTexture,

    /**
     * Supports floating point FBO color buffers (Format.RGB16F)
     */
    FloatColorBuffer,

    /**
     * Supports floating point depth buffer
     */
    FloatDepthBuffer,

    /**
     * Supports Format.RGB111110F for textures
     */
    PackedFloatTexture,

    /**
     * Supports Format.RGB9E5 for textures
     */
    SharedExponentTexture,

    /**
     * Supports Format.RGB111110F for FBO color buffers
     */
    PackedFloatColorBuffer,

    /**
     * Supports Format.RGB9E5 for FBO color buffers
     */
    SharedExponentColorBuffer,

    /**
     * Supports Non-Power-Of-Two (NPOT) textures and framebuffers
     */
    NonPowerOfTwoTextures,

    /**
     * Supports geometry instancing.
     */
    MeshInstancing,

    /**
     * Supports VAO, or vertex buffer arrays
     */
    VertexBufferArray,

    /**
     * Supports multisampling on the screen
     */
    Multisample,

    /**
     * Supports FBO with Depth24Stencil8 image format
     */
    PackedDepthStencilBuffer,

    /**
     * Supports sRGB framebuffers and sRGB texture format
     */
    Srgb,

    /**
     * Supports blitting framebuffers.
     */
    FrameBufferBlit,

    /**
     * Supports {@link Format#DXT1} and sister formats.
     */
    TextureCompressionS3TC,

    /**
     * Supports anisotropic texture filtering.
     */
    TextureFilterAnisotropic,

    /**
     * Supports {@link Format#ETC1} texture compression.
     */
    TextureCompressionETC1,

    /**
     * Supports {@link Format#ETC1} texture compression by uploading
     * the texture as ETC2 (they are backwards compatible).
     */
    TextureCompressionETC2,

    /**
     * Supports OpenGL ES 2
     */
    OpenGLES20,

    /**
     * Supports RGB8 / RGBA8 textures
     */
    Rgba8,

    /**
     * Supports depth textures.
     */
    DepthTexture,

    /**
     * Supports 32-bit index buffers.
     */
    IntegerIndexBuffer,

    /**
     * Partial support for non-power-of-2 textures, typically found
     * on OpenGL ES 2 devices.
     * <p>
     * Use of NPOT textures is allowed iff:
     * <ul>
     * <li>The {@link Texture.WrapMode} is set to
     * {@link Texture.WrapMode#EdgeClamp}.</li>
     * <li>Mip-mapping is not used, meaning {@link Texture.MinFilter} is set to
     * {@link Texture.MinFilter#BilinearNoMipMaps} or
     * {@link Texture.MinFilter#NearestNoMipMaps}</li>
     * </ul>
     */
    PartialNonPowerOfTwoTextures,

    /**
     * When sampling cubemap edges, interpolate between the adjecent faces
     * instead of just sampling one face.
     * <p>
     * Improves the quality of environment mapping.
     */
    SeamlessCubemap,

    /**
     * Running with OpenGL 3.2+ core profile.
     * <p>
     * Compatibility features will not be available.
     */
    CoreProfile,

    /**
     * GPU can provide and accept binary shaders.
     */
    BinaryShader;

    /**
     * Returns true if given the renderer capabilities, the texture
     * can be supported by the renderer.
     * <p>
     * This only checks the format of the texture, non-power-of-2
     * textures are scaled automatically inside the renderer
     * if are not supported natively.
     *
     * @param caps The collection of renderer capabilities {@link Renderer#getCaps() }.
     * @param tex  The texture to check
     * @return True if it is supported, false otherwise.
     */
    public static boolean supports(Collection<Caps> caps, Texture tex) {
        Image img = tex.getImage();
        return tex.getType() != Texture.Type.TwoDimensionalArray
               || caps.contains(Caps.TextureArray)
                  && (img == null || !img.getFormat().isFloatingPont()
                      || caps.contains(Caps.FloatTexture));

    }

    private static boolean supportsColorBuffer(Collection<Caps> caps,
                                               mini.textures.FrameBuffer.RenderBuffer colorBuf) {
        Image.Format colorFmt = colorBuf.getFormat();
        return !colorFmt.isDepthFormat() && !colorFmt.isCompressed()
               && (!colorFmt.isFloatingPont() || caps.contains(Caps.FloatColorBuffer));

    }

    /**
     * Returns true if given the renderer capabilities, the framebuffer
     * can be supported by the renderer.
     *
     * @param caps The collection of renderer capabilities {@link Renderer#getCaps() }.
     * @param fb   The framebuffer to check
     * @return True if it is supported, false otherwise.
     */
    public static boolean supports(Collection<Caps> caps, FrameBuffer fb) {
        if (!caps.contains(Caps.FrameBuffer)) {
            return false;
        }

        if (fb.getSamples() > 1
            && !caps.contains(Caps.FrameBufferMultisample)) {
            return false;
        }

        mini.textures.FrameBuffer.RenderBuffer depthBuf = fb.getDepthBuffer();
        if (depthBuf != null) {
            Image.Format depthFmt = depthBuf.getFormat();
            if (!depthFmt.isDepthFormat()) {
                return false;
            }
        }
        for (int i = 0; i < fb.getNumColorBuffers(); i++) {
            if (!supportsColorBuffer(caps, fb.getColorBuffer(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if given the renderer capabilities, the shader
     * can be supported by the renderer.
     *
     * @param caps   The collection of renderer capabilities {@link Renderer#getCaps() }.
     * @param shader The shader to check
     * @return True if it is supported, false otherwise.
     */
    public static boolean supports(Collection<Caps> caps, ShaderProgram shader) {
        for (ShaderSource source : shader.getSources()) {
            if (source.getLanguage().startsWith("GLSL")) {
                int ver = Integer.parseInt(source.getLanguage().substring(4));
                switch (ver) {
                    case 100:
                        if (!caps.contains(Caps.GLSL100)) {
                            return false;
                        }
                    case 110:
                        if (!caps.contains(Caps.GLSL110)) {
                            return false;
                        }
                    case 120:
                        if (!caps.contains(Caps.GLSL120)) {
                            return false;
                        }
                    case 130:
                        if (!caps.contains(Caps.GLSL130)) {
                            return false;
                        }
                    case 140:
                        if (!caps.contains(Caps.GLSL140)) {
                            return false;
                        }
                    case 150:
                        if (!caps.contains(Caps.GLSL150)) {
                            return false;
                        }
                    case 330:
                        if (!caps.contains(Caps.GLSL330)) {
                            return false;
                        }
                    case 400:
                        if (!caps.contains(Caps.GLSL400)) {
                            return false;
                        }
                    case 410:
                        if (!caps.contains(Caps.GLSL410)) {
                            return false;
                        }
                    case 420:
                        if (!caps.contains(Caps.GLSL420)) {
                            return false;
                        }
                    case 430:
                        if (!caps.contains(Caps.GLSL430)) {
                            return false;
                        }
                    case 440:
                        if (!caps.contains(Caps.GLSL440)) {
                            return false;
                        }
                    case 450:
                        if (!caps.contains(Caps.GLSL450)) {
                            return false;
                        }
                    default:
                        return false;
                }
            }
        }
        return true;
    }

}
