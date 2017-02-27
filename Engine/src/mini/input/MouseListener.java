package mini.input;

/**
 * @author Zargess
 */
public interface MouseListener {
    void onClick(MouseButton btn, double x, double y);
    void onRelease(MouseButton btn, double x, double y);
    void onScroll(double offset);
}
