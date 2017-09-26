package mini.material;

import mini.renderEngine.Caps;
import mini.renderEngine.RenderManager;
import mini.scene.Geometry;
import mini.scene.shape.Box;
import mini.system.NullRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MaterialTest {

    private Material material;
    private final Geometry geometry = new Geometry("Geometry", new Box(1, 1, 1));
    private final EnumSet<Caps> myCaps = EnumSet.noneOf(Caps.class);
    private final RenderManager renderManager = new RenderManager(new NullRenderer() {
        @Override
        public EnumSet<Caps> getCaps() {
            return MaterialTest.this.myCaps;
        }
    });

    @Test(expected = IllegalArgumentException.class)
    public void testSelectNonExistentTechnique() {
        material("MatDefs/Gui/Gui.minid");
        material.selectTechnique("Doesn't Exist", renderManager);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSelectDefaultTechnique_NoCaps() {
        material("MatDefs/Gui/Gui.minid");
        material.selectTechnique("Default", renderManager);
    }

    @Test
    public void testSelectDefaultTechnique_GLSL100Cap() {
        supportGlsl(100);
        material("MatDefs/Gui/Gui.minid");

        material.selectTechnique("Default", renderManager);

        checkRequiredCaps(Caps.GLSL100);
    }

    @Test
    public void testSelectDefaultTechnique_GLSL150Cap() {
        supportGlsl(150);
        material("MatDefs/Gui/Gui.minid");

        material.selectTechnique("Default", renderManager);

        checkRequiredCaps(Caps.GLSL150);
    }

    @Test
    public void testSelectDefaultTechnique_GLSL120Cap_MultipleLangs() {
        supportGlsl(120);
        material("MatDefs/Misc/Particle.minid");

        material.selectTechnique("Default", renderManager);

        checkRequiredCaps(Caps.GLSL100, Caps.GLSL120);
    }

    @Test
    public void testSelectDefaultTechnique_GLSL100Cap_MultipleLangs() {
        supportGlsl(100);
        material("MatDefs/Misc/Particle.minid");

        material.selectTechnique("Default", renderManager);

        checkRequiredCaps(Caps.GLSL100);
    }

    @Test
    public void testSelectNamedTechnique_GLSL150Cap() {
        supportGlsl(150);
        material("MatDefs/Light/Lighting.minid");

        material.selectTechnique("PostShadow", renderManager);

        checkRequiredCaps(Caps.GLSL150);
    }

    @Test
    public void testSelectNamedTechnique_GLSL100Cap() {
        supportGlsl(100);
        material("MatDefs/Light/Lighting.minid");

        material.selectTechnique("PostShadow", renderManager);

        checkRequiredCaps(Caps.GLSL100);
    }

    private void checkRequiredCaps(Caps... caps) {
        EnumSet<Caps> expected = EnumSet.noneOf(Caps.class);
        expected.addAll(Arrays.asList(caps));

        Technique tech = material.getActiveTechnique();

        assertEquals(expected, tech.getDef().getRequiredCaps());
    }

    private void supportGlsl(int version) {
        switch (version) {
            case 150:
                myCaps.add(Caps.GLSL150);
            case 140:
                myCaps.add(Caps.GLSL140);
            case 130:
                myCaps.add(Caps.GLSL130);
            case 120:
                myCaps.add(Caps.GLSL120);
            case 110:
                myCaps.add(Caps.GLSL110);
            case 100:
                myCaps.add(Caps.GLSL100);
                break;
        }
    }

    private void caps(Caps... caps) {
        myCaps.addAll(Arrays.asList(caps));
    }

    private void material(String path) {
        material = new Material(path);
        geometry.setMaterial(material);
    }
}
