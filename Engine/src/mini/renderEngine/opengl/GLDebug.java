package mini.renderEngine.opengl;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

public class GLDebug {
    private static String decodeError(int err) {
        String errMsg;
        switch (err) {
            case GL11.GL_NO_ERROR:
                errMsg = "No Error";
                break;
            case GL11.GL_INVALID_ENUM:
                errMsg = "Invalid enum argument";
                break;
            case GL11.GL_INVALID_OPERATION:
                errMsg = "Invalid operation";
                break;
            case GL11.GL_INVALID_VALUE:
                errMsg = "Invalid numeric argument";
                break;
            case GL11.GL_OUT_OF_MEMORY:
                errMsg = "Out of memory";
                break;
            case EXTFramebufferObject.GL_INVALID_FRAMEBUFFER_OPERATION_EXT:
                errMsg = "Framebuffer is not complete";
                break;
            case GL11.GL_STACK_OVERFLOW:
                errMsg = "Internal stack overflow";
                break;
            case GL11.GL_STACK_UNDERFLOW:
                errMsg = "Internal stack underflow";
                break;
            default:
                errMsg = "Unknown";
                break;
        }
        return errMsg + " (Error Code: " + err + ")";
    }

    public static void checkError() {
        int err = GL11.glGetError();
        if (err != 0) {
            throw new RuntimeException("An OpenGL error occurred - " + decodeError(err));
        }
    }
}
