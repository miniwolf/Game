package mini.textures.image;

import mini.renderer.opengl.GLRenderer;
import mini.textures.Texture;

/**
 * Stores / caches texture state parameters so they don't have to be set
 * each time by the {@link GLRenderer}.
 */
public final class LastTextureState {
    public Texture.WrapMode sWrap, tWrap, rWrap;
    public Texture.MagFilter magFilter;
    public Texture.MinFilter minFilter;
    public int anisoFilter;
    public Texture.ShadowCompareMode shadowCompareMode;

    public LastTextureState() {
        reset();
    }

    public void reset() {
        sWrap = null;
        tWrap = null;
        rWrap = null;
        magFilter = null;
        minFilter = null;
        anisoFilter = 1;

        // The default in OpenGL is OFF, so we avoid setting this per texture
        // if its not used.
        shadowCompareMode = Texture.ShadowCompareMode.Off;
    }
}
