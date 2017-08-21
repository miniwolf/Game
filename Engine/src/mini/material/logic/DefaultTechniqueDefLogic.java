package mini.material.logic;

import mini.light.AmbientLight;
import mini.light.Light;
import mini.light.LightList;
import mini.material.TechniqueDef;
import mini.math.ColorRGBA;
import mini.renderEngine.Caps;
import mini.renderEngine.RenderManager;
import mini.renderEngine.Renderer;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.shaders.DefineList;
import mini.shaders.ShaderProgram;

import java.util.EnumSet;

/**
 * @author miniwolf
 */
public class DefaultTechniqueDefLogic implements TechniqueDefLogic {
    protected final TechniqueDef techniqueDef;

    public DefaultTechniqueDefLogic(TechniqueDef techniqueDef) {
        this.techniqueDef = techniqueDef;
    }

    public static void renderMeshFromGeometry(Renderer renderer, Geometry geom) {
        renderer.renderMesh(geom.getMesh(), 0, 1, null);
    }

    protected static ColorRGBA getAmbientColor(LightList lightList, boolean removeLights,
                                               ColorRGBA ambientLightColor) {
        ambientLightColor.set(0, 0, 0, 1);
        for (int j = 0; j < lightList.size(); j++) {
            Light l = lightList.get(j);
            if (l instanceof AmbientLight) {
                ambientLightColor.addLocal(l.getColor());
                if (removeLights) {
                    lightList.remove(l);
                }
            }
        }
        ambientLightColor.a = 1.0f;
        return ambientLightColor;
    }

    @Override
    public ShaderProgram makeCurrent(RenderManager renderManager, EnumSet<Caps> rendererCaps,
                                     LightList lights, DefineList defines) {
        return techniqueDef.getShader(rendererCaps, defines);
    }

    @Override
    public void render(RenderManager renderManager, ShaderProgram shader, Geometry geometry,
                       LightList lights, int lastTexUnit) {
        Renderer renderer = renderManager.getRenderer();
        renderer.setShader(shader);
        renderMeshFromGeometry(renderer, geometry);
    }
}
