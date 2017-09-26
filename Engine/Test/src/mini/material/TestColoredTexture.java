package mini.material;

import mini.app.SimpleApplication;
import mini.asset.TextureKey;
import mini.renderEngine.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.shape.Quad;
import mini.textures.Texture;
import mini.textures.plugins.AWTLoader;

public class TestColoredTexture extends SimpleApplication {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        TestColoredTexture app = new TestColoredTexture();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Quad quadMesh = new Quad(512, 512);
        Geometry quad = new Geometry("Quad", quadMesh);
        quad.setQueueBucket(RenderQueue.Bucket.Gui);

        Material mat = new Material("MatDefs/Misc/Unshaded.minid");
        mat.setTexture("ColorMap",
                       (Texture) AWTLoader.load(new TextureKey("Textures/Terrain/Pond/Pond.jpg")));
        quad.setMaterial(mat);
        guiNode.attachChildAt(quad, 0);
    }
}
