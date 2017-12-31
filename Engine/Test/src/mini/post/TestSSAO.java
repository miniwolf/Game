package mini.post;

import mini.app.SimpleApplication;
import mini.input.KeyboardKey;
import mini.input.controls.KeyTrigger;
import mini.light.AmbientLight;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.scene.Geometry;
import mini.utils.MaterialDebugApplicationState;
import mini.voxelization.VoxelizationRenderer;

public class TestSSAO extends SimpleApplication {
    public static void main(String[] args) {
        TestSSAO app = new TestSSAO();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //cam.setLocation(new Vector3f(8, 8, 8));

        AmbientLight light = new AmbientLight();
        light.setColor(new ColorRGBA(1.8f, 1.8f, 1.8f, 1.0f));

        rootNode.addLight(light);

        //Material material = new Material(assetManager, "MatDefs/Voxelization/Visualization/WorldPosShader.minid");
        Geometry model = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.obj");
        Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");

        material.setColor("Color", ColorRGBA.Red);
//        Node model = (Node) assetManager.loadModel("Models/Sponza/sponza.obj");
        //model.setMaterial(material);
//        model.setLocalScale(0.01f, 0.01f, 0.01f);
        rootNode.attachChild(model);

//        MaterialDebugApplicationState debug = new MaterialDebugApplicationState();
//        debug.registerBinding("MatDefs/Voxelization/Visualization/WorldPosShader.vert", model);
//        debug.registerBinding("MatDefs/Voxelization/Visualization/WorldPosShader.frag", model);
//        debug.registerBinding(new KeyTrigger(KeyboardKey.KEY_R), model);
//        stateManager.attach(debug);

        VoxelizationRenderer voxelizationRenderer = new VoxelizationRenderer(assetManager);
        viewPort.addProcessor(voxelizationRenderer);
    }
}
