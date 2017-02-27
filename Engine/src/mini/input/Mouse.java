package mini.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storing mouse states and position information.
 * Computes the delta state of the position.
 *
 * @author miniwolf and Zargess
 */
public class Mouse {
    private static Map<Integer, Action> actions = new HashMap<>();
    private static List<MouseListener> listeners = new ArrayList<>();

    private static double lastXPos = 0.0, lastYPos = 0.0;
    private static double deltaXPos = 0.0, deltaYPos = 0.0;
    private static double wheelYOffset;

    /**
     * Should not be called.
     * (INTERNAL USE ONLY)
     */
    public static void mouseAction(int button, Action action, int mods) {
        actions.put(button, action);

        MouseButton btn = MouseButton.values()[button];
        boolean isPressed = action == Action.PRESS; // TODO: Missing GLFW_REPEAT definition

        for (MouseListener listener : listeners) {
            if (isPressed) {
                listener.onClick(btn, lastXPos, lastYPos);
            } else {
                listener.onRelease(btn, lastXPos, lastYPos);
            }
        }

        System.out.println("Button " + button + " action " + action);
    }

    /**
     * Should not be called.
     * (INTERNAL USE ONLY)
     */
    public static void updatePosition(double xPos, double yPos) {
        deltaXPos = lastXPos - xPos;
        deltaYPos = lastYPos - yPos;

        lastXPos = xPos;
        lastYPos = yPos;
    }

    /**
     * Should not be called.
     * (INTERNAL USE ONLY)
     */
    public static void updateScrollWheel(double xOffset, double yOffset) {
        wheelYOffset = yOffset;
        for (MouseListener listener : listeners) {
            listener.onScroll(wheelYOffset);
        }
    }

    /**
     * Should not be called.
     * (INTERNAL USE ONLY)
     */
    public static void clearActions() {
        actions.clear();
    }

    /**
     * Current there is no distinction between when you press down the button or you hold it down.
     * The distinction is done in the difference between GLFW_PRESS and GLFW_REPEAT.
     *
     * @param button mouse button integer as found in org.lwjgl.glfw constants.
     *               e.g. GLFW_MOUSE_BUTTON_1
     * @return whether the previous actions for the button was GLFW_PRESS or GLFW_REPEAT.
     */
    public static boolean isButtonDown(int button) {
        Action action = actions.get(button);
        return action != null &&
               (action.equals(Action.PRESS) || action.equals(Action.REPEAT));
        // TODO: Answer the question: Is the button down if
        // !equal(GLFW_PRESS) && .equal(GLFW_REPEAT)
    }

    public static void addMouseListener(MouseListener listener) {
        listeners.add(listener);
    }

    public static void removeMouseListener(MouseListener listener) {
        listeners.remove(listener);
    }

    /**
     * @param button mouse button integer as found in org.lwjgl.glfw constants.
     *               e.g. GLFW_MOUSE_BUTTON_1
     * @return whether the previous actions for the button is GLFW_RELEASE
     */
    public static boolean isButtonUp(int button) {
        Action action = actions.get(button);
        return action != null && action.equals(Action.RELEASE);
    }

    public static double getDeltaX() {
        return deltaXPos;
    }

    public static double getDeltaY() {
        return deltaYPos;
    }

    public static double getDeltaWheelY() {
        return wheelYOffset;
    }
}
