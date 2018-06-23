package mini.system.lwjgl;

import mini.system.ApplicationSettings;

public class LwjglOffscreenBuffer extends LwjglWindow {
    public LwjglOffscreenBuffer() {
        super(Type.OffscreenSurface);
    }

    @Override
    protected void showWindow() {
    }

    @Override
    protected void setWindowIcon(ApplicationSettings settings) {
    }
}
