package mini.shaders;

import java.util.ArrayList;
import java.util.List;

/**
 * Shader node definition structure meant for holding loaded data from a
 * material definition j3md file
 */
public class ShaderNodeDefinition {

    private String name;
    private Shader.ShaderType type;
    private List<String> shadersLanguage = new ArrayList<>();
    private List<String> shadersPath = new ArrayList<>();
    private String documentation;
    private List<ShaderNodeVariable> inputs = new ArrayList<>();
    private List<ShaderNodeVariable> outputs = new ArrayList<>();
    private String path = null;
    private boolean noOutput = false;

    /**
     * creates a ShaderNodeDefinition
     *
     * @param name           the name of the definition
     * @param type           the type of the shader
     * @param shaderPath     the path of the shader
     * @param shaderLanguage the shader language (minimum required for this definition)
     */
    public ShaderNodeDefinition(String name, Shader.ShaderType type, String shaderPath,
                                String shaderLanguage) {
        this.name = name;
        this.type = type;
        shadersLanguage.add(shaderLanguage);
        shadersPath.add(shaderPath);
    }

    /**
     * creates a ShaderNodeDefinition
     */
    public ShaderNodeDefinition() {
    }

    /**
     * returns the name of the definition
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the definition
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type of shader the definition applies to
     */
    public Shader.ShaderType getType() {
        return type;
    }

    /**
     * sets the type of shader this definition applies to
     *
     * @param type the type
     */
    public void setType(Shader.ShaderType type) {
        this.type = type;
    }

    /**
     * @return the documentation for this definition
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * sets the documentation
     *
     * @param documentation the documentation
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * @return the input variables of this definition
     */
    public List<ShaderNodeVariable> getInputs() {
        return inputs;
    }

    /**
     * sets the input variables of this definition
     *
     * @param inputs the inputs
     */
    public void setInputs(List<ShaderNodeVariable> inputs) {
        this.inputs = inputs;
    }

    /**
     * @return the output variables of this definition
     */
    public List<ShaderNodeVariable> getOutputs() {
        return outputs;
    }

    /**
     * sets the output variables of this definition
     *
     * @param outputs the output
     */
    public void setOutputs(List<ShaderNodeVariable> outputs) {
        this.outputs = outputs;
    }

    /**
     * retrun the path of this definition
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * sets the path of this definition
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getShadersLanguage() {
        return shadersLanguage;
    }

    public List<String> getShadersPath() {
        return shadersPath;
    }

    public boolean isNoOutput() {
        return noOutput;
    }

    public void setNoOutput(boolean noOutput) {
        this.noOutput = noOutput;
    }

    /**
     * convenience tostring
     *
     * @return a string
     */
    @Override
    public String toString() {
        return "\nShaderNodeDefinition{\n" + "name=" + name + "\ntype=" + type + "\nshaderPath="
               + shadersPath + "\nshaderLanguage=" + shadersLanguage + "\ndocumentation="
               + documentation + "\ninputs=" + inputs + ",\noutputs=" + outputs + '}';
    }
}
