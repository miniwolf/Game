package mini.shinyRenderer;

import mini.shaders.ShaderProgram;
import mini.shaders.UniformMatrix;
import mini.shaders.UniformSampler;
import mini.shaders.UniformVec3;
import mini.utils.MyFile;

public class ShinyShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("Engine/shaders", "shinyVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("Engine/shaders", "shinyFS.glsl");

    protected UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
    protected UniformVec3 cameraPosition = new UniformVec3("cameraPosition");
    protected UniformVec3 lightDirection = new UniformVec3("lightDirection");

    private UniformSampler diffuseMap = new UniformSampler("diffuseMap");
    private UniformSampler enviroMap = new UniformSampler("enviroMap");

    public ShinyShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normal");
        super.storeAllUniformLocations(projectionViewMatrix, diffuseMap, cameraPosition, lightDirection, enviroMap);
        connectTextureUnits();
    }

    private void connectTextureUnits() {
        super.start();
        diffuseMap.loadTexUnit(0);
        enviroMap.loadTexUnit(1);
        super.stop();
    }

}
