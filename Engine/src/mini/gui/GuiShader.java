package mini.gui;

import mini.scene.VertexBuffer;
import mini.shaders.ShaderProgram;
import mini.shaders.UniformBinding;
import mini.utils.MyFile;

import java.io.IOException;

/**
 * Created by miniwolf on 17-02-2017.
 */
public class GuiShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("Engine/shaders", "guiVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("Engine/shaders", "guiFS.glsl");

    //protected UniformMatrix transformationMatrix = new UniformMatrix("transformationMatrix");

    public GuiShader() {
        try {
            addSource(ShaderType.Vertex, "Gui Vertex", VERTEX_SHADER.getLines());
            addSource(ShaderType.Fragment, "Gui Fragment", FRAGMENT_SHADER.getLines());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAttribute(VertexBuffer.Type.Position).setName("in_position");
        addUniformBinding(UniformBinding.WorldMatrix);
    }
}
