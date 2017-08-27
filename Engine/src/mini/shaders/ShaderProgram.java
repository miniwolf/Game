package mini.shaders;

import mini.renderEngine.opengl.GLRenderer;
import mini.scene.VertexBuffer;
import mini.utils.NativeObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaderProgram extends NativeObject {
    private boolean isUpdateNeeded = true;

    /**
     * A list of all shader sources currently attached.
     */
    private List<ShaderSource> shaderSourceList = new ArrayList<>();

    /**
     * Maps attribute name to the location of the attribute in the shader.
     */
    private Map<Integer, Attribute> attribs = new HashMap<>();

    /**
     * Maps uniform name to the uniform variable.
     */
    private Map<String, Uniform> uniforms = new HashMap<>();

    /**
     * Uniforms bound to {@link UniformBinding}s.
     * <p>
     * Managed by the {@link UniformBindingManager}.
     */
    private List<Uniform> boundUniforms = new ArrayList<>();

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
     * Creates a new shader, {@link #initialize() } must be called
     * after this constructor for the shader to be usable.
     */
    public ShaderProgram() {
        super();
        shaderSourceList = new ArrayList<>();
        uniforms = new HashMap<>();
        attribs = new HashMap<>();
        boundUniforms = new ArrayList<>();
    }

    /**
     * Do not use this constructor. Used for destructable clones only.
     */
    protected ShaderProgram(ShaderProgram s) {
        super(s.id);

        // Shader sources cannot be shared, therefore they must
        // be destroyed together with the parent shader.
        shaderSourceList = new ArrayList<>();
        for (ShaderSource source : s.shaderSourceList) {
            shaderSourceList.add((ShaderSource) source.createDestructableClone());
        }

        uniforms = null;
        boundUniforms = null;
        attribs = null;
    }

    /**
     * Adds source code to a certain pipeline.
     *
     * @param type  The pipeline to control
     * @param lines The shader source code lines (in GLSL).
     */
    public void addSource(ShaderType type, String name, String source, String defines, String language) {
        ShaderSource shaderSource = new ShaderSource(type);
        shaderSource.setSource(source);
        shaderSource.setName(name);
        shaderSource.setLanguage(language);
        if (defines != null) {
            shaderSource.setDefines(defines);
        }
        shaderSourceList.add(shaderSource);
        setUpdateNeeded();
    }

    public List<ShaderSource> getSources() {
        return shaderSourceList;
    }

    public void addUniformBinding(UniformBinding binding) {
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

    public Uniform getUniform(String name) {
        assert name.startsWith("m_") || name.startsWith("g_");
        Uniform uniform = uniforms.get(name);
        if (uniform == null) {
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

    public Map<String, Uniform> getUniformMap() {
        return uniforms;
    }

    public List<Uniform> getBoundUniforms() {
        return boundUniforms;
    }

    public boolean isUpdateNeeded() {
        return isUpdateNeeded;
    }

    public void clearUpdateNeeded() {
        isUpdateNeeded = false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "[numSources=" + shaderSourceList.size() +
               ", numUniforms=" + uniforms.size() +
               ", shaderSources=" + getSources() + "]";
    }

    /**
     * Usually called when the shader itself changes or during any
     * time when the variable locations need to be refreshed.
     */
    public void resetLocations() {
        if (uniforms != null) {
            // NOTE: Shader sources will be reset separately from the shader itself.
            for (Uniform uniform : uniforms.values()) {
                uniform.reset(); // fixes issue with re-initialization
            }
        }
        if (attribs != null) {
            for (Map.Entry<Integer, Attribute> entry : attribs.entrySet()) {
                entry.getValue().location = ShaderVariable.LOC_UNKNOWN;
            }
        }
    }

    @Override
    public void setUpdateNeeded() {
        super.setUpdateNeeded();
        resetLocations();
    }

    /**
     * Called by the object manager to reset all object IDs. This causes
     * the shader to be reuploaded to the GPU incase the display was restarted.
     */
    @Override
    public void resetObject() {
        this.id = -1;
        for (ShaderSource source : shaderSourceList) {
            source.resetObject();
        }
        setUpdateNeeded();
    }

    @Override
    public void deleteObject(Object rendererObject) {
        ((GLRenderer) rendererObject).deleteShader(this);
    }

    public NativeObject createDestructableClone() {
        return new ShaderProgram(this);
    }

    @Override
    public long getUniqueId() {
        return ((long) OBJTYPE_SHADER << 32) | ((long) id);
    }
}
