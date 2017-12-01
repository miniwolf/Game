package mini.light;

import mini.app.SimpleApplication;
import mini.input.controls.ActionListener;
import mini.input.controls.AnalogListener;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.FastMath;
import mini.math.Quaternion;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Spatial;
import mini.scene.shape.Box;
import mini.scene.shape.Sphere;
import mini.shadow.DirectionalLightShadowRenderer;
import mini.shadow.EdgeFilteringMode;
import mini.textures.Texture;
import mini.utils.TangentBinormalGenerator;

import java.util.ArrayList;
import java.util.List;

public class TestDirectionalLightShadow extends SimpleApplication implements ActionListener,
                                                                             AnalogListener {
    private static final int SHADOWMAP_SIZE = 1024;

    private List<Spatial> objects = new ArrayList<>();
    private Geometry ground;
    private Material[] mats;
    private Material matGroundU, matGroundL;

    private DirectionalLight light;
    private DirectionalLightShadowRenderer lightShadowRenderer;

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        TestDirectionalLightShadow app = new TestDirectionalLightShadow();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Put camera in a bad position
        cam.setLocation(new Vector3f(3.5f, 43, -83.5f));
        cam.setRotation(new Quaternion(0.2f, -0.09f, 0.01f, 0.98f));

        flyCam.setMoveSpeed(100);

        loadSceneData();

        lightShadowRenderer = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        lightShadowRenderer.setLight(light);
        lightShadowRenderer.setLambda(0.55f);
        lightShadowRenderer.setShadowIntensity(0.8f);
        lightShadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
        lightShadowRenderer.displayDebug();
        viewPort.addProcessor(lightShadowRenderer);
    }

    private void loadSceneData() {
        Geometry sphere = new Geometry("Sphere", new Sphere(30, 30, 2));
        sphere.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Geometry cube = new Geometry("Cube", new Box(1, 1, 1));
        cube.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        objects.add(sphere);
        objects.add(cube);

        mats = new Material[2];
        mats[0] = assetManager.loadMaterial("Materials/RedColor.mini");
        mats[1] = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.mini");
        mats[1].setBoolean("UseMaterialColors", true);
        mats[1].setColor("Ambient", ColorRGBA.White);
        mats[1].setColor("Diffuse", ColorRGBA.White.clone());

        objects.parallelStream().forEach(TangentBinormalGenerator::generate);

        Spatial t = objects.get(0).clone(false);
        t.setLocalScale(10f);
        t.setLocalTranslation(0, 25, 0);
        t.setMaterial(mats[0]);
        rootNode.attachChild(t);

        for (int i = 0; i < 60; i++) {
            t = objects.get(FastMath.nextRandomInt(0, objects.size() - 1)).clone(false);
            t.setLocalScale(FastMath.nextRandomFloat() * 10f);
            t.setMaterial(mats[FastMath.nextRandomInt(0, mats.length - 1)]);
            t.setLocalTranslation(FastMath.nextRandomFloat() * 200f,
                                  FastMath.nextRandomFloat() * 30f + 20, 30f * (i * 2f));
            rootNode.attachChild(t);
        }

        Box box = new Box(1000, 2, 1000);
        box.scaleTextureCoordinates(new Vector2f(10, 10));

        ground = new Geometry("scil", box);
        ground.setLocalTranslation(0, 10, 550);
        matGroundU = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        matGroundU.setColor("Color", ColorRGBA.Green);

        matGroundL = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Texture grass = assetManager.loadTexture("Textures/Terrain/Splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        matGroundL.setTexture("DiffuseMap", grass);

        ground.setMaterial(matGroundL);
        ground.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(ground);

        light = new DirectionalLight();
        light.setDirection(new Vector3f(-1, -1, -1));
        rootNode.addLight(light);

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.02f));
        rootNode.addLight(ambientLight);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

    }

    @Override
    public void onAnalog(String name, float value, float tpf) {

    }
}
