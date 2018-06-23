package mini.niftygui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import mini.app.SimpleApplication;
import mini.asset.ModelKey;
import mini.light.DirectionalLight;
import mini.light.PointLight;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.post.niftygui.NiftyMiniDisplay;
import mini.scene.Geometry;
import mini.utils.TangentBinormalGenerator;

public class TestNiftyGuiController extends SimpleApplication implements ScreenController {
    private Button applyButton;
    private Nifty nifty;

    public static void main(String[] args) {
        TestNiftyGuiController app = new TestNiftyGuiController();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        NiftyMiniDisplay niftyDisplay = new NiftyMiniDisplay(assetManager, guiViewPort,
                                                             inputManager);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("minitest/nifty/Button.xml", "Screen", this);
        guiViewPort.addProcessor(niftyDisplay);
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);

        addTeapot();
        addLighting();
    }

    private void addLighting() {
        PointLight pl = new PointLight();
        pl.setColor(ColorRGBA.White);
        pl.setRadius(4f);
        rootNode.addLight(pl);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        dl.setColor(ColorRGBA.Green);
        rootNode.addLight(dl);
    }

    private void addTeapot() {
        Geometry teapot = (Geometry) assetManager
                .loadAsset(new ModelKey("/Models/Teapot/Teapot.obj"));
        TangentBinormalGenerator.generate(teapot.getMesh(), true);
        teapot.setLocalScale(2f);
        Material mat = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        mat.setFloat("Shininess", 25);
        mat.setBoolean("UseMaterialColors", true);
        cam.setLocation(new Vector3f(0.015041917f, 0.4572918f, 5.2874837f));
        cam.setRotation(new Quaternion(-1.8875003E-4f, 0.99882424f, 0.04832061f, 0.0039016632f));

        mat.setColor("Ambient", ColorRGBA.Black);
        mat.setColor("Diffuse", ColorRGBA.Gray);
        mat.setColor("Specular", ColorRGBA.Gray);

        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);
    }

    @NiftyEventSubscriber(id = "ApplyButton")
    public void onButton(String id, ButtonClickedEvent event) {
        System.err.println("It works " + id);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        applyButton = screen.findNiftyControl("ApplyButton", Button.class);
    }

    @NiftyEventSubscriber(id = "Layer0")
    public void onElementPrimaryClick(final String id, final NiftyMousePrimaryClickedEvent event) {
        System.out.println(id);
    }

    public void primaryClick() {
        System.out.println("Primary clicked on nifty internal");
    }

    public void mouseMoved() {
        System.out.println("Mouse moved on nifty internal");
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
