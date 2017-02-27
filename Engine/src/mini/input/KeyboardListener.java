package mini.input;

/**
 * Created by costa on 19-02-2017.
 */
public interface KeyboardListener {
    void onClick(KeyboardKey key, int mods);
    void onRelease(KeyboardKey key, int mods);
}
