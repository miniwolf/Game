package mini.textures;

import mini.textures.plugins.AWTLoader;
import mini.utils.MyFile;

public class TextureBuilder {
    private boolean clampEdges = false;
    private boolean mipmap = false;
    private boolean anisotropic = true;
    private boolean nearest = false;

    private MyFile file;

    public TextureBuilder(MyFile textureFile) {
        this.file = textureFile;
    }

    public Texture create() {
        Image load = (Image) AWTLoader.load(file);
        return new Texture2D(load);
    }

    public TextureBuilder clampEdges() {
        this.clampEdges = true;
        return this;
    }

    public TextureBuilder normalMipMap() {
        this.mipmap = true;
        this.anisotropic = false;
        return this;
    }

    public TextureBuilder nearestFiltering() {
        this.mipmap = false;
        this.anisotropic = false;
        this.nearest = true;
        return this;
    }

    public TextureBuilder anisotropic() {
        this.mipmap = true;
        this.anisotropic = true;
        return this;
    }

    protected boolean isClampEdges() {
        return clampEdges;
    }

    protected boolean isMipmap() {
        return mipmap;
    }

    protected boolean isAnisotropic() {
        return anisotropic;
    }

    protected boolean isNearest() {
        return nearest;
    }
}
