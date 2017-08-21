package mini.shaders;

/**
 * represents a mapping between 2 ShaderNodeVariables
 */
public class VariableMapping implements Cloneable {

    private ShaderNodeVariable leftVariable;
    private ShaderNodeVariable rightVariable;
    private String condition;
    private String leftSwizzling = "";
    private String rightSwizzling = "";

    /**
     * creates a VariableMapping
     */
    public VariableMapping() {
    }

    /**
     * creates a VariableMapping
     *
     * @param leftVariable the left hand side variable of the expression
     * @param leftSwizzling the swizzling of the left variable
     * @param rightVariable the right hand side variable of the expression
     * @param rightSwizzling the swizzling of the right variable
     * @param condition the condition for this mapping
     */
    public VariableMapping(ShaderNodeVariable leftVariable, String leftSwizzling, ShaderNodeVariable rightVariable, String rightSwizzling, String condition) {
        this.leftVariable = leftVariable;
        this.rightVariable = rightVariable;
        this.condition = condition;
        this.leftSwizzling = leftSwizzling;
        this.rightSwizzling = rightSwizzling;
    }

    /**
     *
     * @return the left variable
     */
    public ShaderNodeVariable getLeftVariable() {
        return leftVariable;
    }

    /**
     * sets the left variable
     *
     * @param leftVariable the left variable
     */
    public void setLeftVariable(ShaderNodeVariable leftVariable) {
        this.leftVariable = leftVariable;
    }

    /**
     *
     * @return the right variable
     */
    public ShaderNodeVariable getRightVariable() {
        return rightVariable;
    }

    /**
     * sets the right variable
     *
     * @param rightVariable the right variable
     */
    public void setRightVariable(ShaderNodeVariable rightVariable) {
        this.rightVariable = rightVariable;
    }

    /**
     *
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * sets the condition
     *
     * @param condition the condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     *
     * @return the left swizzle
     */
    public String getLeftSwizzling() {
        return leftSwizzling;
    }

    /**
     * sets the left swizzle
     *
     * @param leftSwizzling the left swizzle
     */
    public void setLeftSwizzling(String leftSwizzling) {
        this.leftSwizzling = leftSwizzling;
    }

    /**
     *
     * @return the right swizzle
     */
    public String getRightSwizzling() {
        return rightSwizzling;
    }

    /**
     * sets the right swizzle
     *
     * @param rightSwizzling the right swizzle
     */
    public void setRightSwizzling(String rightSwizzling) {
        this.rightSwizzling = rightSwizzling;
    }

    @Override
    public String toString() {
        return "\n{" + leftVariable.toString() + (leftSwizzling.length() > 0 ? ("." + leftSwizzling) : "") + " = " + rightVariable.getType() + " " + rightVariable.getNameSpace() + "." + rightVariable.getName() + (rightSwizzling.length() > 0 ? ("." + rightSwizzling) : "") + " : " + condition + "}";
    }

    @Override
    protected VariableMapping clone() throws CloneNotSupportedException {
        VariableMapping clone = (VariableMapping) super.clone();

        clone.leftVariable = leftVariable.clone();
        clone.rightVariable = rightVariable.clone();

        return clone;
    }
}
