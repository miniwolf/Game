package mini.entityRenderers;

import mini.scene.VertexBuffer;
import mini.shaders.ShaderProgram;
import mini.shaders.UniformBinding;
import mini.utils.MyFile;

import java.io.IOException;

public class EntityShader extends ShaderProgram {
    private static final MyFile VERTEX_SHADER = new MyFile("Engine/shaders", "entityVS.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("Engine/shaders", "entityFS.glsl");

    //protected UniformBoolean hasExtraMap = new UniformBoolean("hasExtraMap");
    //protected UniformVec4 plane = new UniformVec4("plane");

    //private UniformSampler diffuseMap = new UniformSampler("diffuseMap");
    //private UniformSampler extraMap = new UniformSampler("extraMap");

    public EntityShader() {
        try {
            addSource(ShaderType.Vertex, "Entity Vertex", VERTEX_SHADER.getLines());
            addSource(ShaderType.Fragment, "Entity Fragment", FRAGMENT_SHADER.getLines());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAttribute(VertexBuffer.Type.Position).setName("in_position");
        getAttribute(VertexBuffer.Type.TexCoord).setName("in_textureCoords");
        getAttribute(VertexBuffer.Type.Normal).setName("in_normal");
        //super.storeAllUniformLocations(projectionViewMatrix, diffuseMap, extraMap, hasExtraMap,
//                                       lightDirection, plane);
        addUniformBinding(UniformBinding.ViewProjectionMatrix);
        addUniformBinding(UniformBinding.LightDirection);
        connectTextureUnits();
    }

    private void connectTextureUnits() {
        super.start();
        //diffuseMap.loadTexUnit(0);
        //extraMap.loadTexUnit(1);
        super.stop();
    }
}
