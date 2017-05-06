package mini.shaders;

/**
 * Created by miniwolf on 18-03-2017.
 */
public enum VarType {
    Float("float"),
    Boolean("bool"),
    Int("int"),

    Vector2("vec2"),
    Vector3("vec3"),
    Vector4("vec4"),

    Matrix3f(true,false,"mat3"),
    Matrix4f(true,false,"mat4"),

    Texture2D(false,true,"sampler2D|sampler2DShadow");


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

    public boolean isTextureType() {
        return textureType;
    }
}
