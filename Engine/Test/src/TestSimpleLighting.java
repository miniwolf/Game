import mini.asset.MaterialKey;
import mini.material.plugins.MiniLoader;
import mini.renderEngine.CameraImpl;
import mini.light.PointLight;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.objConverter.ObjFileLoader;
import mini.renderEngine.RenderEngine;
import mini.renderEngine.RenderManager;
import mini.renderEngine.ViewPort;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.shape.Sphere;
import mini.shaders.VarType;
import mini.utils.DisplayManager;
import mini.utils.MyFile;

import java.io.IOException;

/**
 * Created by miniwolf on 06-05-2017.
 */
public class TestSimpleLighting {
    protected Node rootNode = new Node("Root Node");
    protected ViewPort viewPort;
    protected GLRenderer renderer;
    protected CameraImpl cam;
    protected RenderManager renderManager;
    private DisplayManager displayManager;

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/lwjgl-natives-windows/");
        TestSimpleLighting app = new TestSimpleLighting();
        app.initialize();
        while (true) {
            app.update();
        }
    }

    public void update() {
        displayManager.update();
        rootNode.updateGeometricState();

        renderManager.render(true);
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     * <p>
     * Initializes the <code>Application</code>, by creating a display and
     * default camera. If display settings are not specified, a default
     * 640x480 display is created. Default values are used for the camera;
     * perspective projection with 45° field of view, with near
     * and far values 1 and 1000 units respectively.
     */
    public void initialize() {
        initDisplay();
        initCamera();
        viewPort.attachScene(rootNode);
        userInit();
    }

    private void initDisplay() {
        displayManager = DisplayManager.createDisplay();
        renderer = new GLRenderer();
        renderer.initialize();
    }

    /**
     * Creates the camera to use for rendering. Default values are perspective
     * projection with 45° field of view, with near and far values 1 and 1000
     * units respectively.
     */
    private void initCamera() {
        cam = new CameraImpl(1024, 768);

        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 1f, 1000f);
        cam.setPosition(new Vector3f(0f, 0f, 10f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        renderManager = new RenderManager(renderer);

        viewPort = renderManager.createMainView("Default", cam);
        viewPort.setClearFlags(true, true, true);
    }

    private void userInit() {
        Geometry teapot = (Geometry) ObjFileLoader.loadOBJ(new MyFile("Game/obj", "teapot.obj"));
        teapot.setLocalScale(2f);
        Material mat = new Material("Engine/MatDefs/Light/Lighting.minid");
        mat.setFloat("Shininess", 25);
        mat.setBoolean("UseMaterialColors", true);
        cam.setPosition(new Vector3f(0.015041917f, 0.4572918f, 5.2874837f));
        cam.setRotation(new Quaternion(-1.8875003E-4f, 0.99882424f, 0.04832061f, 0.0039016632f));

        mat.setColor("Ambient", ColorRGBA.Black);
        mat.setColor("Diffuse", ColorRGBA.Gray);
        mat.setColor("Specular", ColorRGBA.Gray);

        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);

        Geometry lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
        try {
            lightMdl.setMaterial(
                    (Material) MiniLoader.load(new MaterialKey("Engine/Materials/RedColor.mini")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lightMdl.getMesh().setStatic();
        rootNode.attachChild(lightMdl);

        PointLight pl = new PointLight();
        pl.setRadius(4f);
        rootNode.addLight(pl);
    }
}
