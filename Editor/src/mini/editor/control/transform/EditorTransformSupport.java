package mini.editor.control.transform;

import mini.editor.annotation.MiniThread;
import mini.renderer.Camera;

public interface EditorTransformSupport {
    @MiniThread
    Camera getCamera();
}
