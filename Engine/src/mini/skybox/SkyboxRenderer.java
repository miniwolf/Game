package mini.skybox;

import org.lwjgl.opengl.GL11;

import mini.openglObjects.VAO;
import mini.utils.Camera;
import mini.utils.OpenGlUtils;

public class SkyboxRenderer {
    private SkyboxShader shader;

    public SkyboxRenderer() {
        this.shader = new SkyboxShader();
    }

    public void render(Skybox skybox, Camera camera) {
        prepare(skybox, camera);
        VAO model = skybox.getCubeVao();
        model.bind(0);
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
        model.unbind(0);
        finish();
    }

    public void cleanUp() {
        shader.cleanUp();
    }

    private void prepare(Skybox skybox, Camera camera) {
        shader.start();
        shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
        skybox.getTexture().bindToUnit(0);
        OpenGlUtils.disableBlending();
        OpenGlUtils.enableDepthTesting(true);
        OpenGlUtils.cullBackFaces(true);
        OpenGlUtils.antialias(false);
    }

    private void finish() {
        shader.stop();
    }
}
