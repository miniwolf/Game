package mini.shaders;

import mini.math.ColorRGBA;
import mini.math.Matrix3f;
import mini.math.Matrix4f;
import mini.math.Quaternion;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.utils.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Uniform extends ShaderVariable {
    private static final Integer ZERO_INT = 0;
    private static final Float ZERO_FLT = 0f;
    private static final int NOT_FOUND = -1;
    private static final FloatBuffer ZERO_BUF = BufferUtils.createFloatBuffer(4 * 4);

    /**
     * Currently set value of the uniform.
     */
    private Object value = null;

    /**
     * For arrays or matrices, efficient format
     * that can be sent to GL faster.
     */
    private FloatBuffer multiData = null;

    /**
     * Type of uniform
     */
    private VarType varType;

    /**
     * Binding to a renderer value, or null if user-defined uniform
     */
    protected UniformBinding binding;

    /**
     * Used to track which uniforms to clear to avoid
     * values leaking from other materials that use that shader.
     */
    private boolean setByCurrentMaterial = false;

    private int location;

    protected void storeUniformLocation(int programID) {
        location = GL20.glGetUniformLocation(programID, name);
        if (location == NOT_FOUND) {
            System.err.println("No uniform variable called " + name + " found!");
        }
    }

    public void clearValue() {
        updateNeeded = true;

        if (multiData != null) {
            multiData.clear();

            while (multiData.remaining() > 0) {
                ZERO_BUF.clear();
                ZERO_BUF.limit(Math.min(multiData.remaining(), 16));
                multiData.put(ZERO_BUF);
            }

            multiData.clear();

            return;
        }

        if (varType == null) {
            return;
        }

        switch (varType) {
            case Int:
                this.value = ZERO_INT;
                break;
            case Boolean:
                this.value = Boolean.FALSE;
                break;
            case Float:
                this.value = ZERO_FLT;
                break;
            case Vector2f:
                if (this.value != null) {
                    ((Vector2f) this.value).set(Vector2f.ZERO);
                }
                break;
            case Vector3f:
                if (this.value != null) {
                    ((Vector3f) this.value).set(Vector3f.ZERO);
                }
                break;
            case Vector4f:
                if (this.value != null) {
                    if (this.value instanceof ColorRGBA) {
                        ((ColorRGBA) this.value).set(ColorRGBA.BlackNoAlpha);
                    } else if (this.value instanceof Vector4f) {
                        ((Vector4f) this.value).set(Vector4f.ZERO);
                    } else {
                        ((Quaternion) this.value).set(Quaternion.ZERO);
                    }
                }
                break;
            default:
                // won't happen because those are either textures
                // or multidata types
        }
    }

    public void setValue(VarType type, Object value) {
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
            case Matrix3f:
                if (value.equals(this.value)) {
                    return;
                }
                Matrix3f m3 = (Matrix3f) value;
                if (multiData == null) {
                    multiData = BufferUtils.createFloatBuffer(9);
                }
                m3.fillFloatBuffer(multiData, true);
                multiData.clear();
                if (this.value == null) {
                    this.value = new Matrix3f(m3);
                } else {
                    ((Matrix3f) this.value).set(m3);
                }
                break;
            case Matrix4f:
                if (value.equals(this.value)) {
                    return;
                }
                Matrix4f m4 = (Matrix4f) value;
                if (multiData == null) {
                    multiData = BufferUtils.createFloatBuffer(16);
                }
                m4.fillFloatBuffer(multiData, true);
                multiData.clear();
                if (this.value == null) {
                    this.value = new Matrix4f(m4);
                } else {
                    ((Matrix4f) this.value).copy(m4);
                }
                break;
            case Vector3f:
                if (value.equals(this.value)) {
                    return;
                }
                if (this.value == null) {
                    this.value = new Vector3f((Vector3f) value);
                } else {
                    ((Vector3f) this.value).set((Vector3f) value);
                }
                break;
            case Vector4f:
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

    public void setVector4fLength(int length) {
        if (location == -1) {
            return;
        }

        multiData = BufferUtils.ensureLargeEnough(multiData, length * 4);
        value = multiData;
        varType = VarType.Vector4fArray;
        updateNeeded = true;
        setByCurrentMaterial = true;
    }

    public void setVector4fInArray(float x, float y, float z, float w, int index) {
        if (location == -1) {
            return;
        }

        if (varType != null && varType != VarType.Vector4fArray) {
            throw new IllegalArgumentException("Expected a " + varType.name() + " value!");
        }

        multiData.position(index * 4);
        multiData.put(x).put(y).put(z).put(w);
        multiData.rewind();
        updateNeeded = true;
        setByCurrentMaterial = true;
    }

    public Object getValue() {
        return value;
    }

    public VarType getVarType() {
        return varType;
    }

    public FloatBuffer getMultiData() {
        return multiData;
    }

    public boolean isSetByCurrentMaterial() {
        return setByCurrentMaterial;
    }

    public void clearSetByCurrentMaterial() {
        setByCurrentMaterial = false;
    }

    public UniformBinding getBinding() {
        return binding;
    }

    public void reset() {
        setByCurrentMaterial = false;
        location = -2;
        updateNeeded = true;
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    public void clearUpdateNeeded() {
        updateNeeded = false;
    }
}
