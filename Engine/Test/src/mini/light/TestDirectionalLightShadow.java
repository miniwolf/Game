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
import mini.shadow.DirectionalLightShadowFilter;
import mini.shadow.DirectionalLightShadowRenderer;
import mini.shadow.EdgeFilteringMode;
import mini.textures.Texture;
import mini.utils.TangentBinormalGenerator;

public class TestDirectionalLightShadow extends SimpleApplication
        implements ActionListener, AnalogListener {

    public static final int SHADOWMAP_SIZE = 1024;
    private Spatial[] obj;
    private Material[] mat;
    private DirectionalLightShadowRenderer dlsr;
    private DirectionalLightShadowFilter dlsf;
    private Geometry ground;
    private Material matGroundU;
    private Material matGroundL;
    private DirectionalLight light;
    private AmbientLight al;
    private float frustumSize = 100;

    public static void main(String[] args) {
        TestDirectionalLightShadow app = new TestDirectionalLightShadow();
        app.start();
    }

    public void onAnalog(String name, float value, float tpf) {
        if (cam.isParallelProjection()) {
            // Instead of moving closer/farther to object, we zoom in/out.
            if (name.equals("Size-")) {
                frustumSize += 5f * tpf;
            } else {
                frustumSize -= 5f * tpf;
            }

            float aspect = (float) cam.getWidth() / cam.getHeight();
            cam.setFrustum(-1000, 1000, -aspect * frustumSize, aspect * frustumSize, frustumSize,
                           -frustumSize);
        }
    }

    public void loadScene() {
        obj = new Spatial[2];
        // Setup first view

        mat = new Material[2];
        mat[0] = assetManager.loadMaterial("Materials/RedColor.mini");
        mat[1] = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.mini");
        mat[1].setBoolean("UseMaterialColors", true);
        mat[1].setColor("Ambient", ColorRGBA.White);
        mat[1].setColor("Diffuse", ColorRGBA.White.clone());

        obj[0] = new Geometry("sphere", new Sphere(30, 30, 2));
        obj[0].setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        obj[1] = new Geometry("cube", new Box(1.0f, 1.0f, 1.0f));
        obj[1].setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        TangentBinormalGenerator.generate(obj[1]);
        TangentBinormalGenerator.generate(obj[0]);

        Spatial t = obj[0].clone(false);
        t.setLocalScale(10f);
        t.setMaterial(mat[1]);
        rootNode.attachChild(t);
        t.setLocalTranslation(0, 25, 0);

        for (int i = 0; i < 60; i++) {
            t = obj[FastMath.nextRandomInt(0, obj.length - 1)].clone(false);
            t.setLocalScale(FastMath.nextRandomFloat() * 10f);
            t.setMaterial(mat[FastMath.nextRandomInt(0, mat.length - 1)]);
            rootNode.attachChild(t);
            t.setLocalTranslation(FastMath.nextRandomFloat() * 200f,
                                  FastMath.nextRandomFloat() * 30f + 20, 30f * (i + 2f));
        }

        Box box = new Box(1000, 2, 1000);
        box.scaleTextureCoordinates(new Vector2f(10, 10));

        ground = new Geometry("soil", box);
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
    public void simpleInitApp() {
        // put the camera in a bad position
//        cam.setLocation(new Vector3f(65.25412f, 44.38738f, 9.087874f));
//        cam.setRotation(new Quaternion(0.078139365f, 0.050241485f, -0.003942559f, 0.9956679f));

        cam.setLocation(new Vector3f(3.3720117f, 42.838284f, -83.43792f));
        cam.setRotation(new Quaternion(0.13833192f, -0.08969371f, 0.012581267f, 0.9862358f));

        flyCam.setMoveSpeed(100);

        loadScene();

        dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(light);
        dlsr.setLambda(0.55f);
        dlsr.setShadowIntensity(0.8f);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
        dlsr.displayDebug();
        viewPort.addProcessor(dlsr);

//        dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
//        dlsf.setLight(l);
//        dlsf.setLambda(0.55f);
//        dlsf.setShadowIntensity(0.8f);
//        dlsf.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
//        dlsf.setEnabled(false);

//        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
//        fpp.addFilter(dlsf);

//        viewPort.addProcessor(fpp);

    }

    public void onAction(String name, boolean keyPressed, float tpf) {
    }
}
