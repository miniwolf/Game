package mini.model.fbx;

import mini.animation.AnimationControl;
import mini.animation.SkeletonControl;
import mini.app.SimpleApplication;
import mini.input.KeyboardKey;
import mini.input.controls.ActionListener;
import mini.input.controls.KeyTrigger;
import mini.light.AmbientLight;
import mini.light.DirectionalLight;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.debug.SkeletonDebugger;
import mini.textures.Texture;
import mini.utils.sky.SkyFactory;

public class LoadModelWithAnimation extends SimpleApplication implements ActionListener {
    private Node bumpy;
    private Spatial reference;
    private AnimationControl control;

    public static void main(String[] args) {
        LoadModelWithAnimation app = new LoadModelWithAnimation();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
        Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
        Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
        Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
        Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
        Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");

        Spatial sky = new SkyFactory(assetManager).createSky(west, east, north, south, up, down);
        rootNode.attachChild(sky);

        viewPort.setBackgroundColor(new ColorRGBA(0.1019f, 0.2823f, 0.3843f, 1));

        //setupAnimation();
        setupAnimation2();

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

    private void setupAnimation2() {
        bumpy = assetManager.loadModel("Models/mia/Mixamo-Mia_JoyfulJumpAnimation.fbx");
        bumpy.setLocalTranslation(0, -1, 0);
        bumpy.setLocalScale(0.01f);
        rootNode.attachChild(bumpy);
    }

    private void setupAnimation() {
        bumpy = assetManager.loadModel("Models/animation/demo/DefaultAvatar.fbx");

        bumpy.setLocalTranslation(0, -1, 0);
        bumpy.setLocalScale(0.01f);
        rootNode.attachChild(bumpy);

        Node spatial = assetManager.loadModel("Models/animation/demo/animation/06_13.fbx");
        //var animation = constructAnimationFromTakes(spatial.takes.get(0), bumpy); // Maybe a little bit more? Though it is a test...
        spatial.setLocalTranslation(0, -1, 0);
        spatial.setLocalScale(0.01f);

        //rootNode.attachChild(spatial);
        bumpy.removeControl(AnimationControl.class);

        AnimationControl animationControl = spatial.getControl(AnimationControl.class);
        animationControl.reassign(bumpy);
        animationControl.setSkeleton(bumpy.getControl(SkeletonControl.class).getSkeleton());
        bumpy.addControl(animationControl);
        //animationControl.addAnimation(animation);
        SkeletonControl skeletonControl = bumpy.getControl(SkeletonControl.class);
        skeletonControl.setHardwareSkinningPreferred(false);

        var skeletonDebugger = new SkeletonDebugger("Skeleton1",
                                                    bumpy.getControl(AnimationControl.class)
                                                         .getSkeleton());
        var mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Green);
        mat.setFloat("PointSize", 7f);
        mat.getAdditionalRenderState().setDepthTest(false);
        skeletonDebugger.setMaterial(mat);

        var skeletonDebugger2 = new SkeletonDebugger("Skeleton2",
                                                     spatial.getControl(AnimationControl.class)
                                                            .getSkeleton());
        var mat2 = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        mat2.getAdditionalRenderState().setWireframe(true);
        mat2.setColor("Color", ColorRGBA.Red);
        mat2.setFloat("PointSize", 7f);
        mat2.getAdditionalRenderState().setDepthTest(false);
        skeletonDebugger2.setMaterial(mat2);
        bumpy.attachChild(skeletonDebugger2);
        bumpy.attachChild(skeletonDebugger);

        reference = assetManager.loadModel("Models/Teapot/Teapot.obj");
        reference.setLocalTranslation(15, -1, 15);
        rootNode.attachChild(reference);

        animationControl.createChannel().setAnim("06_13");
    }

//    private Animation constructAnimationFromTakes(FBXTake take, Node node) {
//        var animation = new Animation(take.getName(), take.getTime(), node);
//        animation.setTracks(take.getTracks().toArray(new SpatialTrack[0]));
//        return animation;
//    }

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
