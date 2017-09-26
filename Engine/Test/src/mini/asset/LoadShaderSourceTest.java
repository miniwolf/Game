package mini.asset;

import mini.shaders.plugins.GLSLLoader;
import org.junit.Test;

import java.io.IOException;

public class LoadShaderSourceTest {

    @Test
    public void testLoadShaderSource() {
        try {
            String showNormals = (String) GLSLLoader.load(new ShaderNodeDefinitionKey("MatDefs/Misc/ShowNormals.frag"));
            System.out.println(showNormals);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
