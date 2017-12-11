package mini.renderer.opengl;

import java.nio.IntBuffer;

/**
 * GL functions only available on vanilla desktop OpenGL 3.0+.
 */
public interface GL3 extends GL2 {
    int GL_DEPTH_STENCIL_ATTACHMENT = 0x821A;
    int GL_GEOMETRY_SHADER = 0x8DD9;
    int GL_NUM_EXTENSIONS = 0x821D;
    int GL_R8 = 0x8229;
    int GL_R16F = 0x822D;
    int GL_R32F = 0x822E;
    int GL_RG16F = 0x822F;
    int GL_RG32F = 0x8230;
    int GL_RG = 0x8227;
    int GL_RG8 = 0x822B;
    int GL_TEXTURE_SWIZZLE_A = 0x8E45;
    int GL_TEXTURE_SWIZZLE_B = 0x8E44;
    int GL_TEXTURE_SWIZZLE_G = 0x8E43;
    int GL_TEXTURE_SWIZZLE_R = 0x8E42;
    int GL_R8I = 33329;
    int GL_R8UI = 33330;
    int GL_R16I = 33331;
    int GL_R16UI = 33332;
    int GL_R32I = 33333;
    int GL_R32UI = 33334;
    int GL_RG8I = 33335;
    int GL_RG8UI = 33336;
    int GL_RG16I = 33337;
    int GL_RG16UI = 33338;
    int GL_RG32I = 33339;
    int GL_RG32UI = 33340;
    int GL_RGBA32UI = 36208;
    int GL_RGB32UI = 36209;
    int GL_RGBA16UI = 36214;
    int GL_RGB16UI = 36215;
    int GL_RGBA8UI = 36220;
    int GL_RGB8UI = 36221;
    int GL_RGBA32I = 36226;
    int GL_RGB32I = 36227;
    int GL_RGBA16I = 36232;
    int GL_RGB16I = 36233;
    int GL_RGBA8I = 36238;
    int GL_RGB8I = 36239;
    int GL_RED_INTEGER = 36244;
    int GL_RG_INTEGER = 33320;
    int GL_RGB_INTEGER = 36248;
    int GL_RGBA_INTEGER = 36249;

    void glBindFragDataLocation(int param1, int param2, String param3); /// GL3+

    void glBindVertexArray(int param1); /// GL3+

    void glDeleteVertexArrays(IntBuffer arrays); /// GL3+

    void glFramebufferTextureLayer(int param1, int param2, int param3, int param4,
                                   int param5); /// GL3+

    void glGenVertexArrays(IntBuffer param1); /// GL3+

    String glGetString(int param1, int param2); /// GL3+
}
