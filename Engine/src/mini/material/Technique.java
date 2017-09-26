package mini.material;

import mini.light.LightList;
import mini.material.logic.TechniqueDefLogic;
import mini.renderEngine.Caps;
import mini.renderEngine.RenderManager;
import mini.scene.Geometry;
import mini.shaders.DefineList;
import mini.shaders.ShaderProgram;
import mini.shaders.VarType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by miniwolf on 30-04-2017.
 */
public class Technique {
    private final TechniqueDef def;
    private final Material owner;
    private final DefineList paramDefines;
    private final DefineList dynamicDefines;

    /**
     * Creates a new technique instance that implements the given
     * technique definition.
     *
     * @param owner The material that will own this technique
     * @param def   The technique definition being implemented.
     */
    public Technique(Material owner, TechniqueDef def) {
        this.owner = owner;
        this.def = def;
        this.paramDefines = def.createDefineList();
        this.dynamicDefines = def.createDefineList();
    }

    /**
     * Called by the material to tell the technique a parameter was modified.
     * Specify <code>null</code> for value if the param is to be cleared.
     */
    final void notifyParamChanged(String paramName, VarType type, Object value) {
        Integer defineId = def.getShaderParamDefineId(paramName);

        if (defineId == null) {
            return;
        }

        paramDefines.set(defineId, type, value);
    }

    /**
     * Called by the material to tell the technique that it has been made
     * current.
     * The technique updates dynamic defines based on the
     * currently set material parameters.
     */
    final void notifyTechniqueSwitched() {
        Map<String, MatParam> paramMap = owner.getParamsMap();
        paramDefines.clear();
        for (MatParam param : paramMap.values()) {
            notifyParamChanged(param.getName(), param.getVarType(), param.getValue());
        }
    }

    private void applyOverrides(DefineList defineList, List<MatParamOverride> overrides) {
        for (MatParamOverride override : overrides) {
            if (!override.isEnabled()) {
                continue;
            }
            Integer defineId = def.getShaderParamDefineId(override.name);
            if (defineId != null) {
                if (def.getDefineIdType(defineId) == override.type) {
                    defineList.set(defineId, override.type, override.value);
                }
            }
        }
    }

    /**
     * Called by the material to determine which shader to use for rendering.
     * <p>
     * The {@link TechniqueDefLogic} is used to determine the shader to use
     * based on the {@link LightMode}.
     *
     * @param renderManager The render manager for which the shader is to be selected.
     * @param rendererCaps  The renderer capabilities which the shader should support.
     * @return A compatible shader.
     */
    ShaderProgram makeCurrent(RenderManager renderManager, List<MatParamOverride> worldOverrides,
                              List<MatParamOverride> forcedOverrides, LightList lights,
                              Set<Caps> rendererCaps) {
        TechniqueDefLogic logic = def.getLogic();

        dynamicDefines.clear();
        dynamicDefines.setAll(paramDefines);

        if (worldOverrides != null) {
            applyOverrides(dynamicDefines, worldOverrides);
        }
        if (forcedOverrides != null) {
            applyOverrides(dynamicDefines, forcedOverrides);
        }

        return logic.makeCurrent(renderManager, rendererCaps, lights, dynamicDefines);
    }

    /**
     * Render the technique according to its {@link TechniqueDefLogic}.
     *
     * @param renderManager The render manager to perform the rendering against.
     * @param shader        The shader
     * @param geometry      The geometry to render
     */
    void render(RenderManager renderManager, ShaderProgram shader, Geometry geometry,
                LightList lights, int lastTexUnit) {
        TechniqueDefLogic logic = def.getLogic();
        logic.render(renderManager, shader, geometry, lights, lastTexUnit);
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

    /**
     * Get the {@link DefineList} for dynamic defines.
     *
     * Dynamic defines are used to implement material parameter -> define
     * bindings as well as {@link TechniqueDefLogic} specific functionality.
     *
     * @return all dynamic defines.
     */
    public DefineList getDynamicDefines() {
        return dynamicDefines;
    }

    /**
     * Compute the sort ID. Similar to {@link Object#hashCode()} but used
     * for sorting geometries for rendering.
     *
     * @return the sort ID for this technique instance.
     */
    public int getSortId() {
        int hash = 17;
        hash = hash * 23 + def.getSortId();
        hash = hash * 23 + paramDefines.hashCode();
        return hash;
    }
}
