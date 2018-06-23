package mini.editor.config;

import mini.editor.util.OpenGLVersion;

public class DefaultSettingsProvider {

    public interface Preferences {
        String PREF_OPEN_GL = "core.graphics.openGL.version";
        String PREF_GAMMA_CORRECTION = "core.graphics.gammaCorrection";
        String PREF_FRAME_RATE = "core.graphics.frame.rate";
    }

    public interface Defaults {
        OpenGLVersion PREF_DEFAULT_OPEN_GL = OpenGLVersion.GL_33;
        boolean PREF_DEFAULT_GAMMA_CORRECTION = true;
        int PREF_DEFAULT_FRAME_RATE = 60;
    }
}
