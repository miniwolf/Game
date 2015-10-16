package terrain;

import model.RawModel;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import texture.TerrainTexturePack;
import tools.Maths;

import java.util.List;

/**
 * @author miniwolf
 */
public class TerrainRenderer {
    private TerrainShader shader;

    public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.connectTextureUnits();
        shader.stop();
    }

    private void prepareTerrain(Terrain terrain, boolean textured) {
        RawModel model = terrain.getModel();
        GL30.glBindVertexArray(model.getVAOID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        bindTextures(terrain, textured);
        shader.loadShineVariables(1, 0);
    }

    private void bindTextures(Terrain terrain, boolean textured) {
        TerrainTexturePack pack = terrain.getTexturePack();

        if ( textured ) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0); // TextureBank
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, pack.getBackgroundTexture().getTextureID());
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, pack.getrTexture().getTextureID());
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, pack.getgTexture().getTextureID());
            GL13.glActiveTexture(GL13.GL_TEXTURE3);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, pack.getbTexture().getTextureID());
            GL13.glActiveTexture(GL13.GL_TEXTURE4);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getBlendMap().getTextureID());
        }
    }

    private void unbindTerrain() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    private void loadModelMatrix(Terrain terrain) {
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(new Vector3f(terrain.getX(), 0, terrain.getZ()),
                0, 0, 0, // rotation terrain :p
                1);
        shader.loadTransformationMatrix(transformationMatrix);
    }

    public void render(List<Terrain> terrains, boolean textured) {
        for ( Terrain terrain : terrains ) {
            prepareTerrain(terrain, textured);
            loadModelMatrix(terrain);
            GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
            unbindTerrain();
        }
    }
}
