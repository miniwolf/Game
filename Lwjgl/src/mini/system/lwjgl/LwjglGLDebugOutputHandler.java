package mini.system.lwjgl;

import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.ARBDebugOutputCallback;

import java.util.HashMap;
import java.util.Map;

class LwjglGLDebugOutputHandler implements ARBDebugOutputCallback.Handler {

    private static final Map<Integer, String> constMap = new HashMap<>();
    private static final String MESSAGE_FORMAT =
            "[MiniEngine] OpenGL debug message\r\n" +
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

        constMap.put(ARBDebugOutput.GL_DEBUG_CALLBACK_FUNCTION_ARB, "CALLBACK_FUNCTION");
        constMap.put(ARBDebugOutput.GL_DEBUG_CALLBACK_USER_PARAM_ARB, "USER_PARAM");
    }

    @Override
    public void handleMessage(int source, int type, int id, int severity, String message) {
        String sourceStr = constMap.get(source);
        String typeStr = constMap.get(type);
        String severityStr = constMap.get(severity);

        //System.out.println(String.format(MESSAGE_FORMAT, id, sourceStr, typeStr, severityStr, message));
        //Thread.dumpStack();
    }

}
