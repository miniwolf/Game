package mini.water;

import mini.scene.VertexBuffer;
import mini.shaders.ShaderProgram;
import mini.utils.MyFile;

import java.io.IOException;

public class WaterShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("Engine/shaders", "waterVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("Engine/shaders", "waterFS.glsl");

    //protected UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
    //protected UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
    //protected UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");

    //protected UniformFloat moveFactor = new UniformFloat("moveFactor");
    //protected UniformVec3 cameraPosition = new UniformVec3("cameraPosition");
    //protected UniformVec3 lightDirection = new UniformVec3("lightDirection");

    //private UniformSampler reflectionTexture = new UniformSampler("reflectionTexture");
    //private UniformSampler refractionTexture = new UniformSampler("refractionTexture");
   // private UniformSampler dudvMap = new UniformSampler("dudvMap");
    //private UniformSampler normalMap = new UniformSampler("normalMap");
    //private UniformSampler depthMap = new UniformSampler("depthMap");

    public WaterShader() {
        try {
            addSource(ShaderType.Vertex, "Water Vertex", VERTEX_SHADER.getLines());
            addSource(ShaderType.Fragment, "Water Fragment", FRAGMENT_SHADER.getLines());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAttribute(VertexBuffer.Type.Position).setName("in_position");
//        super.storeAllUniformLocations(modelMatrix, viewMatrix, projectionMatrix, moveFactor,
//                                       cameraPosition, lightDirection, reflectionTexture,
//                                       refractionTexture,
//                                       dudvMap, normalMap, depthMap);
        connectTextureUnits();
    }

    private void connectTextureUnits() {
        super.start();
//        reflectionTexture.loadTexUnit(0);
//        refractionTexture.loadTexUnit(1);
//        dudvMap.loadTexUnit(2);
//        normalMap.loadTexUnit(3);
//        depthMap.loadTexUnit(4);
        super.stop();
    }
}
