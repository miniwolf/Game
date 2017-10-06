package mini.shaders;

import java.util.ArrayList;
import java.util.List;

/**
 * A ShaderNode is the unit brick part of a shader program. A shader can be
 * describe with several shader nodes that are plugged together through inputs
 * and outputs.
 *
 * A ShaderNode is based on a definition that has a shader code, inputs and
 * output variables. This node can be activated based on a condition, and has
 * input and output mapping.
 *
 * This class is not intended to be used by the engine users directly. It's the
 * structure for loading shader nodes from a minid material definition file
 */
public class ShaderNode implements Cloneable {
    private String name;
    private ShaderNodeDefinition definition;
    private String condition;
    private List<VariableMapping> inputMapping = new ArrayList<>();
    private List<VariableMapping> outputMapping = new ArrayList<>();

    /**
     * creates a ShaderNode
     *
     * @param name the name
     * @param definition the ShaderNodeDefinition
     * @param condition the condition to activate this node
     */
    public ShaderNode(String name, ShaderNodeDefinition definition, String condition) {
        this.name = name;
        this.definition = definition;
        this.condition = condition;
    }

    /**
     * creates a ShaderNode
     */
    public ShaderNode() {
    }

    /**
     *
     * @return the name of the node
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of th node
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * returns the definition
     *
     * @return the ShaderNodeDefinition
     */
    public ShaderNodeDefinition getDefinition() {
        return definition;
    }

    /**
     * sets the definition
     *
     * @param definition the ShaderNodeDefinition
     */
    public void setDefinition(ShaderNodeDefinition definition) {
        this.definition = definition;
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
     * return a list of VariableMapping representing the input mappings of this
     * node
     *
     * @return the input mappings
     */
    public List<VariableMapping> getInputMapping() {
        return inputMapping;
    }

    /**
     * sets the input mappings
     *
     * @param inputMapping the input mappings
     */
    public void setInputMapping(List<VariableMapping> inputMapping) {
        this.inputMapping = inputMapping;
    }

    /**
     * return a list of VariableMapping representing the output mappings of this
     * node
     *
     * @return the output mappings
     */
    public List<VariableMapping> getOutputMapping() {
        return outputMapping;
    }

    /**
     * sets the output mappings
     *
     * @param outputMapping the output mappings
     */
    public void setOutputMapping(List<VariableMapping> outputMapping) {
        this.outputMapping = outputMapping;
    }

    /**
     * convenience tostring
     *
     * @return a string
     */
    @Override
    public String toString() {
        return "\nShaderNode{" + "\nname=" + name + ", \ndefinition=" + definition.getName() + ", \ncondition=" + condition + ", \ninputMapping=" + inputMapping + ", \noutputMapping=" + outputMapping + '}';
    }

    @Override
    public ShaderNode clone() throws CloneNotSupportedException {
        ShaderNode clone = (ShaderNode) super.clone();

        //No need to clone the definition.
        clone.definition = definition;

        clone.inputMapping = new ArrayList<>();
        for (VariableMapping variableMapping : inputMapping) {
            clone.inputMapping.add(variableMapping.clone());
        }

        clone.outputMapping = new ArrayList<>();
        for (VariableMapping variableMapping : outputMapping) {
            clone.outputMapping.add(variableMapping.clone());
        }

        return clone;
    }
}
