package mini.utils.sky;

/**
 * The type of map fed to the shader
 */
public enum EnvMapType {
    /**
     * The environment map is a cube map see {@link mini.textures.TextureCubeMap} or 6 separate
     * images that form a cube map. The texture is either a {@link mini.textures.TextureCubeMap} or
     * 6 {@link mini.textures.Texture2D}. In the latter case, a TextureCubeMap is build from the 6
     * 2D maps.
     */
    CubeMap,
    /**
     *
     */
    SphereMap,
    /**
     * The environment map is an Equirectangular map. A 2D texture with pixels arranged for
     * <a href=https://en.wikipedia.org/wiki/Equirectangular_projection>Equirectangular projection
     * mapping</a>.
     */
    EquirectMap
}
