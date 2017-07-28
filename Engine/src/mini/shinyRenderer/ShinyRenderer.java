package mini.shinyRenderer;

import java.util.List;

import mini.math.Vector3f;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.shaders.UniformBindingManager;

import mini.textures.Texture;
import mini.utils.Camera;
import mini.utils.OpenGlUtils;

public class ShinyRenderer {
    private ShinyShader shader;

    public ShinyRenderer() {
        this.shader = new ShinyShader();
    }

    public void render(List<Node> shinyEntities, Texture environMap, Camera camera,
                       Vector3f lightDir, GLRenderer renderer, UniformBindingManager manager) {
        prepare(camera, lightDir, environMap, renderer, manager);
        for (Node entity : shinyEntities) {
            for (Spatial spatial : entity.getChildren()) {
                if (spatial instanceof Geometry) {
                    renderer.renderMeshFromGeometry((Geometry) spatial);
//                    Geometry geom = (Geometry) spatial;
//                    geom.getMaterial().getDiffuseTexture().bindToUnit(0);
//                    VAO model = geom.getVao();
//                    model.bind(0, 1, 2);
//                    GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
//                    model.unbind(0, 1, 2);
                }
            }
        }
        finish();
    }

    public void cleanUp() {
        //shader.cleanUp();
    }

    private void prepare(Camera camera, Vector3f lightDir, Texture enviromap,
                         GLRenderer renderer, UniformBindingManager manager) {
        //shader.start();
        manager.setCamera(camera);
        manager.setLightDir(lightDir);
        manager.updateUniformBindings(shader);
        renderer.setTexture(1, enviromap);
        OpenGlUtils.antialias(true);
        OpenGlUtils.disableBlending();
        OpenGlUtils.enableDepthTesting(true);
        OpenGlUtils.cullBackFaces(true);
        renderer.setShader(shader);
    }

    private void finish() {
        //shader.stop();
    }
}
