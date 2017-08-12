package mini.skybox;

import mini.scene.VertexBuffer;
import mini.shaders.ShaderProgram;
import mini.shaders.UniformBinding;
import mini.utils.MyFile;

import java.io.IOException;

public class SkyboxShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("shaders", "skyboxVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("shaders", "skyboxFS.glsl");

//    public SkyboxShader() {
//        try {
//            addSource(ShaderType.Vertex, "Skybox Vertex", VERTEX_SHADER.getLines());
//            addSource(ShaderType.Fragment, "Skybox Fragment", FRAGMENT_SHADER.getLines());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        getAttribute(VertexBuffer.Type.Position).setName("in_position");
//        addUniformBinding(UniformBinding.ViewProjectionMatrix);
//    }
}
