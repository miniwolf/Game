package mini.skybox;

import mini.shaders.ShaderProgram;
import mini.utils.MyFile;

public class SkyboxShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("Engine/shaders", "skyboxVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("Engine/shaders", "skyboxFS.glsl");

    //protected UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");

    public SkyboxShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position");
        //super.storeAllUniformLocations(projectionViewMatrix);
    }
}
