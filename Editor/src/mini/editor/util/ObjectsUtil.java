package mini.editor.util;

import java.util.Objects;

public class ObjectsUtil {
    public static <T> T notNull(T obj, String message) {
        return Objects.requireNonNull(obj, message);
    }

    public static <T> T notNull(T obj) {
        return Objects.requireNonNull(obj);
    }
}
