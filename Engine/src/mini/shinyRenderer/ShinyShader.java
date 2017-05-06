package mini.shinyRenderer;

import mini.scene.VertexBuffer;
import mini.shaders.ShaderProgram;
import mini.utils.MyFile;

import java.io.IOException;

public class ShinyShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("Engine/shaders", "shinyVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("Engine/shaders", "shinyFS.glsl");

    //protected UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
    //protected UniformVec3 cameraPosition = new UniformVec3("cameraPosition");
    //protected UniformVec3 lightDirection = new UniformVec3("lightDirection");

    //private UniformSampler diffuseMap = new UniformSampler("diffuseMap");
    //private UniformSampler enviroMap = new UniformSampler("enviroMap");

    public ShinyShader() {
        try {
            addSource(ShaderType.Vertex, "Shiny Vertex", VERTEX_SHADER.getLines());
            addSource(ShaderType.Fragment, "Shiny Fragment", FRAGMENT_SHADER.getLines());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAttribute(VertexBuffer.Type.Position).setName("in_position");
        getAttribute(VertexBuffer.Type.TexCoord).setName("in_textureCoords");
        getAttribute(VertexBuffer.Type.Normal).setName("in_normal");
        //super.storeAllUniformLocations(projectionViewMatrix, diffuseMap, cameraPosition, lightDirection, enviroMap);
        connectTextureUnits();
    }

    private void connectTextureUnits() {
        super.start();
        //diffuseMap.loadTexUnit(0);
        //enviroMap.loadTexUnit(1);
        super.stop();
    }

}
