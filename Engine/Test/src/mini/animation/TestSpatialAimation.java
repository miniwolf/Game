package mini.animation;

import mini.app.SimpleApplication;
import mini.light.AmbientLight;
import mini.light.DirectionalLight;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.shape.Box;

public class TestSpatialAimation extends SimpleApplication {
    public static void main(String[] args) {
        TestSpatialAimation app = new TestSpatialAimation();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        AmbientLight al = new AmbientLight();
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(Vector3f.UNIT_XYZ.negate());
        rootNode.addLight(dl);

        Box box = new Box(1, 1, 1);
        Geometry geom = new Geometry("box", box);
        geom.setMaterial(assetManager.loadMaterial("Textures/Terrain/Brick/BrickWall.mini"));
        Node model = new Node("model");
        model.attachChild(geom);

        Box child = new Box(0.5f, 0.5f, 0.5f);
        Geometry childGeom = new Geometry("box", child);
        childGeom.setMaterial(assetManager.loadMaterial("Textures/Terrain/Brick/BrickWall.mini"));
        Node childModel = new Node("childModel");
        childModel.setLocalTranslation(2, 2, 2);
        childModel.attachChild(childGeom);
        model.attachChild(childModel);

        // animation parameters
        float animationTime = 5;
        int fps = 24;
        float totalMoveLengthX = 10;

        // frames
        int totalFrames = (int) (fps * animationTime);
        float t = 0, dT = animationTime / totalFrames;
        float x = 0, dX = totalMoveLengthX / totalFrames;
        float rx = 0, drX = totalMoveLengthX / totalFrames;
        float[] times = new float[totalFrames];
        Vector3f[] translations = new Vector3f[totalFrames];
        Quaternion[] rotations = new Quaternion[totalFrames];
        Vector3f[] scales = new Vector3f[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            times[i] = t;
            t += dT;
            translations[i] = new Vector3f(x, 0, 0);
            x += dX;
            rotations[i] = new Quaternion(rx, 0,0,1);
            rx += drX;
            scales[i] = Vector3f.UNIT_XYZ;
        }
        SpatialTrack spatialTrack = new SpatialTrack(times, translations, rotations, scales);

        // Create animation
        Animation animation = new Animation("animation", animationTime);
        animation.setTracks(new SpatialTrack[]{spatialTrack});

        // create spatial animation control
        AnimationControl animationControl = new AnimationControl();
        animationControl.addAnimation(animation);

        model.addControl(animationControl);

        rootNode.attachChild(model);

        animationControl.createChannel().setAnim("animation");
    }
}
