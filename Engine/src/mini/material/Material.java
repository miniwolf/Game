package mini.material;

import mini.math.ColorRGBA;
import mini.shaders.VarType;

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
    private final String name;
    private final Map<String, MatParam> paramValues = new HashMap<>();

    public Material(String name) {
        this.name = name;
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

    private void setParam(String name, VarType type, Object value) {
        paramValues.put(name, new MatParam(type, name, value));
    }
}
