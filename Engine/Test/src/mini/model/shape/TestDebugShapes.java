package mini.model.shape;

import mini.app.SimpleApplication;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.debug.Arrow;
import mini.scene.debug.Grid;
import mini.scene.debug.WireBox;

public class TestDebugShapes extends SimpleApplication {

    public static void main(String[] args) {
        TestDebugShapes app = new TestDebugShapes();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(2, 1.5f, 2));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        putArrow(Vector3f.ZERO, Vector3f.UNIT_X, ColorRGBA.Red);
        putArrow(Vector3f.ZERO, Vector3f.UNIT_Y, ColorRGBA.Green);
        putArrow(Vector3f.ZERO, Vector3f.UNIT_Z, ColorRGBA.Blue);

        putBox(new Vector3f(2, 0, 0), 0.5f, ColorRGBA.Yellow);
        putGrid(new Vector3f(3.5f, 0, 0), ColorRGBA.White);
        putSphere(new Vector3f(4.5f, 0, 0), ColorRGBA.Magenta);
    }

    private void putSphere(Vector3f pos, ColorRGBA color) {
        //putShape(new WireSphere) TODO:
    }

    private void putGrid(Vector3f pos, ColorRGBA color) {
        putShape(new Grid(6, 6, 0.2f), color, 1).center().move(pos);
    }

    private void putBox(Vector3f pos, float size, ColorRGBA color) {
        putShape(new WireBox(size, size, size), color, 1).setLocalTranslation(pos);
    }

    private void putArrow(Vector3f pos, Vector3f dir, ColorRGBA color) {
        Arrow arrow = new Arrow(dir);
        putShape(arrow, color, 4).setLocalTranslation(pos);
    }

    private Geometry putShape(Mesh shape, ColorRGBA color, int lineWith) {
        Geometry geom = new Geometry("Shape", shape);
        Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        material.getAdditionalRenderState().setWireframe(true);
        material.getAdditionalRenderState().setLineWidth(lineWith);
        material.setColor("Color", color);
        geom.setMaterial(material);

        rootNode.attachChild(geom);
        return geom;
    }
}
