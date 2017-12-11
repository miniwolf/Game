package mini.shaders;

import java.util.Arrays;
import java.util.List;

/**
 * The new define list.
 */
public final class DefineList {

    public static final int MAX_DEFINES = 64;

    private long isSet;
    private final int[] values;

    public DefineList(int numValues) {
        if (numValues < 0 || numValues > MAX_DEFINES) {
            throw new IllegalArgumentException("numValues must be between 0 and 64");
        }
        values = new int[numValues];
    }

    private DefineList(DefineList original) {
        this.isSet = original.isSet;
        this.values = new int[original.values.length];
        System.arraycopy(original.values, 0, values, 0, values.length);
    }

    private void rangeCheck(int id) {
        assert 0 <= id && id < values.length;
    }

    public boolean isSet(int id) {
        rangeCheck(id);
        return (isSet & (1L << id)) != 0;
    }

    public void unset(int id) {
        rangeCheck(id);
        isSet &= ~(1L << id);
        values[id] = 0;
    }

    public void set(int id, int val) {
        rangeCheck(id);
        isSet |= (1L << id);
        values[id] = val;
    }

    public void set(int id, float val) {
        set(id, Float.floatToIntBits(val));
    }

    public void set(int id, boolean val) {
        if (val) {
            set(id, 1);
        } else {
            // Because #ifdef usage is very common in shaders, unset the define
            // instead of setting it to 0 for booleans.
            unset(id);
        }
    }

    public void set(int id, VarType type, Object value) {
        if (value != null) {
            switch (type) {
                case Int:
                    set(id, (Integer) value);
                    break;
                case Float:
                    set(id, (Float) value);
                    break;
                case Boolean:
                    set(id, ((Boolean) value));
                    break;
                default:
                    set(id, 1);
                    break;
            }
        } else {
            unset(id);
        }
    }

    public void setAll(DefineList other) {
        for (int i = 0; i < other.values.length; i++) {
            if (other.isSet(i)) {
                set(i, other.getInt(i));
            }
        }
    }

    public void clear() {
        isSet = 0;
        Arrays.fill(values, 0);
    }

    public boolean getBoolean(int id) {
        return values[id] != 0;
    }

    public float getFloat(int id) {
        return Float.intBitsToFloat(values[id]);
    }

    public int getInt(int id) {
        return values[id];
    }

    @Override
    public int hashCode() {
        return (int) ((isSet >> 32) ^ isSet);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DefineList)) {
            return false;
        }

        DefineList o = (DefineList) other;
        if (isSet != o.isSet) {
            return false;
        }

        for (int i = 0; i < values.length; i++) {
            if (values[i] != o.values[i]) {
                return false;
            }
        }
        return true;
    }

    public DefineList deepClone() {
        return new DefineList(this);
    }

    public void generateSource(StringBuilder sb, List<String> defineNames, List<VarType> defineTypes) {
        for (int i = 0; i < values.length; i++) {
            if (!isSet(i)) {
                continue;
            }

            sb.append("#define ").append(defineNames.get(i)).append(' ');

            if (defineTypes != null && defineTypes.get(i) == VarType.Float) {
                float val = Float.intBitsToFloat(values[i]);
                if (Float.isInfinite(val) || Float.isNaN(val)) {
                    throw new IllegalArgumentException(
                            "GLSL does not support NaN "
                            + "or Infinite float literals");
                }
                sb.append(val);
            } else {
                sb.append(values[i]);
            }

            sb.append('\n');
        }
    }

    public String generateSource(List<String> defineNames, List<VarType> defineTypes) {
        StringBuilder sb = new StringBuilder();
        generateSource(sb, defineNames, defineTypes);
        return sb.toString();
    }
}
