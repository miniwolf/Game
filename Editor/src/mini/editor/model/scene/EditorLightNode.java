package mini.editor.model.scene;

import mini.editor.annotation.EditorThread;
import mini.light.Light;
import mini.scene.Node;

public class EditorLightNode extends Node {
    private Light light;

    @EditorThread
    public void sync() {
        throw new UnsupportedOperationException();
    }

    @EditorThread
    public void setLight(Light light) {
        this.light = light;
    }

    @EditorThread
    public Light getLight() {
        return light;
    }
}
