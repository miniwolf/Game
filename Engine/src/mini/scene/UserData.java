package mini.scene;

import mini.export.Savable;

import java.util.List;
import java.util.Map;

/**
 * <code>UserData</code> is used to contain user data objects set on spatials (primarily primitives)
 * that do not implement an interface.
 */
public class UserData<T> {
    private static final int TYPE_INTEGER = 0;
    private static final int TYPE_FLOAT = 1;
    private static final int TYPE_BOOLEAN = 2;
    private static final int TYPE_STRING = 3;
    private static final int TYPE_LONG = 4;
    private static final int TYPE_LIST = 5;
    private static final int TYPE_MAP = 6;
    private static final int TYPE_ARRAY = 7;
    private static final int TYPE_SAVABLE = 8;
    private final byte type;
    private final T value;

    /**
     * Creates a new <code>UserData</code> with the given type and value
     *
     * @param type  Type of data, should be between 0 and 7
     * @param value Value of the data
     */
    public UserData(byte type, T value) {
        assert type >= 0 && type <= 7;
        this.type = type;
        this.value = value;
    }

    public static byte getObjectType(Object type) {
        if (type instanceof Integer) {
            return TYPE_INTEGER;
        } else if (type instanceof Boolean) {
            return TYPE_BOOLEAN;
        } else if (type instanceof String) {
            return TYPE_STRING;
        } else if (type instanceof Long) {
            return TYPE_LONG;
        } else if (type instanceof Float) {
            return TYPE_FLOAT;
        } else if (type instanceof List) {
            return TYPE_LIST;
        } else if (type instanceof Map) {
            return TYPE_MAP;
        } else if (type instanceof Object[]) {
            return TYPE_ARRAY;
        } else if (type instanceof Savable) {
            return TYPE_SAVABLE;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type.getClass().getName());
        }
    }

    public byte getType() {
        return type;
    }

    public T getValue() {
        return value;
    }
}
