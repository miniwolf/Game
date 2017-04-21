package mini.material;

import mini.math.ColorRGBA;
import mini.renderEngine.opengl.GLRenderer;
import mini.shaders.ShaderProgram;
import mini.shaders.Uniform;
import mini.shaders.VarType;
import mini.textures.Texture;

import java.util.HashMap;
import java.util.Map;

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
    private String name;
    private final Map<String, MatParam> paramValues = new HashMap<>();
    private Texture diffuseTexture;
    private Texture extraInfoMap;
    private boolean transparent;

    public Material(String name) {
        this.name = name;
    }

    public Material() {
    }

    /**
     * Pass a float to the material shader.  This version avoids auto-boxing
     * if the value is already a Float.
     *
     * @param name the name of the float defined in the material definition (j3md)
     * @param value the float value
     */
    public void setFloat(String name, float value) {
        setParam(name, VarType.Float, value);
    }

    /**
     * Pass a Color to the material shader.
     *
     * @param name the name of the color defined in the material definition (j3md)
     * @param value the ColorRGBA value
     */
    public void setColor(String name, ColorRGBA value) {
        setParam(name, VarType.Vector4, value);
    }

    /**
     * Pass a parameter to the material shader.
     *
     * @param name the name of the parameter defined in the material definition (j3md)
     * @param type the type of the parameter {@link VarType}
     * @param value the value of the parameter
     */
    public void setParam(String name, VarType type, Object value) {
        if (type.isTextureType()) {
            setTextureParam(name, type, (Texture)value);
        } else {
            MatParam val = getParam(name);
            if (val == null) {
                paramValues.put(name, new MatParam(type, name, value));
            } else {
                val.setValue(value);
            }
        }
    }

    /**
     * Set a texture parameter.
     *
     * @param name The name of the parameter
     * @param type The variable type {@link VarType}
     * @param value The texture value of the parameter.
     *
     * @throws IllegalArgumentException is value is null
     */
    public void setTextureParam(String name, VarType type, Texture value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        MatParamTexture val = getTextureParam(name);
        if (val == null) {
            paramValues.put(name, new MatParamTexture(type, name, value));
        } else {
            val.setTextureValue(value);
        }
    }

    public void delete() {
        diffuseTexture.delete();
        if (extraInfoMap != null) {
            extraInfoMap.delete();
        }
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

    public void render(GLRenderer renderer) {
        // Set material parameters
        int unit = updateShaderMaterialParameters(renderer);
    }

    private int updateShaderMaterialParameters(GLRenderer renderer) {
        int unit = 0;
        ShaderProgram shader = renderer.getShader();

        for (MatParam param : paramValues.values()) {
            VarType type = param.getVarType();
            Uniform uniform = shader.getUniform(param.getPrefixedName());

            if (uniform.isSetByCurrentMaterial()) {
                continue;
            }

            if (type.isTextureType()) {
                ((Texture) param.getValue()).bindToUnit(unit);
                uniform.setValue(VarType.Int, unit);
                unit++;
            } else {
                uniform.setValue(type, param.getValue());
            }
        }

        //TODO HACKY HACK remove this when texture unit is handled by the uniform.
        return unit;
    }
}
