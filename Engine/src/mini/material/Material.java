package mini.material;

import mini.asset.AssetKey;
import mini.asset.MaterialKey;
import mini.light.LightList;
import mini.material.logic.DefaultTechniqueDefLogic;
import mini.material.plugins.MiniLoader;
import mini.math.ColorRGBA;
import mini.post.SceneProcessor;
import mini.renderEngine.Caps;
import mini.renderEngine.RenderManager;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.shaders.ShaderProgram;
import mini.shaders.Uniform;
import mini.shaders.VarType;
import mini.textures.Image;
import mini.textures.Texture;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * <code>Material</code> describes the rendering style for a given
 * {@link mini.scene.Entity}.
 * <p>A material is essentially a list of {@link MatParam parameters},
 * those parameters map to uniforms which are defined in a shader.
 * Setting the parameters can modify the behavior of a
 * shader.
 * <p/>
 *
 * @author miniwolf
 */
public class Material {
    private MaterialDef def;
    private Technique technique;
    private String name;
    private RenderState additionalState = null;
    private RenderState mergedRenderState = new RenderState();
    private final Map<String, MatParam> paramValues = new HashMap<>();
    private Map<String, Technique> techniques = new HashMap<>();
    private Texture diffuseTexture;
    private Texture extraInfoMap;
    private boolean transparent;
    private int sortingId = -1;
    private AssetKey key;

    public Material(String defName) {
        this();
        try {
            def = (MaterialDef) MiniLoader.load(new MaterialKey(defName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Material(MaterialDef def) {
        if (def == null) {
            throw new NullPointerException("Material definition cannot be null");
        }
        this.def = def;

        // Load default values from definition (if any)
        def.getMaterialParams().parallelStream().filter(param -> param.getValue() != null)
           .forEach(param -> setParam(param.getName(), param.getVarType(), param.getValue()));
    }

    public Material() {
    }

    /**
     * Returns the asset key name of the asset from which this material was loaded.
     * <p>
     * <p>This value will be <code>null</code> unless this material was loaded
     * from a .j3m file.
     *
     * @return Asset key name of the j3m file
     */
    public String getAssetName() {
        return key != null ? key.getFilename().getName() : null;
    }

    /**
     * @return the name of the material (not the same as the asset name), the returned value can be null
     */
    public String getName() {
        return name;
    }

    /**
     * This method sets the name of the material.
     * The name is not the same as the asset name.
     * It can be null and there is no guarantee of its uniqueness.
     *
     * @param name the name of the material
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Pass a boolean to the material shader.
     *
     * @param name  the name of the boolean defined in the material definition (j3md)
     * @param value the boolean value
     */
    public void setBoolean(String name, boolean value) {
        setParam(name, VarType.Boolean, value);
    }

    /**
     * Pass a float to the material shader.  This version avoids auto-boxing
     * if the value is already a Float.
     *
     * @param name  the name of the float defined in the material definition (j3md)
     * @param value the float value
     */
    public void setFloat(String name, float value) {
        setParam(name, VarType.Float, value);
    }

    /**
     * Pass a Color to the material shader.
     *
     * @param name  the name of the color defined in the material definition (j3md)
     * @param value the ColorRGBA value
     */
    public void setColor(String name, ColorRGBA value) {
        setParam(name, VarType.Vector4f, value);
    }

    /**
     * Check if setting the parameter given the type and name is allowed.
     * @param type The type that the "set" function is designed to set
     * @param name The name of the parameter
     */
    private void checkSetParam(VarType type, String name) {
        MatParam paramDef = def.getMaterialParam(name);
        if (paramDef == null) {
            throw new IllegalArgumentException("Material parameter is not defined: " + name);
        }
        if (type != null && paramDef.getVarType() != type) {
            System.err.println("Material parameter being set: " + name + " with type " + type.name()
                               + " doesn''t match definition types " + paramDef.getVarType());
        }
    }

    /**
     * Pass a parameter to the material shader.
     *
     * @param name  the name of the parameter defined in the material definition (j3md)
     * @param type  the type of the parameter {@link VarType}
     * @param value the value of the parameter
     */
    public void setParam(String name, VarType type, Object value) {
        checkSetParam(type, name);

        if (type.isTextureType()) {
            setTextureParam(name, type, (Texture)value);
        } else {
            MatParam val = getParam(name);
            if (val == null) {
                MatParam paramDef = def.getMaterialParam(name);
                paramValues.put(name, new MatParam(type, name, value));
            } else {
                val.setValue(value);
            }

            if (technique != null) {
                technique.notifyParamChanged(name, type, value);
            }
        }
    }

    /**
     * Clear a parameter from this material. The parameter must exist
     *
     * @param name the name of the parameter to clear
     */
    public void clearParam(String name) {
        MatParam matParam = getParam(name);
        if (matParam == null) {
            return;
        }

        paramValues.remove(name);
    }

    /**
     * Set a texture parameter.
     *
     * @param name  The name of the parameter
     * @param type  The variable type {@link VarType}
     * @param value The texture value of the parameter.
     * @throws IllegalArgumentException is value is null
     */
    public void setTextureParam(String name, VarType type, Texture value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        MatParamTexture val = getTextureParam(name);
        if (val == null) {
            paramValues.put(name, new MatParamTexture(type, name, value, null));
        } else {
            val.setTextureValue(value);
        }
    }

    /**
     * Pass a texture to the material shader.
     *
     * @param name  the name of the texture defined in the material definition
     *              (j3md) (for example Texture for Lighting.j3md)
     * @param value the Texture object previously loaded by the asset manager
     */
    public void setTexture(String name, Texture value) {
        if (value == null) {
            // clear it
            clearParam(name);
            return;
        }

        VarType paramType;
        switch (value.getType()) {
            case TwoDimensional:
                paramType = VarType.Texture2D;
                break;
            /*case TwoDimensionalArray:
                paramType = VarType.TextureArray;
                break;
            case ThreeDimensional:
                paramType = VarType.Texture3D;
                break;
            case CubeMap:
                paramType = VarType.TextureCubeMap;
                break;*/
            default:
                throw new UnsupportedOperationException("Unknown texture type: " + value.getType());
        }

        setTextureParam(name, paramType, value);
    }

    /**
     * Select the technique to use for rendering this material.
     * <p>
     * Any candidate technique for selection (either default or named)
     * must be verified to be compatible with the system, for that, the
     * <code>renderManager</code> is queried for capabilities.
     *
     * @param name          The name of the technique to select, pass
     *                      {@link TechniqueDef#DEFAULT_TECHNIQUE_NAME} to select one of the default
     *                      techniques.
     * @param renderManager The {@link RenderManager render manager}
     *                      to query for capabilities.
     * @throws IllegalArgumentException      If no technique exists with the given
     *                                       name.
     * @throws UnsupportedOperationException If no candidate technique supports
     *                                       the system capabilities.
     */
    public void selectTechnique(String name, final RenderManager renderManager) {
        // check if already created
        Technique tech = techniques.get(name);
        // When choosing technique, we choose one that
        // supports all the caps.
        if (tech == null) {
            EnumSet<Caps> rendererCaps = renderManager.getRenderer().getCaps();
            List<TechniqueDef> techDefs = def.getTechniqueDefs(name);
            if (techDefs == null || techDefs.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("The requested technique %s is not available on material %s",
                                      name, def.getName()));
            }

            TechniqueDef lastTech = null;
            float weight = 0;
            for (TechniqueDef techDef : techDefs) {
                if (rendererCaps.containsAll(techDef.getRequiredCaps())) {
                    float techWeight = techDef.getWeight() + (
                            techDef.getLightMode() == renderManager.getPreferredLightMode() ? 10f :
                            0);
                    if (techWeight > weight) {
                        tech = new Technique(this, techDef);
                        techniques.put(name, tech);
                        weight = techWeight;
                    }
                }
                lastTech = techDef;
            }
            if (tech == null) {
                throw new UnsupportedOperationException(
                        String.format("No technique '%s' on material "
                                      + "'%s' is supported by the video hardware. "
                                      + "The capabilities %s are required.",
                                      name, def.getName(), lastTech.getRequiredCaps()));
            }
        } else if (technique == tech) {
            // attempting to switch to an already
            // active technique.
            return;
        }

        technique = tech;
        tech.notifyTechniqueSwitched();

        // shader was changed
        sortingId = -1;
    }

//    public void delete() {
//        diffuseTexture.delete();
//        if (extraInfoMap != null) {
//            extraInfoMap.delete();
//        }
//    }

    /**
     * Acquire the additional {@link RenderState render state} to apply
     * for this material.
     * <p>
     * <p>The first call to this method will create an additional render
     * state which can be modified by the user to apply any render
     * states in addition to the ones used by the renderer. Only render
     * states which are modified in the additional render state will be applied.
     *
     * @return The additional render state.
     */
    public RenderState getAdditionalRenderState() {
        if (additionalState == null) {
            additionalState = RenderState.ADDITIONAL.clone();
        }
        return additionalState;
    }

    /**
     * Get the material definition (j3md file info) that <code>this</code>
     * material is implementing.
     *
     * @return the material definition this material implements.
     */
    public MaterialDef getMaterialDef() {
        return def;
    }

    /**
     * Returns the parameter set on this material with the given name,
     * returns <code>null</code> if the parameter is not set.
     *
     * @param name The parameter name to look up.
     * @return The MatParam if set, or null if not set.
     */
    public MatParam getParam(String name) {
        return paramValues.get(name);
    }

    /**
     * Returns the texture parameter set on this material with the given name,
     * returns <code>null</code> if the parameter is not set.
     *
     * @param name The parameter name to look up.
     * @return The MatParamTexture if set, or null if not set.
     */
    public MatParamTexture getTextureParam(String name) {
        MatParam param = paramValues.get(name);
        if (param instanceof MatParamTexture) {
            return (MatParamTexture) param;
        }
        return null;
    }

    /**
     * Returns the ListMap of all parameters set on this material.
     *
     * @return a ListMap of all parameters set on this material.
     *
     * @see #setParam(java.lang.String, com.jme3.shader.VarType, java.lang.Object)
     */
    public Map<String, MatParam> getParamsMap() {
        return paramValues;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public boolean hasTransparency() {
        return transparent;
    }

    public boolean hasExtraMap() {
        return extraInfoMap != null;
    }

    public Texture getDiffuseTexture() {
        return (Texture) getParam("diffuseMap").getValue();
    }

    public Texture getExtraInfoMap() {
        return extraInfoMap;
    }

    /**
     * Called by {@link RenderManager} to render the geometry by
     * using this material.
     * <p>
     * The material is rendered as follows:
     * <ul>
     * <li>Determine which technique to use to render the material -
     * either what the user selected via
     * {@link #selectTechnique(java.lang.String, RenderManager)
     * Material.selectTechnique()},
     * or the first default technique that the renderer supports
     * (based on the technique's {@link TechniqueDef#getRequiredCaps() requested rendering capabilities})<ul>
     * <li>If the technique has been changed since the last frame, then it is notified via
     * {@link Technique#makeCurrent(AssetManager, boolean, java.util.EnumSet)
     * Technique.makeCurrent()}.
     * If the technique wants to use a shader to render the model, it should load it at this part -
     * the shader should have all the proper defines as declared in the technique definition,
     * including those that are bound to material parameters.
     * The technique can re-use the shader from the last frame if
     * no changes to the defines occurred.</li></ul>
     * <li>Set the {@link RenderState} to use for rendering. The render states are
     * applied in this order (later RenderStates override earlier RenderStates):<ol>
     * <li>{@link TechniqueDef#getRenderState() Technique Definition's RenderState}
     * - i.e. specific renderstate that is required for the shader.</li>
     * <li>{@link #getAdditionalRenderState() Material Instance Additional RenderState}
     * - i.e. ad-hoc renderstate set per model</li>
     * <li>{@link RenderManager#getForcedRenderState() RenderManager's Forced RenderState}
     * - i.e. renderstate requested by a {@link SceneProcessor} or
     * post-processing filter.</li></ol>
     * <li>If the technique {@link TechniqueDef#isUsingShaders() uses a shader}, then the uniforms of the shader must be updated.<ul>
     * <li>Uniforms bound to material parameters are updated based on the current material parameter values.</li>
     * <li>Uniforms bound to world parameters are updated from the RenderManager.
     * Internally {@link UniformBindingManager} is used for this task.</li>
     * <li>Uniforms bound to textures will cause the texture to be uploaded as necessary.
     * The uniform is set to the texture unit where the texture is bound.</li></ul>
     * <li>If the technique uses a shader, the model is then rendered according
     * to the lighting mode specified on the technique definition.<ul>
     * <li>{@link LightMode#SinglePass single pass light mode} fills the shader's light uniform arrays
     * with the first 4 lights and renders the model once.</li>
     * <li>{@link LightMode#MultiPass multi pass light mode} light mode renders the model multiple times,
     * for the first light it is rendered opaque, on subsequent lights it is
     * rendered with {@link BlendMode#AlphaAdditive alpha-additive} blending and depth writing disabled.</li>
     * </ul>
     * <li>For techniques that do not use shaders,
     * fixed function OpenGL is used to render the model (see {@link GLRenderer} interface):<ul>
     * <li>OpenGL state ({@link FixedFuncBinding}) that is bound to material parameters is updated. </li>
     * <li>The texture set on the material is uploaded and bound.
     * Currently only 1 texture is supported for fixed function techniques.</li>
     * <li>If the technique uses lighting, then OpenGL lighting state is updated
     * based on the light list on the geometry, otherwise OpenGL lighting is disabled.</li>
     * <li>The mesh is uploaded and rendered.</li>
     * </ul>
     * </ul>
     *
     * @param geometry      The geometry to render
     * @param lights        Presorted and filtered light list to use for rendering
     * @param renderManager The render manager requesting the rendering
     */
    public void render(Geometry geometry, LightList lights, RenderManager renderManager) {
        if (technique == null) {
            selectTechnique(TechniqueDef.DEFAULT_TECHNIQUE_NAME, renderManager);
        }

        TechniqueDef techniqueDef = technique.getDef();
        GLRenderer renderer = renderManager.getRenderer();
        EnumSet<Caps> rendererCaps = renderer.getCaps();

        // Apply render state
        updateRenderState(renderer, techniqueDef);

        // Get world overrides
        List<MatParamOverride> overrides = geometry.getWorldMatParamOverrides();

        // Select shader to use
        ShaderProgram shader = technique
                .makeCurrent(renderManager, overrides, lights, rendererCaps);

        // Begin tracking which uniforms were changed by material.
        clearUniformsSetByCurrent(shader);

        // Set uniform bindings
        renderManager.updateUniformBindings(shader);

        // Set material parameters
        int unit = updateShaderMaterialParameters(renderer, shader);

        // Clear any uniforms not changed by material.
        resetUniformsNotSetByCurrent(shader);

        // Delegate rendering to the technique
        technique.render(renderManager, shader, geometry, lights, unit);
    }

    /**
     * Called by {@link RenderManager} to render the geometry by
     * using this material.
     * <p>
     * Note that this version of the render method
     * does not perform light filtering.
     *
     * @param geom The geometry to render
     * @param rm   The render manager requesting the rendering
     */
    public void render(Geometry geom, RenderManager rm) {
        render(geom, geom.getWorldLightList(), rm);
    }

    private void updateRenderState(GLRenderer renderer, TechniqueDef techniqueDef) {
        if (techniqueDef.getRenderState() != null) {
            renderer.applyRenderState(
                    techniqueDef.getRenderState().copyMergedTo(additionalState, mergedRenderState));
        } else {
            renderer.applyRenderState(
                    RenderState.DEFAULT.copyMergedTo(additionalState, mergedRenderState));
        }
    }

    private void clearUniformsSetByCurrent(ShaderProgram shader) {
        Map<String, Uniform> uniforms = shader.getUniformMap();
        for (Uniform uniform : uniforms.values()) {
            uniform.clearSetByCurrentMaterial();
        }
    }

    private void resetUniformsNotSetByCurrent(ShaderProgram shader) {
        Map<String, Uniform> uniforms = shader.getUniformMap();
        for (Uniform u : uniforms.values()) {
            if (!u.isSetByCurrentMaterial()) {
                if (u.getName().charAt(0) != 'g') {
                    // Don't reset world globals!
                    // The benefits gained from this are very minimal
                    // and cause lots of matrix -> FloatBuffer conversions.
                    u.clearValue();
                }
            }
        }
    }

    private int updateShaderMaterialParameters(GLRenderer renderer, ShaderProgram shader) {
        int unit = 0;
        for (MatParam param : paramValues.values()) {
            VarType type = param.getVarType();
            Uniform uniform = shader.getUniform(param.getPrefixedName());

            if (uniform.isSetByCurrentMaterial()) {
                continue;
            }

            if (type.isTextureType()) {
                renderer.setTexture(unit, (Texture) param.getValue());
                uniform.setValue(VarType.Int, unit);
                unit++;
            } else {
                uniform.setValue(type, param.getValue());
            }
        }

        //TODO HACKY HACK remove this when texture unit is handled by the uniform.
        return unit;
    }

    public void setKey(AssetKey key) {
        this.key = key;
    }

    public AssetKey getKey() {
        return key;
    }

    /**
     * Returns the sorting ID or sorting index for this material.
     * <p>
     * <p>The sorting ID is used internally by the system to sort rendering
     * of geometries. It sorted to reduce shader switches, if the shaders
     * are equal, then it is sorted by textures.
     *
     * @return The sorting ID used for sorting geometries for rendering.
     */
    public int getSortId() {
        if (sortingId == -1 && technique != null) {
            sortingId = technique.getSortId() << 16;
            int texturesSortId = 17;
            for (MatParam param : paramValues.values()) {
                if (!param.getVarType().isTextureType()) {
                    continue;
                }
                Texture texture = (Texture) param.getValue();
                if (texture == null) {
                    continue;
                }
                Image image = texture.getImage();
                if (image == null) {
                    continue;
                }
                int textureId = image.getId();
                if (textureId == -1) {
                    textureId = 0;
                }
                texturesSortId = texturesSortId * 23 + textureId;
            }
            sortingId |= texturesSortId & 0xFFFF;
        }
        return sortingId;
    }
}
