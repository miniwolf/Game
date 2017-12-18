package mini.light;

import mini.app.SimpleApplication;
import mini.material.Material;
import mini.material.TechniqueDef;
import mini.math.ColorRGBA;
import mini.math.FastMath;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.shape.Quad;

public class TestManyLights extends SimpleApplication {
    TechniqueDef.LightMode lightMode = TechniqueDef.LightMode.SinglePass;

    public static void main(String[] args) {
        TestManyLights app = new TestManyLights();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        renderManager.setPreferredLightMode(lightMode);
        renderManager.setSinglePassLightBatchSize(6);

        flyCam.setMoveSpeed(10);

        setupPointLight("Lamp", new Vector3f(23, 6, 24), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp1", new Vector3f(8, 6, 24), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp2", new Vector3f(8, 6, 24), Vector3f.ONE, 5000, ColorRGBA.Red);
        setupPointLight("Lamp3", new Vector3f(24, 6, 8), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp4", new Vector3f(8, 6, 8), Vector3f.ONE, 5000, ColorRGBA.Red);
        setupPointLight("Lamp5", new Vector3f(8, 6, 8), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp6", new Vector3f(24, 6, 8), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp7", new Vector3f(24, 6, 24), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp8", new Vector3f(7, 6, 24), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp9", new Vector3f(8, 6, 8), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp10", new Vector3f(8, 6, 8), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp11", new Vector3f(9, 6, 23), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp12", new Vector3f(24, 6, 24), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp13", new Vector3f(8, 6, 24), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp14", new Vector3f(23, 6, 8), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());
        setupPointLight("Lamp15", new Vector3f(23, 6, 24), Vector3f.ONE, 5000,
                        ColorRGBA.randomColor());

        Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        Geometry ground = new Geometry("ground", new Quad(100, 100));
        ground.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        ground.setLocalTranslation(0, -0.91f, 0);
        ground.setMaterial(material);
        rootNode.attachChild(ground);
    }

    private void setupPointLight(String name, Vector3f position, Vector3f scale,
                                 float radius, ColorRGBA color) {
        Node lamp = new Node(name);
        lamp.setLocalTranslation(position);
        PointLight pointLight = new PointLight();
        pointLight.setRadius(radius);
        pointLight.setColor(color);
        lamp.addLight(pointLight);
        rootNode.attachChild(lamp);
    }
}
