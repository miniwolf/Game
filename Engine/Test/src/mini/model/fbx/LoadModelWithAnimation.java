package mini.model.fbx;

import mini.app.SimpleApplication;
import mini.input.KeyboardKey;
import mini.input.controls.ActionListener;
import mini.input.controls.KeyTrigger;
import mini.light.AmbientLight;
import mini.light.DirectionalLight;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Node;
import mini.scene.Spatial;

public class LoadModelWithAnimation extends SimpleApplication implements ActionListener {
    private Node bumpy;
    private Spatial reference;

    public static void main(String[] args) {
        LoadModelWithAnimation app = new LoadModelWithAnimation();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.White);
        renderer.setBackgroundColor(ColorRGBA.White);

        bumpy = (Node) assetManager.loadModel("Models/animation/demo/DefaultAvatar.fbx");
        bumpy.setLocalTranslation(0, -1, 0);
        bumpy.setLocalScale(0.01f);
        rootNode.attachChild(bumpy);

        Spatial spatial = assetManager.loadModel("Models/animation/demo/animation/06_13.fbx");
        rootNode.attachChild(spatial);

        reference = assetManager.loadModel("Models/Teapot/Teapot.obj");
        reference.setLocalTranslation(15, -1, 15);
        rootNode.attachChild(reference);

        // sunset light
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(3, -3, -3).normalizeLocal());
        rootNode.addLight(dl);

        setupKey();
    }

    private void setupKey() {
        inputManager.addMapping("GotoBumpy", new KeyTrigger(KeyboardKey.KEY_F));
        inputManager.addMapping("GotoTeapot", new KeyTrigger(KeyboardKey.KEY_T));
        inputManager.addListener(this, "GotoBumpy");
        inputManager.addListener(this, "GotoTeapot");
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("GotoBumpy")) {
            flyCam.jumpTo(bumpy.getLocalTranslation());
        }

        if (binding.equals("GotoTeapot")) {
            flyCam.jumpTo(reference.getLocalTranslation());
        }
    }
}
