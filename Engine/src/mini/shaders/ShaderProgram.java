package mini.shaders;

import mini.scene.VertexBuffer;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaderProgram {
    private int programID = -1;
    private boolean isUpdateNeeded = true;

    /**
     * A list of all shader sources currently attached.
     */
    private final List<ShaderSource> shaderSourceList = new ArrayList<>();

    /**
     * Maps attribute name to the location of the attribute in the shader.
     */
    private final Map<Integer, Attribute> attribs = new HashMap<>();

    /**
     * Maps uniform name to the uniform variable.
     */
    private final Map<String, Uniform> uniforms = new HashMap<>();

    /**
     * Uniforms bound to {@link UniformBinding}s.
     *
     * Managed by the {@link UniformBindingManager}.
     */
    private final List<Uniform> boundUniforms = new ArrayList<>();

    /**
     * Type of shader. The shader will control the pipeline of it's type.
     */
    public enum ShaderType {

        /**
         * Control fragment rasterization. (e.g color of pixel).
         */
        Fragment("frag"),
        /**
         * Control vertex processing. (e.g transform of model to clip space)
         */
        Vertex("vert"),
        /**
         * Control geometry assembly. (e.g compile a triangle list from input
         * data)
         */
        Geometry("geom"),
        /**
         * Controls tesselation factor (e.g how often a input patch should be
         * subdivided)
         */
        TessellationControl("tsctrl"),
        /**
         * Controls tesselation transform (e.g similar to the vertex shader, but
         * required to mix inputs manual)
         */
        TessellationEvaluation("tseval");

        private String extension;

        public String getExtension() {
            return extension;
        }

        ShaderType(String extension) {
            this.extension = extension;
        }
    }

    /*public ShaderProgram(MyFile vertexFile, MyFile fragmentFile, String... inVariables) {
        int vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        int fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        programID = GL20.glCreateProgram();
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        bindAttributes(inVariables);
        GL20.glLinkProgram(programID);
        GL20.glDetachShader(programID, vertexShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
    }*/

    /**
     * Adds source code to a certain pipeline.
     *
     * @param type The pipeline to control
     * @param lines The shader source code lines (in GLSL).
     */
    public void addSource(ShaderType type, String name, List<String> lines){
        StringBuilder source = new StringBuilder();
        try {
            lines.forEach(line -> source.append(line).append("//\n"));
        } catch (Exception e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }
        ShaderSource shaderSource = new ShaderSource(type);
        shaderSource.setSource(source.toString());
        shaderSource.setName(name);
        shaderSourceList.add(shaderSource);
        setUpdateNeeded();
    }

    public List<ShaderSource> getSources(){
        return shaderSourceList;
    }

    protected void storeAllUniformLocations(Uniform... uniforms) {
        for (Uniform uniform : uniforms) {
            uniform.storeUniformLocation(programID);
        }
        GL20.glValidateProgram(programID);
    }

    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void cleanUp() {
        stop();
        GL20.glDeleteProgram(programID);
    }

    private void bindAttributes(String[] inVariables) {
        for (int i = 0; i < inVariables.length; i++) {
            GL20.glBindAttribLocation(programID, i, inVariables[i]);
        }
    }

    public void addUniformBinding(UniformBinding binding){
        String uniformName = "g_" + binding.name();
        Uniform uniform = uniforms.get(uniformName);
        if (uniform == null) {
            uniform = new Uniform();
            uniform.name = uniformName;
            uniform.binding = binding;
            uniforms.put(uniformName, uniform);
            boundUniforms.add(uniform);
        }
    }

    public Uniform getUniform(String name){
        assert name.startsWith("m_") || name.startsWith("g_");
        Uniform uniform = uniforms.get(name);
        if (uniform == null){
            uniform = new Uniform();
            uniform.name = name;
            uniforms.put(name, uniform);
        }
        return uniform;
    }

    public Attribute getAttribute(VertexBuffer.Type attribType) {
        int ordinal = attribType.ordinal();
        Attribute attrib = attribs.get(ordinal);
        if (attrib == null) {
            attrib = new Attribute();
            attrib.name = attribType.name();
            attribs.put(ordinal, attrib);
        }
        return attrib;
    }

    public void setId(int id) {
        programID = id;
    }

    public int getId() {
        return programID;
    }

    public Map<String, Uniform> getUniformMap(){
        return uniforms;
    }

    public List<Uniform> getBoundUniforms() {
        return boundUniforms;
    }

    public boolean isUpdateNeeded() {
        return isUpdateNeeded;
    }

    public void setUpdateNeeded() {
        isUpdateNeeded = true;
    }

    public void clearUpdateNeeded() {
        isUpdateNeeded = false;
    }
}
