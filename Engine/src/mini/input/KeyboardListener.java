package mini.input;

/**
 * Created by costa on 19-02-2017.
 */
public interface KeyboardListener {
    void OnClick(KeyboardKey key, int mods);
    void OnRelease(KeyboardKey key, int mods);
}
