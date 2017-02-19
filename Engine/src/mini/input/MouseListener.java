package mini.input;

/**
 * Created by costa on 19-02-2017.
 */
public interface MouseListener {
    void OnClick(MouseButton btn, double x, double y);
    void OnRelease(MouseButton btn, double x, double y);
}
