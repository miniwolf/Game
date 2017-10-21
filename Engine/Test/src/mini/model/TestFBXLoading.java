package mini.model;

import mini.app.SimpleApplication;
import mini.light.DirectionalLight;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Spatial;

public class TestFBXLoading extends SimpleApplication {


    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        TestFBXLoading app = new TestFBXLoading();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        Spatial bumpy = assetManager
                .loadModel("Models/Chloe/Chloe Price (No Jacket Episode 2).FBX");
        bumpy.setLocalTranslation(0, -1, 0);
        bumpy.setLocalScale(0.025f);
        bumpy.rotate(0, 0.4f, 0);
        rootNode.attachChild(bumpy);

        // sunset light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, 1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.44f, 0.30f, 0.20f, 1.0f));
        rootNode.addLight(dl);
    }
}
