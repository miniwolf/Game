package mini.input;

/**
 * @author miniwolf and Zargess
 */
public enum Action {
    RELEASE, PRESS, REPEAT;

    private static final Action[] actions = Action.values();

    public static Action[] getValues() {
        return actions;
    }
}
