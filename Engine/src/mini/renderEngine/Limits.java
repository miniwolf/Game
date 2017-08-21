package mini.renderEngine;

/**
 * <code>Limits</code> allows querying the limits of certain features in
 * {@link mini.renderEngine.opengl.GLRenderer}.
 * <p>
 * For example, maximum texture sizes or number of samples.
 *
 * @author Kirill Vainer
 */
public enum Limits {
    /**
     * Maximum number of vertex texture units, or number of textures that can be
     * used in the vertex shader.
     */
    VertexTextureUnits,
    /**
     * Maximum number of fragment texture units, or number of textures that can
     * be used in the fragment shader.
     */
    FragmentTextureUnits,
    FragmentUniformVectors,
    VertexUniformVectors,
    VertexAttributes,
    FrameBufferSamples,
    FrameBufferAttachments,
    FrameBufferMrtAttachments,
    RenderBufferSize,
    TextureSize,
    CubemapSize,
    ColorTextureSamples,
    DepthTextureSamples,
    TextureAnisotropy,
}