package mini.asset;

import mini.shaders.plugins.GLSLLoader;
import org.junit.Assert;
import org.junit.Test;

public class LoadShaderSourceTest {

    @Test
    public void testLoadShaderSource() {
        String expected = "// -- begin import ShaderLib/GLSLCompat.glsllib --\n"
                          + "#if defined GL_ES\n"
                          + "#  define hfloat highp float\n"
                          + "#  define hvec2  highp vec2\n"
                          + "#  define hvec3  highp vec3\n"
                          + "#  define hvec4  highp vec4\n"
                          + "#  define lfloat lowp float\n"
                          + "#  define lvec2 lowp vec2\n"
                          + "#  define lvec3 lowp vec3\n"
                          + "#  define lvec4 lowp vec4\n"
                          + "#else\n"
                          + "#  define hfloat float\n"
                          + "#  define hvec2  vec2\n"
                          + "#  define hvec3  vec3\n"
                          + "#  define hvec4  vec4\n"
                          + "#  define lfloat float\n"
                          + "#  define lvec2  vec2\n"
                          + "#  define lvec3  vec3\n"
                          + "#  define lvec4  vec4\n"
                          + "#endif\n"
                          + "\n"
                          + "#if __VERSION__ >= 130\n"
                          + "out vec4 outFragColor;\n"
                          + "#  define texture1D texture\n"
                          + "#  define texture2D texture\n"
                          + "#  define texture3D texture\n"
                          + "#  define textureCube texture\n"
                          + "#  define texture2DLod textureLod\n"
                          + "#  define textureCubeLod textureLod\n"
                          + "#  if defined VERTEX_SHADER\n"
                          + "#    define varying out\n"
                          + "#    define attribute in\n"
                          + "#  elif defined FRAGMENT_SHADER\n"
                          + "#    define varying in\n"
                          + "#    define gl_FragColor outFragColor\n"
                          + "#  endif\n"
                          + "#endif\n"
                          + "// -- end import ShaderLib/GLSLCompat.glsllib --\n"
                          + "varying vec3 normal;\n"
                          + "\n"
                          + "void main(){\n"
                          + "   gl_FragColor = vec4((normal * vec3(0.5)) + vec3(0.5), 1.0);\n"
                          + "}\n";
        AssetManager assetManager = new AssetManager();
        assetManager.registerLoader(GLSLLoader.class, "frag");
        String showNormals = (String) assetManager.loadAsset("MatDefs/Misc/ShowNormals.frag");
        Assert.assertEquals(expected, showNormals);
    }
}
