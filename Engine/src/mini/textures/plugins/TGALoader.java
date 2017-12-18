package mini.textures.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetLoader;
import mini.asset.TextureKey;
import mini.textures.Image;

import java.io.IOException;
import java.io.InputStream;

public class TGALoader implements AssetLoader<Image> {

    @Override
    public Image load(AssetInfo assetInfo) throws IOException {
        if (!(assetInfo.getKey() instanceof TextureKey)) {
            throw new IllegalArgumentException("Texture assets must be loaded using a TextureKey");
        }

        boolean flip = ((TextureKey) assetInfo.getKey()).isFlipY();
        try (InputStream stream = assetInfo.openStream()) {
            return load(stream, flip);
        }
    }

    private Image load(InputStream stream, boolean flip) {
        return null;
    }
}
