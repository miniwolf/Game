package mini.gui;

import mini.math.Matrix3f;
import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.openglObjects.VAO;
import mini.textures.GUITexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.List;

/**
 * Created by miniwolf on 17-02-2017.
 */
public class GuiRenderer {
    private final GuiShader shader;

    public GuiRenderer() {
        this.shader = new GuiShader();
    }

    public void render(List<GUITexture> guiTextures) {
        init();
        for (GUITexture guiTexture : guiTextures) {
            VAO model = guiTexture.getModel().getVao();
            model.bind(0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, guiTexture.getTextureId());
            Matrix4f transformationMatrix = new Matrix4f(); // TODO: Create this inside Gui Texture
            transformationMatrix.setScale(guiTexture.getScale().x, guiTexture.getScale().y, 1);
            transformationMatrix.setTranslation(guiTexture.getPosition().x,
                                                guiTexture.getPosition().y, 0);
            shader.transformationMatrix.loadMatrix(transformationMatrix);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
            model.unbind(0);
        }
        end();
    }

    private void init() {
        shader.start();
    }

    private void end() {
        shader.stop();
    }

    public void cleanUp() {
        shader.cleanUp();
    }
}