package mini.renderer.opengl;

import java.nio.IntBuffer;

/**
 * Framebuffer object functions.
 * <p>
 * Available by default in OpenGL ES 2, but on desktop GL 2
 * an extension is required.
 */
public interface GLFbo {

    int GL_COLOR_ATTACHMENT0_EXT = 0x8CE0;
    int GL_COLOR_ATTACHMENT1_EXT = 0x8CE1;
    int GL_COLOR_ATTACHMENT2_EXT = 0x8CE2;
    int GL_COLOR_ATTACHMENT3_EXT = 0x8CE3;
    int GL_COLOR_ATTACHMENT4_EXT = 0x8CE4;
    int GL_COLOR_ATTACHMENT5_EXT = 0x8CE5;
    int GL_COLOR_ATTACHMENT6_EXT = 0x8CE6;
    int GL_COLOR_ATTACHMENT7_EXT = 0x8CE7;
    int GL_COLOR_ATTACHMENT8_EXT = 0x8CE8;
    int GL_COLOR_ATTACHMENT9_EXT = 0x8CE9;
    int GL_COLOR_ATTACHMENT10_EXT = 0x8CEA;
    int GL_COLOR_ATTACHMENT11_EXT = 0x8CEB;
    int GL_COLOR_ATTACHMENT12_EXT = 0x8CEC;
    int GL_COLOR_ATTACHMENT13_EXT = 0x8CED;
    int GL_COLOR_ATTACHMENT14_EXT = 0x8CEE;
    int GL_COLOR_ATTACHMENT15_EXT = 0x8CEF;
    int GL_DEPTH_ATTACHMENT_EXT = 0x8D00;
    int GL_DRAW_FRAMEBUFFER_BINDING_EXT = 0x8CA6;
    int GL_DRAW_FRAMEBUFFER_EXT = 0x8CA9;
    int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT = 0x8CD1;
    int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE_EXT = 0x8CD0;
    int GL_FRAMEBUFFER_COMPLETE_EXT = 0x8CD5;
    int GL_FRAMEBUFFER_EXT = 0x8D40;
    int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT = 0x8CD6;
    int GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT = 0x8CD9;
    int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT = 0x8CDB;
    int GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT = 0x8CDA;
    int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT = 0x8CD7;
    int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_EXT = 0x8D56;
    int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT = 0x8CDC;
    int GL_FRAMEBUFFER_UNSUPPORTED_EXT = 0x8CDD;
    int GL_INVALID_FRAMEBUFFER_OPERATION_EXT = 0x506;
    int GL_MAX_COLOR_ATTACHMENTS_EXT = 0x8CDF;
    int GL_MAX_RENDERBUFFER_SIZE_EXT = 0x84E8;
    int GL_READ_FRAMEBUFFER_BINDING_EXT = 0x8CAA;
    int GL_READ_FRAMEBUFFER_EXT = 0x8CA8;
    int GL_RENDERBUFFER_EXT = 0x8D41;

    void glBindFramebufferEXT(int param1, int param2);

    void glBindRenderbufferEXT(int param1, int param2);

    void glBlitFramebufferEXT(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0,
                              int dstY0, int dstX1, int dstY1, int mask, int filter);

    int glCheckFramebufferStatusEXT(int param1);

    void glDeleteFramebuffersEXT(IntBuffer param1);

    void glDeleteRenderbuffersEXT(IntBuffer param1);

    void glFramebufferRenderbufferEXT(int param1, int param2, int param3, int param4);

    void glFramebufferTexture2DEXT(int param1, int param2, int param3, int param4,
                                   int param5);

    void glGenFramebuffersEXT(IntBuffer param1);

    void glGenRenderbuffersEXT(IntBuffer param1);

    void glGenerateMipmapEXT(int param1);

    void glRenderbufferStorageEXT(int param1, int param2, int param3, int param4);

    void glRenderbufferStorageMultisampleEXT(int target, int samples, int internalformat,
                                             int width, int height);
}
