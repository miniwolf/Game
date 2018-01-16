package mini.renderer.niftygui;

import de.lessvoid.nifty.spi.render.RenderImage;
import mini.asset.TextureKey;
import mini.post.niftygui.NiftyMiniDisplay;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.Texture2D;

public class RenderImageMini implements RenderImage {
    private Texture2D texture;
    private int width, height;

    public RenderImageMini(String fileName, boolean linear, NiftyMiniDisplay display) {
        TextureKey textureKey = new TextureKey(fileName, true);

        texture = (Texture2D) display.getAssetManager().loadTexture(textureKey);
        texture.setMagFilter(linear ? Texture.MagFilter.Bilinear : Texture.MagFilter.Nearest);
        texture.setMinFilter(
                linear ? Texture.MinFilter.BilinearNoMipMaps : Texture.MinFilter.NearestNoMipMaps);
        Image image = texture.getImage();

        width = image.getWidth();
        height = image.getHeight();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void dispose() {
    }

    public Texture2D getTexture() {
        return texture;
    }
}
