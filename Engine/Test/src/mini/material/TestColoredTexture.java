package mini.material;

import mini.app.SimpleApplication;
import mini.asset.TextureKey;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.shape.Quad;

public class TestColoredTexture extends SimpleApplication {

    public static void main(String[] args) {
        TestColoredTexture app = new TestColoredTexture();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Quad quadMesh = new Quad(512, 512);
        Geometry quad = new Geometry("Quad", quadMesh);
        quad.setQueueBucket(RenderQueue.Bucket.Gui);

        Material mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        mat.setTexture("ColorMap",
                       assetManager.loadAsset(new TextureKey("Textures/Terrain/Pond/Pond.jpg")));
        quad.setMaterial(mat);
        guiNode.attachChildAt(quad, 0);
    }
}
