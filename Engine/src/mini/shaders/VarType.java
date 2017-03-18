package mini.shaders;

/**
 * Created by miniwolf on 18-03-2017.
 */
public enum VarType {
    Float("float"),
    Vector3("vec3"),
    Vector4("vec4");

    private boolean multiData = false;
    private boolean textureType = false;
    private String glslType;

    VarType(String glslType) {
        this.glslType = glslType;
    }

    VarType(boolean multiData, boolean textureType, String glslType) {
        this.multiData = multiData;
        this.textureType = textureType;
        this.glslType = glslType;
    }
}
