package mini.renderer.opengl;

/**
 * GL functions only available on vanilla desktop OpenGL 4.0.
 */
public interface GL4 extends GL3 {
    int GL_TESS_CONTROL_SHADER = 0x8E88;
    int GL_TESS_EVALUATION_SHADER = 0x8E87;
    int GL_PATCHES = 0xE;

    void glPatchParameter(int count);
}
