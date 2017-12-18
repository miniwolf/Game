package mini.post;

import mini.app.SimpleApplication;
import mini.light.AmbientLight;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Spatial;

public class TestSSAO extends SimpleApplication {
    public static void main(String[] args) {
        TestSSAO app = new TestSSAO();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(68.5f, 8, 8));

        AmbientLight light = new AmbientLight();
        light.setColor(new ColorRGBA(1.8f, 1.8f, 1.8f, 1.0f));

        rootNode.addLight(light);

        Material material = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Spatial model = assetManager.loadModel("Models/Sponza/sponza.obj");
        rootNode.attachChild(model);
    }
}
