package mini.light;

import mini.app.SimpleApplication;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.FastMath;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.LightNode;
import mini.scene.Node;
import mini.scene.shape.Sphere;
import mini.scene.shape.Torus;

public class TestLightNode extends SimpleApplication {
    private float angle;
    private Node lightParentNode;

    public static void main(String[] args) {
        TestLightNode app = new TestLightNode();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Torus torus = new Torus(10, 6, 1, 3);
        Geometry g = new Geometry("Torus geometry", torus);
        g.rotate(-FastMath.HALF_PI, 0, 0);
        g.center();

        Material material = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        material.setFloat("Shininess", 32f);
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", ColorRGBA.Black);
        material.setColor("Diffuse", ColorRGBA.White);
        material.setColor("Specular", ColorRGBA.White);
        g.setMaterial(material);

        rootNode.attachChild(g);

        Geometry lightModel = new Geometry("Light", new Sphere(10, 10, 0.1f));
        lightModel.setMaterial(assetManager.loadMaterial("Materials/RedColor.mini"));

        lightParentNode = new Node("LightParentNode");
        lightParentNode.attachChild(lightModel);
        rootNode.attachChild(lightParentNode);

        PointLight pointLight = new PointLight();
        pointLight.setColor(ColorRGBA.Green);
        pointLight.setRadius(4f);
        rootNode.addLight(pointLight);

        LightNode lightNode = new LightNode("pointLight", pointLight);
        lightParentNode.attachChild(lightNode);

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setColor(ColorRGBA.Red);
        directionalLight.setDirection(new Vector3f(0, 1, 0));
        rootNode.addLight(directionalLight);
    }

    @Override
    public void simpleUpdate(float tpf) {
        angle += tpf;
        angle %= FastMath.TWO_PI;

        lightParentNode.setLocalTranslation(
                new Vector3f(FastMath.cos(angle) * 3f, 2, FastMath.sin(angle) * 3f));
    }
}
