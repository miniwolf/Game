package mini.shinyRenderer;

import java.util.List;

import mini.math.Vector3f;
import org.lwjgl.opengl.GL11;

import mini.openglObjects.VAO;
import mini.scene.Entity;
import mini.textures.Texture;
import mini.utils.Camera;
import mini.utils.OpenGlUtils;

public class ShinyRenderer {
    private ShinyShader shader;

    public ShinyRenderer() {
        this.shader = new ShinyShader();
    }

    public void render(List<Entity> shinyEntities, Texture environMap, Camera camera,
                       Vector3f lightDir) {
        prepare(camera, lightDir, environMap);
        for (Entity entity : shinyEntities) {
            entity.getSkin().getDiffuseTexture().bindToUnit(0);
            VAO model = entity.getModel().getVao();
            model.bind(0, 1, 2);
            GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
            model.unbind(0, 1, 2);
        }
        finish();
    }

    public void cleanUp() {
        shader.cleanUp();
    }

    private void prepare(Camera camera, Vector3f lightDir, Texture enviromap) {
        shader.start();
        shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
        shader.lightDirection.loadVec3(lightDir);
        shader.cameraPosition.loadVec3(camera.getPosition());
        enviromap.bindToUnit(1);
        OpenGlUtils.antialias(true);
        OpenGlUtils.disableBlending();
        OpenGlUtils.enableDepthTesting(true);
        OpenGlUtils.cullBackFaces(true);
    }

    private void finish() {
        shader.stop();
    }
}
