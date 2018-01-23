package mini.textures;

import mini.app.SimpleApplication;
import mini.scene.Spatial;
import mini.utils.sky.SkyFactory;

public class TestSkyLoading extends SimpleApplication {
    public static void main(String[] args) {
        TestSkyLoading app = new TestSkyLoading();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
        Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
        Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
        Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
        Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
        Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");

        Spatial sky = new SkyFactory(assetManager).createSky(west, east, north, south, up, down);
        rootNode.attachChild(sky);
    }
}
