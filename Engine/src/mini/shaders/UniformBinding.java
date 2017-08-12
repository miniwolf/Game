package mini.shaders;

/**
 * Created by miniwolf on 21-04-2017.
 */
public enum UniformBinding {

    /**
     * The world matrix. Converts Model space to World space.
     * Type: mat4
     */
    WorldMatrix("mat4"),

    /**
     * The view matrix. Converts World space to View space.
     * Type: mat4
     */
    ViewMatrix("mat4"),

    /**
     * The projection matrix. Converts View space to Clip/Projection space.
     * Type: mat4
     */
    ProjectionMatrix("mat4"),

    /**
     * The world view matrix. Converts Model space to View space.
     * Type: mat4
     */
    WorldViewMatrix("mat4"),

    /**
     * The normal matrix. The inverse transpose of the worldview matrix.
     * Converts normals from model space to view space.
     * Type: mat3
     */
    NormalMatrix("mat3"),

    /**
     * The world view projection matrix. Converts Model space to Clip/Projection
     * space.
     * Type: mat4
     */
    WorldViewProjectionMatrix("mat4"),

    /**
     * The view projection matrix. Converts World space to Clip/Projection
     * space.
     * Type: mat4
     */
    ViewProjectionMatrix("mat4"),

    /**
     * The world matrix inverse transpose. Converts a normals from Model space
     * to world space.
     * Type: mat3
     */
    WorldMatrixInverseTranspose("mat3"),



    WorldMatrixInverse("mat4"),
    ViewMatrixInverse("mat4"),
    ProjectionMatrixInverse("mat4"),
    ViewProjectionMatrixInverse("mat4"),
    WorldViewMatrixInverse("mat4"),
    NormalMatrixInverse("mat3"),
    WorldViewProjectionMatrixInverse("mat4"),

    /**
     * Contains the four viewport parameters in this order:
     * X = Left,
     * Y = Top,
     * Z = Right,
     * W = Bottom.
     * Type: vec4
     */
    ViewPort("vec4"),

    /**
     * The near and far values for the camera frustum.
     * X = Near
     * Y = Far.
     * Type: vec2
     */
    FrustumNearFar("vec2"),

    /**
     * The width and height of the camera.
     * Type: vec2
     */
    Resolution("vec2"),

    /**
     * The inverse of the resolution, 1/width and 1/height.
     * Type: vec2
     */
    ResolutionInverse("vec2"),

    /**
     * Aspect ratio of the resolution currently set. Width/Height.
     * Type: float
     */
    Aspect("float"),

    /**
     * Camera position in world space.
     * Type: vec3
     */
    CameraPosition("vec3"),

    /**
     * Direction of the camera.
     * Type: vec3
     */
    CameraDirection("vec3"),

    /**
     * Left vector of the camera.
     * Type: vec3
     */
    CameraLeft("vec3"),

    /**
     * Up vector of the camera.
     * Type: vec3
     */
    CameraUp("vec3"),

    /**
     * Time in seconds since the application was started.
     * Type: float
     */
    Time("float"),

    /**
     * Time in seconds that the last frame took.
     * Type: float
     */
    Tpf("float"),

    /**
     * Frames per second.
     * Type: float
     */
    FrameRate("float"),

    /**
     * The light direction when rendering in multi pass mode
     * Type: vec4
     */
    LightDirection("vec4"),

    /**
     * The light position when rendering in multi pass mode
     * Type: vec4
     */
    LightPosition("vec4"),

    /**
     * Ambient light color
     * Type: vec4
     */
    AmbientLightColor("vec4"),

    /**
     * The light color when rendering in multi pass mode
     * Type: vec4
     */
    LightColor("vec4");

    String glslType;

    private UniformBinding() {
    }

    private UniformBinding(String glslType) {
        this.glslType = glslType;
    }

    public String getGlslType() {
        return glslType;
    }
}