package mini.shaders;

/**
 * Created by miniwolf on 28-04-2017.
 */

import mini.renderEngine.opengl.GLRenderer;
import mini.utils.NativeObject;

/**
 * Shader source describes a shader object in OpenGL. Each shader source
 * is assigned a certain pipeline which it controls (described by it's type).
 */
public class ShaderSource extends NativeObject {
    ShaderProgram.ShaderType sourceType;
    String language;
    String name;
    String source;
    String defines;

    public ShaderSource(ShaderProgram.ShaderType type) {
        super();
        this.sourceType = type;
        if (type == null) {
            throw new IllegalArgumentException("The shader type must be specified");
        }
    }

    protected ShaderSource(ShaderSource ss) {
        this.id = ss.id;
        // No data needs to be copied.
        // (This is a destructable clone)
    }

    public ShaderSource() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ShaderProgram.ShaderType getType() {
        return sourceType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (language == null) {
            throw new IllegalArgumentException("Shader language cannot be null");
        }
        this.language = language;
        setUpdateNeeded();
    }

    public void setSource(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Shader source cannot be null");
        }
        this.source = source;
        setUpdateNeeded();
    }

    public void setDefines(String defines) {
        if (defines == null) {
            throw new IllegalArgumentException("Shader defines cannot be null");
        }
        this.defines = defines;
        setUpdateNeeded();
    }

    public String getSource() {
        return source;
    }

    public String getDefines() {
        return defines;
    }

    @Override
    public String toString() {
        String nameTxt = "";
        if (name != null) {
            nameTxt = "name=" + name + ", ";
        }
        if (defines != null) {
            nameTxt += "defines, ";
        }

        return getClass().getSimpleName() + "[" + nameTxt + "type="
               + sourceType.name() + ", language=" + language + "]";
    }

    @Override
    public void resetObject() {
        id = -1;
        setUpdateNeeded();
    }

    @Override
    public void deleteObject(Object rendererObject) {
        ((GLRenderer) rendererObject).deleteShaderSource(ShaderSource.this);
    }

    public NativeObject createDestructableClone() {
        return new ShaderSource(ShaderSource.this);
    }

    @Override
    public long getUniqueId() {
        return ((long)OBJTYPE_SHADER << 32) | ((long)id);
    }
}
