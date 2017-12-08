package mini.model;

import mini.app.SimpleApplication;
import mini.asset.MaterialKey;
import mini.asset.ModelKey;
import mini.light.DirectionalLight;
import mini.light.PointLight;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Spatial;
import mini.scene.shape.Sphere;

public class TestTeapot extends SimpleApplication {
    private PointLight pl;
    private Spatial lightMdl;

    public static void main(String[] args) {
        TestTeapot app = new TestTeapot();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        Spatial bumpy = assetManager.loadAsset(new ModelKey("Models/Teapot/Teapot.obj"));
        rootNode.attachChild(bumpy);

        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightMdl.setMaterial(assetManager.loadAsset(new MaterialKey("Materials/RedColor.mini")));
        rootNode.attachChild(lightMdl);

        // flourescent main light
        pl = new PointLight();
        pl.setColor(new ColorRGBA(0.88f, 0.92f, 0.95f, 1.0f));
        rootNode.addLight(pl);

        // sunset light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, 1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.44f, 0.30f, 0.20f, 1.0f));
        rootNode.addLight(dl);

        // skylight
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.6f, -1, -0.6f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.10f, 0.22f, 0.44f, 1.0f));
        rootNode.addLight(dl);

        // white ambient light
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, -0.5f, -0.1f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.50f, 0.40f, 0.50f, 1.0f));
        rootNode.addLight(dl);
    }
}
