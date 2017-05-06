package mini.material.logic;

import mini.material.TechniqueDef;
import mini.renderEngine.RenderManager;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.shaders.ShaderProgram;

/**
 * @author miniwolf
 */
public class DefaultTechniqueDefLogic implements TechniqueDefLogic {
    private final TechniqueDef techniqueDef;

    public DefaultTechniqueDefLogic(TechniqueDef techniqueDef) {
        this.techniqueDef = techniqueDef;
    }

    private static void renderMeshFromGeometry(GLRenderer renderer, Geometry geom) {
        renderer.renderMesh(geom.getMesh());
    }

    @Override
    public void render(RenderManager renderManager, ShaderProgram shader, Geometry geometry,
                       int lastTexUnit) {
        GLRenderer renderer = renderManager.getRenderer();
        renderer.setShader(shader);
        renderMeshFromGeometry(renderer, geometry);
    }
}
