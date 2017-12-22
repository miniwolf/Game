package mini.post;

import mini.app.SimpleApplication;
import mini.light.AmbientLight;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Node;

public class TestSSAO extends SimpleApplication {
    public static void main(String[] args) {
        TestSSAO app = new TestSSAO();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(8, 8, 8));

        AmbientLight light = new AmbientLight();
        light.setColor(new ColorRGBA(1.8f, 1.8f, 1.8f, 1.0f));

        rootNode.addLight(light);

        Node model = (Node) assetManager.loadModel("Models/Sponza/sponza.obj");
        model.setLocalScale(0.01f, 0.01f, 0.01f);
        rootNode.attachChild(model);
    }
}
