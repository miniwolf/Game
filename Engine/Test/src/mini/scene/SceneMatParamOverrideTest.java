package mini.scene;

import mini.material.MatParamOverride;
import org.junit.Test;

import static mini.scene.MPOTestUtils.mpoBool;
import static mini.scene.MPOTestUtils.mpoInt;
import static mini.scene.MPOTestUtils.validateScene;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Validates how {@link MatParamOverride MPOs} work on the scene level.
 */
public class SceneMatParamOverrideTest {

    private static Node createDummyScene() {
        Node scene = new Node("Scene Node");

        Node a = new Node("A");
        Node b = new Node("B");

        Node c = new Node("C");
        Node d = new Node("D");

        Node e = new Node("E");
        Node f = new Node("F");

        Node g = new Node("G");
        Node h = new Node("H");
        Node j = new Node("J");

        scene.attachChild(a);
        scene.attachChild(b);

        a.attachChild(c);
        a.attachChild(d);

        b.attachChild(e);
        b.attachChild(f);

        c.attachChild(g);
        c.attachChild(h);
        c.attachChild(j);

        return scene;
    }

    @Test
    public void testOverrides_Empty() {
        Node n = new Node("Node");
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());

        n.updateGeometricState();
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());
    }

    @Test
    public void testOverrides_AddRemove() {
        MatParamOverride override = mpoBool("Test", true);
        Node n = new Node("Node");

        n.removeMatParamOverride(override);
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());

        n.addMatParamOverride(override);

        assertSame(n.getLocalMatParamOverrides().get(0), override);
        assertTrue(n.getWorldMatParamOverrides().isEmpty());
        n.updateGeometricState();

        assertSame(n.getLocalMatParamOverrides().get(0), override);
        assertSame(n.getWorldMatParamOverrides().get(0), override);

        n.removeMatParamOverride(override);
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertSame(n.getWorldMatParamOverrides().get(0), override);

        n.updateGeometricState();
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());
    }

    @Test
    public void testOverrides_Clear() {
        MatParamOverride override = mpoBool("Test", true);
        Node n = new Node("Node");

        n.clearMatParamOverrides();
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());

        n.addMatParamOverride(override);
        n.clearMatParamOverrides();
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());

        n.addMatParamOverride(override);
        n.updateGeometricState();
        n.clearMatParamOverrides();
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertSame(n.getWorldMatParamOverrides().get(0), override);

        n.updateGeometricState();
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());

        n.addMatParamOverride(override);
        n.clearMatParamOverrides();
        n.updateGeometricState();
        assertTrue(n.getLocalMatParamOverrides().isEmpty());
        assertTrue(n.getWorldMatParamOverrides().isEmpty());
    }

    @Test
    public void testOverrides_AddAfterAttach() {
        Node scene = createDummyScene();
        scene.updateGeometricState();

        Node root = new Node("Root Node");
        root.updateGeometricState();

        root.attachChild(scene);
        scene.getChild("A").addMatParamOverride(mpoInt("val", 5));

        validateScene(root);
    }

    @Test
    public void testOverrides_AddBeforeAttach() {
        Node scene = createDummyScene();
        scene.getChild("A").addMatParamOverride(mpoInt("val", 5));
        scene.updateGeometricState();

        Node root = new Node("Root Node");
        root.updateGeometricState();

        root.attachChild(scene);

        validateScene(root);
    }

    @Test
    public void testOverrides_RemoveBeforeAttach() {
        Node scene = createDummyScene();
        scene.updateGeometricState();

        Node root = new Node("Root Node");
        root.updateGeometricState();

        scene.getChild("A").addMatParamOverride(mpoInt("val", 5));
        validateScene(scene);

        scene.getChild("A").clearMatParamOverrides();
        validateScene(scene);

        root.attachChild(scene);
        validateScene(root);
    }

    @Test
    public void testOverrides_RemoveAfterAttach() {
        Node scene = createDummyScene();
        scene.updateGeometricState();

        Node root = new Node("Root Node");
        root.updateGeometricState();

        scene.getChild("A").addMatParamOverride(mpoInt("val", 5));

        root.attachChild(scene);
        validateScene(root);

        scene.getChild("A").clearMatParamOverrides();
        validateScene(root);
    }

    @Test
    public void testOverrides_IdenticalNames() {
        Node scene = createDummyScene();

        scene.getChild("A").addMatParamOverride(mpoInt("val", 5));
        scene.getChild("C").addMatParamOverride(mpoInt("val", 7));

        validateScene(scene);
    }

//    @Test
//    public void testOverrides_CloningScene_DoesntCloneMPO() {
//        Node originalScene = createDummyScene();
//
//        originalScene.getChild("A").addMatParamOverride(mpoInt("int", 5));
//        originalScene.getChild("A").addMatParamOverride(mpoBool("bool", true));
//        originalScene.getChild("A").addMatParamOverride(mpoFloat("float", 3.12f));
//
//        Node clonedScene = originalScene.clone(false);
//
//        validateScene(clonedScene);
//        validateScene(originalScene);
//
//        List<MatParamOverride> clonedOverrides = clonedScene.getChild("A").getLocalMatParamOverrides();
//        List<MatParamOverride> originalOverrides = originalScene.getChild("A").getLocalMatParamOverrides();
//
//        assertNotSame(clonedOverrides, originalOverrides);
//        assertEquals(clonedOverrides, originalOverrides);
//
//        for (int i = 0; i < clonedOverrides.size(); i++) {
//            assertNotSame(clonedOverrides.get(i), originalOverrides.get(i));
//            assertEquals(clonedOverrides.get(i), originalOverrides.get(i));
//        }
//    }

//    @Test
//    public void testOverrides_SaveAndLoad_KeepsMPOs() {
//        MatParamOverride override = mpoInt("val", 5);
//        Node scene = createDummyScene();
//        scene.getChild("A").addMatParamOverride(override);
//
//        Node loadedScene = BinaryExporter.saveAndLoad(assetManager, scene);
//
//        Node root = new Node("Root Node");
//        root.attachChild(loadedScene);
//        validateScene(root);
//        validateScene(scene);
//
//        assertNotSame(override, loadedScene.getChild("A").getLocalMatParamOverrides().get(0));
//        assertEquals(override, loadedScene.getChild("A").getLocalMatParamOverrides().get(0));
//    }

    @Test
    public void testEquals() {
        assertEquals(mpoInt("val", 5), mpoInt("val", 5));
        assertEquals(mpoBool("val", true), mpoBool("val", true));
        assertNotEquals(mpoInt("val", 5), mpoInt("val", 6));
        assertNotEquals(mpoInt("val1", 5), mpoInt("val2", 5));
        assertNotEquals(mpoBool("val", true), mpoInt("val", 1));
    }
}
