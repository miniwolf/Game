package mini.shaders;

/**
 * Created by miniwolf on 18-03-2017.
 */
public enum VarType {
    Float("float"),
    Boolean("bool"),
    Int("int"),

    Vector2f("vec2"),
    Vector3f("vec3"),
    Vector4f("vec4"),

    IntArray(true,false,"int"),
    FloatArray(true,false,"float"),
    Vector2fArray(true,false,"vec2"),
    Vector3fArray(true,false,"vec3"),
    Vector4fArray(true,false,"vec4"),

    Matrix3f(true,false,"mat3"),
    Matrix4f(true,false,"mat4"),

    Matrix3fArray(true,false,"mat3"),
    Matrix4fArray(true,false,"mat4"),

    Texture2D(false,true,"sampler2D|sampler2DShadow"),
    Texture3D(false,true,"sampler3D"),
    TextureArray(false,true,"sampler2DArray|sampler2DArrayShadow"),
    TextureCubeMap(false,true,"samplerCube");

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

    public String getGlslType() {
        return glslType;
    }

    public boolean isMultiData() {
        return multiData;
    }

}
