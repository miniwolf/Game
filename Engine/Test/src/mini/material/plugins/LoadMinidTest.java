package mini.material.plugins;

import mini.material.Material;
import mini.renderEngine.Caps;
import mini.renderEngine.RenderManager;
import mini.scene.Geometry;
import mini.scene.shape.Box;
import mini.shaders.ShaderProgram;
import mini.system.NullRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class LoadMinidTest {

    private Material material;
    private final Geometry geometry = new Geometry("Geometry", new Box(1, 1, 1));
    private final EnumSet<Caps> myCaps = EnumSet.noneOf(Caps.class);
    private final RenderManager renderManager = new RenderManager(new NullRenderer() {
        @Override
        public EnumSet<Caps> getCaps() {
            return LoadMinidTest.this.myCaps;
        }
    });

    @Test
    public void testShaderNodesMaterialDefLoading() {
        supportGlsl(100);
        material("matdef.minid");
        material.selectTechnique("Default", renderManager);

        assertEquals(material.getActiveTechnique().getDef().getShaderNodes().size(), 2);
        ShaderProgram s = material.getActiveTechnique().getDef().getShader(myCaps, material
                .getActiveTechnique().getDynamicDefines());
        assertEquals(s.getSources().size(), 2);
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
