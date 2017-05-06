package mini.material;

import mini.entityRenderers.EntityShader;
import mini.material.logic.TechniqueDefLogic;
import mini.renderEngine.RenderManager;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.shaders.ShaderProgram;

/**
 * Created by miniwolf on 30-04-2017.
 */
public class Technique {
    private final TechniqueDef def;
    private final Material owner;

    /**
     * Creates a new technique instance that implements the given
     * technique definition.
     *
     * @param owner The material that will own this technique
     * @param def The technique definition being implemented.
     */
    public Technique(Material owner, TechniqueDef def) {
        this.owner = owner;
        this.def = def;
    }

    /**
     * Render the technique according to its {@link TechniqueDefLogic}.
     *
     * @param renderManager The render manager to perform the rendering against.
     * @param shader The shader
     * @param geometry The geometry to render
     */
    void render(RenderManager renderManager, ShaderProgram shader, Geometry geometry, int lastTexUnit) {
        TechniqueDefLogic logic = def.getLogic();
        logic.render(renderManager, shader, geometry, lastTexUnit);
    }

    /**
     * Returns the technique definition that is implemented by this technique
     * instance.
     *
     * @return the technique definition that is implemented by this technique
     * instance.
     */
    public TechniqueDef getDef() {
        return def;
    }
}
