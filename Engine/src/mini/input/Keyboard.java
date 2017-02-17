package mini.input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

/**
 * Created by miniwolf on 15-02-2017.
 */
public class Keyboard {
    private static Map<Integer, Integer> keys = new HashMap<>();

    /**
     * Should not be used
     * (INTERNAL USE)
     */
    public static void updateKey(int key, int action, int mods) {
        keys.put(key, action);
    }

    /**
     * Should not be used
     * (INTERNAL USE)
     */
    public static void clearActions() {
        keys.clear();
    }

    /**
     * @param key
     * @return
     */
    public static boolean isKeyDown(int key) {
        Integer action = keys.get(key);
        return action != null && (action.equals(GLFW_PRESS) || action.equals(GLFW_REPEAT));
    }

    public static boolean isKeyUp(int key) {
        Integer action = keys.get(key);
        return action != null && (action.equals(GLFW_RELEASE));
    }
}
