package mini.material.logic;

import mini.light.LightList;
import mini.material.TechniqueDef;
import mini.renderEngine.RenderManager;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.shaders.DefineList;
import mini.shaders.ShaderProgram;

/**
 * @author miniwolf
 */
public class DefaultTechniqueDefLogic implements TechniqueDefLogic {
    protected final TechniqueDef techniqueDef;

    public DefaultTechniqueDefLogic(TechniqueDef techniqueDef) {
        this.techniqueDef = techniqueDef;
    }

    private static void renderMeshFromGeometry(GLRenderer renderer, Geometry geom) {
        renderer.renderMesh(geom.getMesh());
    }

    @Override
    public ShaderProgram makeCurrent(RenderManager renderManager, LightList lights, DefineList defines) {
        return techniqueDef.getShader(defines);
    }

    @Override
    public void render(RenderManager renderManager, ShaderProgram shader, Geometry geometry,
                       LightList lights, int lastTexUnit) {
        GLRenderer renderer = renderManager.getRenderer();
        renderer.setShader(shader);
        renderMeshFromGeometry(renderer, geometry);
    }
}
