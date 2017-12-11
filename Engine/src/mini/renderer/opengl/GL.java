package mini.renderer.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Baseline GL methods that must be available on all platforms.
 * <p>
 * This is the subset of vanilla desktop OpenGL 2 and OpenGL ES 2.
 */
public interface GL {
    int GL_ALPHA = 0x1906;
    int GL_ALWAYS = 0x207;
    int GL_ARRAY_BUFFER = 0x8892;
    int GL_BACK = 0x405;
    int GL_BLEND = 0xBE2;
    int GL_BYTE = 0x1400;
    int GL_CLAMP_TO_EDGE = 0x812F;
    int GL_COLOR_BUFFER_BIT = 0x4000;
    int GL_COMPILE_STATUS = 0x8B81;
    int GL_CULL_FACE = 0xB44;
    int GL_DECR = 0x1E03;
    int GL_DECR_WRAP = 0x8508;
    int GL_DEPTH_BUFFER_BIT = 0x100;
    int GL_DEPTH_COMPONENT = 0x1902;
    int GL_DEPTH_COMPONENT16 = 0x81A5;
    int GL_DEPTH_TEST = 0xB71;
    int GL_DOUBLE = 0x140A;
    int GL_DST_ALPHA = 0x0304;
    int GL_DST_COLOR = 0x306;
    int GL_DYNAMIC_DRAW = 0x88E8;
    int GL_ELEMENT_ARRAY_BUFFER = 0x8893;
    int GL_EQUAL = 0x202;
    int GL_EXTENSIONS = 0x1F03;
    int GL_FALSE = 0x0;
    int GL_FLOAT = 0x1406;
    int GL_FRAGMENT_SHADER = 0x8B30;
    int GL_FRONT = 0x404;
    int GL_FUNC_ADD = 0x8006;
    int GL_FUNC_SUBTRACT = 0x800A;
    int GL_FUNC_REVERSE_SUBTRACT = 0x800B;
    int GL_FRONT_AND_BACK = 0x408;
    int GL_GEQUAL = 0x206;
    int GL_GREATER = 0x204;
    int GL_GREEN = 0x1904;
    int GL_INCR = 0x1E02;
    int GL_INCR_WRAP = 0x8507;
    int GL_INFO_LOG_LENGTH = 0x8B84;
    int GL_INT = 0x1404;
    int GL_INVALID_ENUM = 0x500;
    int GL_INVALID_VALUE = 0x501;
    int GL_INVALID_OPERATION = 0x502;
    int GL_INVERT = 0x150A;
    int GL_KEEP = 0x1E00;
    int GL_LEQUAL = 0x203;
    int GL_LESS = 0x201;
    int GL_LINEAR = 0x2601;
    int GL_LINEAR_MIPMAP_LINEAR = 0x2703;
    int GL_LINEAR_MIPMAP_NEAREST = 0x2701;
    int GL_LINES = 0x1;
    int GL_LINE_LOOP = 0x2;
    int GL_LINE_STRIP = 0x3;
    int GL_LINK_STATUS = 0x8B82;
    int GL_LUMINANCE = 0x1909;
    int GL_LUMINANCE_ALPHA = 0x190A;
    int GL_MAX = 0x8008;
    int GL_MAX_CUBE_MAP_TEXTURE_SIZE = 0x851C;
    int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49;
    int GL_MAX_FRAGMENT_UNIFORM_VECTORS = 0x8DFD;
    int GL_MAX_TEXTURE_IMAGE_UNITS = 0x8872;
    int GL_MAX_TEXTURE_SIZE = 0xD33;
    int GL_MAX_VERTEX_ATTRIBS = 0x8869;
    int GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS = 0x8B4C;
    int GL_MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A;
    int GL_MAX_VERTEX_UNIFORM_VECTORS = 0x8DFB;
    int GL_MIRRORED_REPEAT = 0x8370;
    int GL_MIN = 0x8007;
    int GL_NEAREST = 0x2600;
    int GL_NEAREST_MIPMAP_LINEAR = 0x2702;
    int GL_NEAREST_MIPMAP_NEAREST = 0x2700;
    int GL_NEVER = 0x200;
    int GL_NO_ERROR = 0x0;
    int GL_NONE = 0x0;
    int GL_NOTEQUAL = 0x205;
    int GL_ONE = 0x1;
    int GL_ONE_MINUS_DST_ALPHA = 0x0305;
    int GL_ONE_MINUS_DST_COLOR = 0x307;
    int GL_ONE_MINUS_SRC_ALPHA = 0x303;
    int GL_ONE_MINUS_SRC_COLOR = 0x301;
    int GL_OUT_OF_MEMORY = 0x505;
    int GL_POINTS = 0x0;
    int GL_POLYGON_OFFSET_FILL = 0x8037;
    int GL_QUERY_RESULT = 0x8866;
    int GL_QUERY_RESULT_AVAILABLE = 0x8867;
    int GL_RED = 0x1903;
    int GL_RENDERER = 0x1F01;
    int GL_REPEAT = 0x2901;
    int GL_REPLACE = 0x1E01;
    int GL_RGB = 0x1907;
    int GL_RGB565 = 0x8D62;
    int GL_RGB5_A1 = 0x8057;
    int GL_RGBA = 0x1908;
    int GL_RGBA4 = 0x8056;
    int GL_SCISSOR_TEST = 0xC11;
    int GL_SHADING_LANGUAGE_VERSION = 0x8B8C;
    int GL_SHORT = 0x1402;
    int GL_SRC_ALPHA = 0x302;
    int GL_SRC_ALPHA_SATURATE = 0x0308;
    int GL_SRC_COLOR = 0x300;
    int GL_STATIC_DRAW = 0x88E4;
    int GL_STENCIL_BUFFER_BIT = 0x400;
    int GL_STENCIL_TEST = 0xB90;
    int GL_STREAM_DRAW = 0x88E0;
    int GL_STREAM_READ = 0x88E1;
    int GL_TEXTURE = 0x1702;
    int GL_TEXTURE0 = 0x84C0;
    int GL_TEXTURE1 = 0x84C1;
    int GL_TEXTURE2 = 0x84C2;
    int GL_TEXTURE3 = 0x84C3;
    int GL_TEXTURE4 = 0x84C4;
    int GL_TEXTURE5 = 0x84C5;
    int GL_TEXTURE6 = 0x84C6;
    int GL_TEXTURE7 = 0x84C7;
    int GL_TEXTURE8 = 0x84C8;
    int GL_TEXTURE9 = 0x84C9;
    int GL_TEXTURE10 = 0x84CA;
    int GL_TEXTURE11 = 0x84CB;
    int GL_TEXTURE12 = 0x84CC;
    int GL_TEXTURE13 = 0x84CD;
    int GL_TEXTURE14 = 0x84CE;
    int GL_TEXTURE15 = 0x84CF;
    int GL_TEXTURE_2D = 0xDE1;
    int GL_TEXTURE_CUBE_MAP = 0x8513;
    int GL_TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515;
    int GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516;
    int GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517;
    int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518;
    int GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519;
    int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
    int GL_TEXTURE_BASE_LEVEL = 0x813C;
    int GL_TEXTURE_MAG_FILTER = 0x2800;
    int GL_TEXTURE_MAX_LEVEL = 0x813D;
    int GL_TEXTURE_MIN_FILTER = 0x2801;
    int GL_TEXTURE_WRAP_S = 0x2802;
    int GL_TEXTURE_WRAP_T = 0x2803;
    int GL_TIME_ELAPSED = 0x88BF;
    int GL_TRIANGLES = 0x4;
    int GL_TRIANGLE_FAN = 0x6;
    int GL_TRIANGLE_STRIP = 0x5;
    int GL_TRUE = 0x1;
    int GL_UNPACK_ALIGNMENT = 0xCF5;
    int GL_UNSIGNED_BYTE = 0x1401;
    int GL_UNSIGNED_INT = 0x1405;
    int GL_UNSIGNED_SHORT = 0x1403;
    int GL_UNSIGNED_SHORT_5_6_5 = 0x8363;
    int GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034;
    int GL_VENDOR = 0x1F00;
    int GL_VERSION = 0x1F02;
    int GL_VERTEX_SHADER = 0x8B31;
    int GL_ZERO = 0x0;

    void resetStats();

    void glActiveTexture(int texture);

    void glAttachShader(int program, int shader);

    void glBeginQuery(int target, int query);

    void glBindBuffer(int target, int buffer);

    void glBindTexture(int target, int texture);

    void glBlendEquationSeparate(int colorMode, int alphaMode);

    void glBlendFunc(int sfactor, int dfactor);

    void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha);

    void glBufferData(int target, long data_size, int usage);

    void glBufferData(int target, FloatBuffer data, int usage);

    void glBufferData(int target, ShortBuffer data, int usage);

    void glBufferData(int target, ByteBuffer data, int usage);

    void glBufferSubData(int target, long offset, FloatBuffer data);

    void glBufferSubData(int target, long offset, ShortBuffer data);

    void glBufferSubData(int target, long offset, ByteBuffer data);

    void glClear(int mask);

    void glClearColor(float red, float green, float blue, float alpha);

    void glColorMask(boolean red, boolean green, boolean blue, boolean alpha);

    void glCompileShader(int shader);

    void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height,
                                int border, ByteBuffer data);

    void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width,
                                   int height, int format, ByteBuffer data);

    int glCreateProgram();

    int glCreateShader(int shaderType);

    void glCullFace(int mode);

    void glDeleteBuffers(IntBuffer buffers);

    void glDeleteProgram(int program);

    void glDeleteShader(int shader);

    void glDeleteTextures(IntBuffer textures);

    void glDepthFunc(int func);

    void glDepthMask(boolean flag);

    void glDepthRange(double nearVal, double farVal);

    void glDetachShader(int program, int shader);

    void glDisable(int cap);

    void glDisableVertexAttribArray(int index);

    void glDrawArrays(int mode, int first, int count);

    void glDrawRangeElements(int mode, int start, int end, int count, int type,
                             long indices); /// GL2+

    void glEnable(int cap);

    void glEnableVertexAttribArray(int index);

    void glEndQuery(int target);

    void glGenBuffers(IntBuffer buffers);

    void glGenTextures(IntBuffer textures);

    void glGenQueries(int number, IntBuffer ids);

    int glGetAttribLocation(int program, String name);

    void glGetBoolean(int pname, ByteBuffer params);

    void glGetBufferSubData(int target, long offset, ByteBuffer data);

    int glGetError();

    void glGetInteger(int pname, IntBuffer params);

    void glGetProgram(int program, int pname, IntBuffer params);

    String glGetProgramInfoLog(int program, int maxSize);

    long glGetQueryObjectui64(int query, int pname);

    int glGetQueryObjectiv(int query, int pname);

    void glGetShader(int shader, int pname, IntBuffer params);

    String glGetShaderInfoLog(int shader, int maxSize);

    String glGetString(int name);

    int glGetUniformLocation(int program, String name);

    boolean glIsEnabled(int cap);

    void glLineWidth(float width);

    void glLinkProgram(int program);

    void glPixelStorei(int pname, int param);

    void glPolygonOffset(float factor, float units);

    void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer data);

    void glReadPixels(int x, int y, int width, int height, int format, int type, long offset);

    void glScissor(int x, int y, int width, int height);

    void glShaderSource(int shader, String[] string, IntBuffer length);

    void glStencilFuncSeparate(int face, int func, int ref, int mask);

    void glStencilOpSeparate(int face, int sfail, int dpfail, int dppass);

    void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border,
                      int format, int type, ByteBuffer data);

    void glTexParameterf(int target, int pname, float param);

    void glTexParameteri(int target, int pname, int param);

    void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
                         int format, int type, ByteBuffer data);

    void glUniform1(int location, FloatBuffer value);

    void glUniform1(int location, IntBuffer value);

    void glUniform1f(int location, float v0);

    void glUniform1i(int location, int v0);

    void glUniform2(int location, IntBuffer value);

    void glUniform2(int location, FloatBuffer value);

    void glUniform2f(int location, float v0, float v1);

    void glUniform3(int location, IntBuffer value);

    void glUniform3(int location, FloatBuffer value);

    void glUniform3f(int location, float v0, float v1, float v2);

    void glUniform4(int location, FloatBuffer value);

    void glUniform4(int location, IntBuffer value);

    void glUniform4f(int location, float v0, float v1, float v2, float v3);

    void glUniformMatrix3(int location, boolean transpose, FloatBuffer value);

    void glUniformMatrix4(int location, boolean transpose, FloatBuffer value);

    void glUseProgram(int program);

    void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride,
                               long pointer);

    void glViewport(int x, int y, int width, int height);
}
