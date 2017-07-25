package mini.material;

import mini.shaders.VarType;

/**
 * Describes a material parameter. This is used for both defining a name and type
 * as well as a material parameter value.
 *
 * @author miniwolf
 */
public class MatParam {
    protected final VarType type;
    protected final String name;
    protected Object value;
    private String prefixedName;

    public MatParam(VarType type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.prefixedName = "m_" + name;
    }

    /**
     * Returns the material parameter type.
     *
     * @return the material parameter type.
     */
    public VarType getVarType() {
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

    /**
     * Sets the value of this material parameter.
     * <p>
     * It is assumed the value is of the same {@link MatParam#getVarType() type}
     * as this material parameter.
     *
     * @param value the value of this material parameter.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the name with "m_" prefixed to it.
     *
     * @return the name with "m_" prefixed to it
     */
    public String getPrefixedName() {
        return prefixedName;
    }
}
