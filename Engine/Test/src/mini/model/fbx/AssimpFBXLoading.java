package mini.model.fbx;

import mini.app.SimpleApplication;
import mini.scene.plugins.AssimpLoader;

public class AssimpFBXLoading extends SimpleApplication {
    public static void main(String[] args) {
        AssimpFBXLoading app = new AssimpFBXLoading();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(AssimpLoader.class, "obj");

        assetManager.loadModel("/Models/Sponza/sponza.obj");
    }
}
