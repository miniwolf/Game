package mini.input.events;

import mini.input.KeyboardKey;

/**
 * Keyboard key event.
 */
public class KeyInputEvent extends InputEvent {
    private KeyboardKey keyCode;
    private boolean pressed;
    private boolean repeating;

    public KeyInputEvent(KeyboardKey keyCode, boolean pressed, boolean repeating) {
        this.keyCode = keyCode;
        this.pressed = pressed;
        this.repeating = repeating;
    }

    /**
     * Returns the key character. Returns 0 if the key has no character.
     *
     * @return the key character. 0 if the key has no character.
     */
    public char getKeyChar() {
        return keyCode.getCharValue();
    }

    /**
     * The key code.
     * <p>
     * See KEY_*** constants in {@link KeyInput}.
     *
     * @return key code.
     */
    public KeyboardKey getKey() {
        return keyCode;
    }

    /**
     * Returns true if this event is key press, false is it was a key release.
     *
     * @return true if this event is key press, false is it was a key release.
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Returns true if this event is a repeat event. Not used anymore.
     *
     * @return true if this event is a repeat event
     */
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * Returns true if this event is a key release, false if it was a key press.
     *
     * @return true if this event is a key release, false if it was a key press.
     */
    public boolean isReleased() {
        return !pressed;
    }

    @Override
    public String toString() {
        String str = "Key(" + keyCode;

        if (repeating) {
            return str + ", REPEATING)";
        } else if (pressed) {
            return str + ", PRESSED)";
        } else {
            return str + ", RELEASED)";
        }
    }
}
