package mini.system.lwjgl;

import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GLDebugMessageARBCallback;

import java.util.HashMap;

class LwjglGLDebugOutputHandler extends GLDebugMessageARBCallback {

    private static final HashMap<Integer, String> constMap = new HashMap<>();
    private static final String MESSAGE_FORMAT =
            "[JME3] OpenGL debug message\r\n" +
            "       ID: %d\r\n" +
            "       Source: %s\r\n" +
            "       Type: %s\r\n" +
            "       Severity: %s\r\n" +
            "       Message: %s";

    static {
        constMap.put(ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB, "API");
        constMap.put(ARBDebugOutput.GL_DEBUG_SOURCE_APPLICATION_ARB, "APPLICATION");
        constMap.put(ARBDebugOutput.GL_DEBUG_SOURCE_OTHER_ARB, "OTHER");
        constMap.put(ARBDebugOutput.GL_DEBUG_SOURCE_SHADER_COMPILER_ARB, "SHADER_COMPILER");
        constMap.put(ARBDebugOutput.GL_DEBUG_SOURCE_THIRD_PARTY_ARB, "THIRD_PARTY");
        constMap.put(ARBDebugOutput.GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB, "WINDOW_SYSTEM");

        constMap.put(ARBDebugOutput.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB, "DEPRECATED_BEHAVIOR");
        constMap.put(ARBDebugOutput.GL_DEBUG_TYPE_ERROR_ARB, "ERROR");
        constMap.put(ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB, "OTHER");
        constMap.put(ARBDebugOutput.GL_DEBUG_TYPE_PERFORMANCE_ARB, "PERFORMANCE");
        constMap.put(ARBDebugOutput.GL_DEBUG_TYPE_PORTABILITY_ARB, "PORTABILITY");
        constMap.put(ARBDebugOutput.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB, "UNDEFINED_BEHAVIOR");

        constMap.put(ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB, "HIGH");
        constMap.put(ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB, "MEDIUM");
        constMap.put(ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB, "LOW");
    }

    @Override
    public void invoke(int source, int type, int id, int severity, int length, long message,
                       long userParam) {
        String sourceStr = constMap.get(source);
        String typeStr = constMap.get(type);
        String severityStr = constMap.get(severity);

        System.err.println(
                String.format(MESSAGE_FORMAT, id, sourceStr, typeStr, severityStr, message));
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void callback(long args) {
        super.callback(args);
    }
}
