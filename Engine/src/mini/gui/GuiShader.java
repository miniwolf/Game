package mini.gui;

import mini.shaders.ShaderProgram;
import mini.utils.MyFile;

/**
 * Created by miniwolf on 17-02-2017.
 */
public class GuiShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("Engine/shaders", "guiVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("Engine/shaders", "guiFS.glsl");

    //protected UniformMatrix transformationMatrix = new UniformMatrix("transformationMatrix");

    public GuiShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position");
        //super.storeAllUniformLocations(transformationMatrix);
    }
}
