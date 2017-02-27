package mini.input;

import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

/**
 * Created by miniwolf on 15-02-2017.
 */
public class Keyboard {
    private static Map<Integer, Integer> keys = new HashMap<>();
    private static List<KeyboardListener> listeners = new ArrayList<>();

    /**
     * Should not be used
     * (INTERNAL USE)
     */
    public static void updateKey(int key, int action, int mods) {
        keys.put(key, action);

        KeyboardKey k = getKey(key);
        Action a = action == 0 ? Action.RELEASE : Action.PRESS;
        boolean isPressed = a == Action.PRESS;
        for ( KeyboardListener listener : listeners ) {
            if (isPressed) {
                listener.onClick(k, mods);
            } else  {
                listener.onRelease(k, mods);
            }
        }
    }

    /**
     * Should not be used
     * (INTERNAL USE)
     */
    public static void clearActions() {
        keys.clear();
    }

    /**
     * Register as listener to keyboard
     * @param listener
     */
    public static void addListener(KeyboardListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister as listener to keyboard
     * @param listener
     */
    public static void removeListener(KeyboardListener listener) {
        listeners.remove(listener);
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

    private static KeyboardKey getKey(int key) {
        Optional<KeyboardKey> opt = Arrays.stream(KeyboardKey.values()).parallel().filter(x -> x.getValue() == key).findFirst();

        return opt.isPresent() ? opt.get() : KeyboardKey.KEY_UNKNOWN;
    }
}
