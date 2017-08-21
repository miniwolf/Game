import mini.app.Application;
import mini.app.SimpleApplication;
import mini.asset.MaterialKey;
import mini.asset.ModelKey;
import mini.light.DirectionalLight;
import mini.light.PointLight;
import mini.material.Material;
import mini.material.plugins.MiniLoader;
import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.objConverter.ObjFileLoader;
import mini.scene.Geometry;
import mini.scene.plugins.OBJLoader;
import mini.scene.shape.Sphere;
import mini.utils.MyFile;
import mini.utils.TangentBinormalGenerator;

import java.io.IOException;

/**
 * Created by miniwolf on 06-05-2017.
 */
public class TestSimpleLighting extends SimpleApplication {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        TestSimpleLighting app = new TestSimpleLighting();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Geometry teapot = null;
        try {
            teapot = (Geometry) OBJLoader.load(new ModelKey("obj/teapot.obj"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TangentBinormalGenerator.generate(teapot.getMesh(), true);
        teapot.setLocalScale(2f);
        Material mat = new Material("MatDefs/Light/Lighting.minid");
        mat.setFloat("Shininess", 25);
        mat.setBoolean("UseMaterialColors", true);
        cam.setLocation(new Vector3f(0.015041917f, 0.4572918f, 5.2874837f));
        cam.setRotation(new Quaternion(-1.8875003E-4f, 0.99882424f, 0.04832061f, 0.0039016632f));

        mat.setColor("Ambient", ColorRGBA.Black);
        mat.setColor("Diffuse", ColorRGBA.Gray);
        mat.setColor("Specular", ColorRGBA.Gray);

        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);

        Geometry lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        try {
            lightMdl.setMaterial(
                    (Material) MiniLoader.load(new MaterialKey("Materials/RedColor.mini")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lightMdl.getMesh().setStatic();
        rootNode.attachChild(lightMdl);

        PointLight pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        pl.setRadius(4f);
        rootNode.addLight(pl);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        dl.setColor(ColorRGBA.Green);
        rootNode.addLight(dl);
    }
}
