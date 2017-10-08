package mini.asset;

import mini.textures.Texture;

/**
 * Used to load textures from image files such as JPG or PNG.
 * Note that texture loaders actually load the asset as an {@link Image}
 * object, which is then converted to a {@link Texture} in the
 * {@link TextureProcessor#postProcess(mini.asset.AssetKey, java.lang.Object) }
 * method. Since textures are cloneable smart assets, the texture stored
 * in the cache will be collected when all clones of the texture become
 * unreachable.
 */
public class TextureKey extends AssetKey<Texture> {
    private boolean generateMips;
    private boolean flipY;
    private int anisotropy;
    private Texture.Type textureTypeHint = Texture.Type.TwoDimensional;

    public TextureKey(String path, boolean flipY) {
        super(path);
        this.flipY = flipY;
    }

    public TextureKey(String name) {
        super(name);
        this.flipY = true;
    }

    public TextureKey() {
    }

    @Override
    public String toString() {
        String type;
        switch (textureTypeHint) {
            case CubeMap:
                type = " (Cube)";
                break;
            case ThreeDimensional:
                type = " (3D)";
                break;
            case TwoDimensionalArray:
                type = " (Array)";
                break;
            case TwoDimensional:
                type = "";
                break;
            default:
                type = " (" + textureTypeHint.toString() + ")";
                break;
        }
        return name + (flipY ? " (Flipped)" : "") + type + (generateMips ? " (Mipmapped)" : "");
    }

    public boolean isFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public int getAnisotropy() {
        return anisotropy;
    }

    public void setAnisotropy(int anisotropy) {
        this.anisotropy = anisotropy;
    }

    public boolean isGenerateMips() {
        return generateMips;
    }

    public void setGenerateMips(boolean generateMips) {
        this.generateMips = generateMips;
    }

    /**
     * The type of texture expected to be returned.
     *
     * @return type of texture expected to be returned.
     */
    public Texture.Type getTextureTypeHint() {
        return textureTypeHint;
    }

    /**
     * Hints the loader as to which type of texture is expected.
     *
     * @param textureTypeHint The type of texture expected to be loaded.
     */
    public void setTextureTypeHint(Texture.Type textureTypeHint) {
        this.textureTypeHint = textureTypeHint;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextureKey other = (TextureKey) obj;
        return super.equals(obj) && this.generateMips == other.generateMips
               && this.flipY == other.flipY && this.anisotropy == other.anisotropy
               && this.textureTypeHint == other.textureTypeHint;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (super.hashCode());
        hash = 17 * hash + (this.generateMips ? 1 : 0);
        hash = 17 * hash + (this.flipY ? 1 : 0);
        hash = 17 * hash + this.anisotropy;
        hash = 17 * hash + (this.textureTypeHint != null ? this.textureTypeHint.hashCode() : 0);
        return hash;
    }
}
