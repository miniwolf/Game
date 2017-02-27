package mini.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author miniwolf and Zargess
 */
public class Keyboard {
    private static Map<KeyboardKey, Action> keys = new HashMap<>();
    private static List<KeyboardListener> listeners = new ArrayList<>();

    /**
     * Should not be used
     * (INTERNAL USE)
     */
    public static void updateKey(KeyboardKey key, Action action, int mods) {
        keys.put(key, action);
        boolean isPressed = action == Action.PRESS;
        for (KeyboardListener listener : listeners) {
            if (isPressed) {
                listener.onClick(key, mods);
            } else {
                listener.onRelease(key, mods);
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
     *
     * @param listener
     */
    public static void addListener(KeyboardListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister as listener to keyboard
     *
     * @param listener
     */
    public static void removeListener(KeyboardListener listener) {
        listeners.remove(listener);
    }

    /**
     * @param key
     * @return whether key is pressed down.
     */
    public static boolean isKeyDown(KeyboardKey key) {
        Action action = keys.get(key);
        return action != null && (action.equals(Action.PRESS) || action.equals(Action.REPEAT));
    }

    public static boolean isKeyUp(KeyboardKey key) {
        Action action = keys.get(key);
        return action != null && (action.equals(Action.RELEASE));
    }
}
