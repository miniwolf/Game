package mini.input.controls;

/**
 * A <code>KeyTrigger</code> is used as a mapping to keyboard keys.
 */
public class KeyTrigger implements Trigger {

    private final int keyCode;

    /**
     * Create a new <code>KeyTrigger</code> for the given keycode.
     *
     * @param keyCode the code for the key, see constants in {@link KeyInput}.
     */
    public KeyTrigger(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getName() {
        return "KeyCode " + keyCode;
    }

    public static int keyHash(int keyCode) {
        assert keyCode >= 0 && keyCode <= 255;
        return keyCode & 0xff;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int triggerHashCode() {
        return keyHash(keyCode);
    }

}
