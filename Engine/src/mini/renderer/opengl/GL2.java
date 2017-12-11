package mini.renderer.opengl;

import java.nio.ByteBuffer;

/**
 * GL functions only available on vanilla desktop OpenGL 2.
 */
public interface GL2 extends GL {
    int GL_ALPHA8 = 0x803C;
    int GL_ALPHA_TEST = 0xBC0;
    int GL_BGR = 0x80E0;
    int GL_BGRA = 0x80E1;
    int GL_COMPARE_REF_TO_TEXTURE = 0x884E;
    int GL_DEPTH_COMPONENT24 = 0x81A6;
    int GL_DEPTH_COMPONENT32 = 0x81A7;
    int GL_DEPTH_TEXTURE_MODE = 0x884B;
    int GL_DOUBLEBUFFER = 0xC32;
    int GL_DRAW_BUFFER = 0xC01;
    int GL_FILL = 0x1B02;
    int GL_GENERATE_MIPMAP = 0x8191;
    int GL_INTENSITY = 0x8049;
    int GL_LINE = 0x1B01;
    int GL_LUMINANCE8 = 0x8040;
    int GL_LUMINANCE8_ALPHA8 = 0x8045;
    int GL_MAX_ELEMENTS_INDICES = 0x80E9;
    int GL_MAX_ELEMENTS_VERTICES = 0x80E8;
    int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49;
    int GL_MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A;
    int GL_READ_BUFFER = 0xC02;
    int GL_RGB8 = 0x8051;
    int GL_STACK_OVERFLOW = 0x503;
    int GL_STACK_UNDERFLOW = 0x504;
    int GL_TEXTURE_3D = 0x806F;
    int GL_POINT_SPRITE = 0x8861;
    int GL_TEXTURE_COMPARE_FUNC = 0x884D;
    int GL_TEXTURE_COMPARE_MODE = 0x884C;
    int GL_TEXTURE_WRAP_R = 0x8072;
    int GL_VERTEX_PROGRAM_POINT_SIZE = 0x8642;
    int GL_UNSIGNED_INT_8_8_8_8 = 0x8035;

    void glAlphaFunc(int func, float ref);

    void glPointSize(float size);

    void glPolygonMode(int face, int mode);

    void glDrawBuffer(int mode);

    void glReadBuffer(int mode);

    void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height,
                                int depth, int border, ByteBuffer data);

    void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset,
                                   int width, int height, int depth, int format, ByteBuffer data);

    void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
                      int border, int format, int type, ByteBuffer data);

    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
                         int height, int depth, int format, int type, ByteBuffer data);
}
