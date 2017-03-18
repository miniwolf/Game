package mini.material;

import mini.shaders.VarType;

/**
 * Describes a material parameter. This is used for both defining a name and type
 * as well as a material parameter value.
 *
 * @author miniwolf
 */
public class MatParam {
    private final VarType type;
    private final String name;
    private final Object value;

    public MatParam(VarType type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    /**
     * @return the material parameter type.
     */
    public VarType getType() {
        return type;
    }

    /**
     * @return the name of the material parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of this material parameter.
     * <p>
     * Material parameters that are used for material definitions
     * will not have a value, unless there's a default value declared
     * in the definition.
     *
     * @return the value of this material parameter.
     */
    public Object getValue() {
        return value;
    }
}
