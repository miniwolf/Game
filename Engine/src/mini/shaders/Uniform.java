package mini.shaders;

import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.math.Vector4f;
import org.lwjgl.opengl.GL20;

public class Uniform extends ShaderVariable {
    private static final int NOT_FOUND = -1;

    /**
     * Currently set value of the uniform.
     */
    protected Object value = null;

    /**
     * Type of uniform
     */
    protected VarType varType;

    /**
     * Used to track which uniforms to clear to avoid
     * values leaking from other materials that use that shader.
     */
    protected boolean setByCurrentMaterial = false;

    private int location;

    protected void storeUniformLocation(int programID){
        location = GL20.glGetUniformLocation(programID, name);
        if (location == NOT_FOUND){
            System.err.println("No uniform variable called " + name + " found!");
        }
    }

    public void setValue(VarType type, Object value){
        if (location == LOC_NOT_DEFINED) {
            return;
        }

        if (varType != null && varType != type) {
            throw new IllegalArgumentException("Expected a " + varType.name() + " value!");
        }

        if (value == null) {
            throw new IllegalArgumentException("for uniform " + name + ": value cannot be null");
        }

        setByCurrentMaterial = true;

        switch (type) {
            case Vector3:
                if (value.equals(this.value)) {
                    return;
                }
                if (this.value == null) {
                    this.value = new Vector3f((Vector3f) value);
                } else {
                    ((Vector3f) this.value).set((Vector3f) value);
                }
                break;
            case Vector4:
                if (value.equals(this.value)) {
                    return;
                }
                if (value instanceof ColorRGBA) {
                    if (this.value == null) {
                        this.value = new ColorRGBA();
                    }
                    ((ColorRGBA) this.value).set((ColorRGBA) value);
                } else if (value instanceof Vector4f) {
                    if (this.value == null) {
                        this.value = new Vector4f();
                    }
                    ((Vector4f) this.value).set((Vector4f) value);
                } else {
                    if (this.value == null) {
                        this.value = new Quaternion();
                    }
                    ((Quaternion) this.value).set((Quaternion) value);
                }
                break;
            // Only use check if equals optimization for primitive values
            case Int:
            case Float:
                if (value.equals(this.value)) {
                    return;
                }
                this.value = value;
                break;
            default:
                this.value = value;
                break;
        }

        varType = type;
        updateNeeded = true;
    }

    public boolean isSetByCurrentMaterial() {
        return setByCurrentMaterial;
    }
}
