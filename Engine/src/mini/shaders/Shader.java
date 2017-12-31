package mini.shaders;

import mini.asset.AssetManager;
import mini.renderer.Renderer;
import mini.scene.VertexBuffer;
import mini.utils.NativeObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Shader extends NativeObject {

    /**
     * A list of all shader sources currently attached.
     */
    private final List<ShaderSource> shaderSourceList;

    /**
     * Maps uniform name to the uniform variable.
     */
    private final Map<String, Uniform> uniforms;

    /**
     * Uniforms bound to {@link UniformBinding}s.
     * <p>
     * Managed by the {@link UniformBindingManager}.
     */
    private final List<Uniform> boundUniforms;

    /**
     * Maps attribute name to the location of the attribute in the shader.
     */
    private final Map<Integer, Attribute> attribs;

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

    /**
     * Creates a new shader.
     */
    public Shader() {
        super();
        shaderSourceList = new ArrayList<>();
        uniforms = new HashMap<>();
        attribs = new HashMap<>();
        boundUniforms = new ArrayList<>();
    }

    /**
     * Do not use this constructor. Used for destructable clones only.
     */
    protected Shader(Shader s) {
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

    public void preloadSource(AssetManager assetManager) {
        for (ShaderSource shaderSource : shaderSourceList) {
            shaderSource.source = (String) assetManager.loadAsset(shaderSource.name);
            shaderSource.setUpdateNeeded();
        }
        updateNeeded = true;
    }

    /**
     * Adds source code to a certain pipeline.
     *
     * @param type     The pipeline to control
     * @param source   The shader source code (in GLSL).
     * @param defines  Preprocessor defines (placed at the beginning of the shader)
     * @param language The shader source language, currently accepted is GLSL###
     *                 where ### is the version, e.g. GLSL100 = GLSL 1.0, GLSL330 = GLSL 3.3, etc.
     */
    public void addSource(ShaderType type, String name, String source, String defines,
                          String language) {
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

    public void removeUniform(String name) {
        uniforms.remove(name);
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

    public Collection<ShaderSource> getSources() {
        return shaderSourceList;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "[numSources=" + shaderSourceList.size() +
               ", numUniforms=" + uniforms.size() +
               ", shaderSources=" + getSources() + "]";
    }

    /**
     * Removes the "set-by-current-material" flag from all uniforms.
     * When a uniform is modified after this call, the flag shall
     * become "set-by-current-material".
     * A call to {@link #resetUniformsNotSetByCurrent() } will reset
     * all uniforms that do not have the "set-by-current-material" flag
     * to their default value (usually all zeroes or false).
     */
    public void clearUniformsSetByCurrentFlag() {
        int size = uniforms.size();
        for (Uniform u : uniforms.values()) {
            u.clearSetByCurrentMaterial();
        }
    }

    /**
     * Resets all uniforms that do not have the "set-by-current-material" flag
     * to their default value (usually all zeroes or false).
     * When a uniform is modified, that flag is set, to remove the flag,
     * use {@link #clearUniformsSetByCurrent() }.
     */
    public void resetUniformsNotSetByCurrent() {
        int size = uniforms.size();
        for (Uniform u : uniforms.values()) {
            if (!u.isSetByCurrentMaterial()) {
                u.clearValue();
            }
        }
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
            for (Attribute attribute : attribs.values()) {
                attribute.location = ShaderVariable.LOC_UNKNOWN;
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
        ((Renderer) rendererObject).deleteShader(this);
    }

    public NativeObject createDestructableClone() {
        return new Shader(this);
    }

    @Override
    public long getUniqueId() {
        return ((long) OBJTYPE_SHADER << 32) | ((long) id);
    }
}
