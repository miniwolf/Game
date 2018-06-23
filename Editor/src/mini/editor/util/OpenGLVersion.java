package mini.editor.util;

import mini.system.ApplicationSettings;

public enum OpenGLVersion {
    GL_20(ApplicationSettings.LWJGL_OPENGL2, "2.0 with any profile"),
    GL_32(ApplicationSettings.LWJGL_OPENGL3, "3.2 with core profile"),
    GL_33(ApplicationSettings.LWJGL_OPENGL33, "3.3 with core profile");
    /**
     * The value for render.
     */
    private final String render;
    /**
     * The label for UI.
     */
    private final String label;

    OpenGLVersion(String render, String label) {
        this.render = render;
        this.label = label;
    }

    /**
     * @return the value for render.
     */
    public String getRender() {
        return render;
    }
}
