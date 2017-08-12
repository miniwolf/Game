package mini.skybox;

import mini.renderEngine.Camera;
import mini.shaders.UniformBindingManager;
import org.lwjgl.opengl.GL11;

import mini.openglObjects.VAO;
import mini.utils.OpenGlUtils;

public class SkyboxRenderer {
    private SkyboxShader shader;

    public SkyboxRenderer() {
        this.shader = new SkyboxShader();
    }

    public void render(Skybox skybox, Camera camera, UniformBindingManager manager) {
        prepare(skybox, camera, manager);
        VAO model = skybox.getCubeVao();
        model.bind(0);
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
        model.unbind(0);
        finish();
    }

    public void cleanUp() {
        //shader.cleanUp();
    }

    private void prepare(Skybox skybox, Camera camera, UniformBindingManager manager) {
        //shader.start();
        manager.setCamera(camera);
        manager.updateUniformBindings(shader);
        // TODO: Use GLREnderer for this
        //skybox.getTexture().bindToUnit(0);
        OpenGlUtils.disableBlending();
        OpenGlUtils.enableDepthTesting(true);
        OpenGlUtils.cullBackFaces(true);
        OpenGlUtils.antialias(false);
    }

    private void finish() {
       // shader.stop();
    }
}
