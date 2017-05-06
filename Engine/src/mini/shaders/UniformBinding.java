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
     * The view projection matrix. Converts World space to Clip/Projection space.
     * Type: mat4
     */
    ViewProjectionMatrix("mat4"),

    /**
     * Camera position in world space.
     * Type: vec3
     */
    CameraPosition("vec3"),

    LightDirection("vec3");

    private String glslType;

    UniformBinding(String glslType) {
        this.glslType = glslType;
    }
}
