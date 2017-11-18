package mini.collision;

import mini.app.SimpleApplication;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Ray;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.debug.Arrow;
import mini.scene.shape.Box;

public class TestMousePicker extends SimpleApplication {
    private Geometry mark;
    private Node shootables;

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        TestMousePicker app = new TestMousePicker();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        initMark(); // a red sphere to mark the hit

        // Create four coloured boxes and a floor to "shoot" at:

        shootables = new Node("Shootables");
        rootNode.attachChild(shootables);

        shootables.attachChild(makeCube("Dragon", -2, 0, 1));
        shootables.attachChild(makeCube("Tin can", 1, -2, 0));
        shootables.attachChild(makeCube("Sheriff", 0, 1, -2));
        shootables.attachChild(makeCube("Deputy", 1, 0, -4));
        shootables.attachChild(makeFloor());
    }

    @Override
    public void simpleUpdate() {
        Vector3f origin = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
        Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);
        CollisionResults result = new CollisionResults();
        shootables.collideWith(ray, result);

        if (result.size() > 0) {
            CollisionResult closest = result.getClosestCollision();
            mark.setLocalTranslation(closest.getContactPoint());

            Quaternion q = new Quaternion();
            q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
            mark.setLocalRotation(q);

            rootNode.attachChild(mark);
        } else {
            rootNode.detachChild(mark);
        }
    }

    /**
     * A floor to show that the "shot" can go through several objects.
     *
     * @return a floor
     */
    private Geometry makeFloor() {
        Box box = new Box(15, .2f, 15);
        Geometry cube = new Geometry("Floor", box);
        cube.setLocalTranslation(0, -4, -5);
        Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        material.setColor("Color", ColorRGBA.Green);
        cube.setMaterial(material);
        return cube;
    }

    private Geometry makeCube(String name, float x, float y, float z) {
        Box box = new Box(1, 1, 1);
        Geometry cube = new Geometry(name, box);
        cube.setLocalTranslation(x, y, z);
        Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        material.setColor("Color", ColorRGBA.randomColor());
        cube.setMaterial(material);
        return cube;
    }

    private void initMark() {
        Arrow arrow = new Arrow(Vector3f.UNIT_Z.mult(2f));

        mark = new Geometry("BOOM!", arrow);

        Material markMat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        markMat.getAdditionalRenderState().setLineWidth(3);
        markMat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(markMat);
    }
}
