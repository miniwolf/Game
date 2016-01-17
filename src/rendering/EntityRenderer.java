package rendering;

import entities.Entity;
import model.RawModel;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import shaders.Shader;
import texture.ModelTexture;
import model.TexturedModel;
import tools.Maths;

import java.util.List;
import java.util.Map;

/**
 * @author miniwolf
 */
public class EntityRenderer {
    private Shader shader;

    public EntityRenderer(Shader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    private void prepareTextureModel(TexturedModel texturedModel, boolean textured) {
        RawModel model = texturedModel.getRawModel();
        GL30.glBindVertexArray(model.getVAOID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        ModelTexture texture = texturedModel.getTexture();
        if ( texture.isHasTransparency() ) {
            Renderer.disableCulling();
        }
        shader.loadFakeLightingVariable(texture.isUseFakeLighting());
        shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());

        GL13.glActiveTexture(GL13.GL_TEXTURE0); // TextureBank
        if ( textured ) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturedModel.getTexture().getTextureID());
        }
    }

    private void unbindTexturedModel() {
        Renderer.enableCulling();
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    private void prepareInstance(Entity entity) {
        Matrix4f modelMatrix = Maths.createModelMatrix(entity.getPosition(),
                                                       entity.getRotX(), entity.getRotY(), entity.getRotZ(),
                                                       entity.getScale());
        shader.loadModelMatrix(modelMatrix);
    }

    void render(Map<TexturedModel, List<Entity>> entities, boolean textured) {
        for ( TexturedModel model : entities.keySet() ) {
            prepareTextureModel(model, textured);
            List<Entity> batch = entities.get(model);
            for ( Entity entity : batch ) {
                prepareInstance(entity);
                GL11.glDrawElements(entity.getMode(), model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
            }
            unbindTexturedModel();
        }
    }
}
