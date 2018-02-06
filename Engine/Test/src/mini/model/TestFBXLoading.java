package mini.model;

import mini.app.SimpleApplication;
import mini.input.KeyboardKey;
import mini.input.controls.AnalogListener;
import mini.input.controls.KeyTrigger;
import mini.light.DirectionalLight;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Spatial;

public class TestFBXLoading extends SimpleApplication implements AnalogListener {
    private Spatial bumpy;

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        TestFBXLoading app = new TestFBXLoading();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setupInput();

        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        flyCam.setMoveSpeed(20);

//        bumpy = assetManager.loadModel("Models/chest/Models/chest.fbx");
        bumpy = assetManager
                .loadModel("Models/Chloe/Chloe Price (No Jacket Episode 2).FBX");
        bumpy.setLocalTranslation(0, -1, 0);
        bumpy.rotate(0, 0f, 0);
        rootNode.attachChild(bumpy);

        // sunset light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, 1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.44f, 0.30f, 0.20f, 1.0f));
        rootNode.addLight(dl);
    }

    private void setupInput() {
        inputManager.addMapping("RotateY", new KeyTrigger(KeyboardKey.KEY_R));
        inputManager.addMapping("RotateX", new KeyTrigger(KeyboardKey.KEY_X));
        inputManager.addMapping("RotateZ", new KeyTrigger(KeyboardKey.KEY_F));
        inputManager.addListener(this, "RotateX", "RotateY", "RotateZ");
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("RotateY")) {
            bumpy.rotate(0, value, 0);
        }
        if (name.equals("RotateX")) {
            bumpy.rotate(value, 0, 0);
        }
        if (name.equals("RotateZ")) {
            bumpy.rotate(0, 0, value);
        }
    }
}
