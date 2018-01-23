package mini.textures;

import mini.app.SimpleApplication;
import mini.asset.TextureKey;
import mini.scene.Spatial;
import mini.utils.sky.EnvMapType;
import mini.utils.sky.SkyFactory;

public class TestLoadingDDSCubeMap extends SimpleApplication {
    public static void main(String[] args) {
        TestLoadingDDSCubeMap app = new TestLoadingDDSCubeMap();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        TextureKey key = new TextureKey("Textures/Sky/Bright/BrightSky.dds", true);
        key.setGenerateMips(true);
        key.setTextureTypeHint(Texture.Type.CubeMap);
        Texture texture = assetManager.loadTexture(key);

        Spatial sky = new SkyFactory(assetManager).createSky(texture, EnvMapType.CubeMap);
        rootNode.attachChild(sky);
    }
}
