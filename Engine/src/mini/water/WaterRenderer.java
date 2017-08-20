package mini.water;

import java.util.List;

import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.shaders.UniformBindingManager;
import mini.shaders.VarType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import mini.openglObjects.VAO;
import mini.textures.Texture;
import mini.utils.MyFile;
import mini.utils.OpenGlUtils;

public class WaterRenderer {
    private static final MyFile DUDV_MAP = new MyFile("Textures", "waterDUDV.png");
    private static final MyFile NORMAL_MAP = new MyFile("Textures", "normal.png");
    // private static final float WAVE_SPEED = 0.03f;

    private VAO quad;
    private WaterShader shader;
    private WaterFrameBuffers fbos;

    private float moveFactor = 0;

    private Texture dudvTexture;
    private Texture normalMap;

    public WaterRenderer(WaterFrameBuffers fbos) {
        this.shader = new WaterShader();
        this.fbos = fbos;
        this.quad = QuadGenerator.generateQuad();
//        this.normalMap = Texture.newTexture(NORMAL_MAP).create();
//        this.dudvTexture = Texture.newTexture(DUDV_MAP).anisotropic().create();
    }

    public void render(List<WaterTile> water, Camera camera, Vector3f lightDir,
                       UniformBindingManager manager) {
        prepareRender(camera, lightDir, manager);
        for (WaterTile tile : water) {
            Matrix4f modelMatrix = createModelMatrix(tile.getX(), tile.getHeight(), tile.getZ(),
                                                     WaterTile.TILE_SIZE);
            manager.setWorldMatrix(modelMatrix);
            manager.updateUniformBindings(shader);
            // TODO: Updating this is unnecessary as it is only the model matrix that is changed.
            GL11.glDrawElements(GL11.GL_TRIANGLES, quad.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
        }
        finish();
    }

    public void cleanUp() {
        quad.delete();
        //dudvTexture.delete();
        //normalMap.delete();
        fbos.cleanUp();
        //shader.cleanUp();
    }

    private void prepareRender(Camera camera, Vector3f lightDir, UniformBindingManager manager) {
        //shader.start();
//        manager.setCamera(camera);
//        manager.setLightDir(lightDir);

        moveFactor += 0.0005f;
        moveFactor %= 1;
        shader.getUniform("m_moveFactor").setValue(VarType.Float, moveFactor);

        quad.bind(0);
        bindTextures();
        doRenderSettings();
    }

    private void bindTextures() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getReflectionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionTexture());
        // TODO: Do this together with GLRenderer
        //dudvTexture.bindToUnit(2);
        //normalMap.bindToUnit(3);
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
    }

    private void doRenderSettings() {
        OpenGlUtils.enableDepthTesting(true);
        OpenGlUtils.antialias(false);
        OpenGlUtils.cullBackFaces(true);
        OpenGlUtils.enableAlphaBlending();
    }

    private void finish() {
        quad.unbind(0);
        //shader.stop();
    }

    private Matrix4f createModelMatrix(float x, float y, float z, float scale) {
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.setTranslation(new Vector3f(x, y, z));
        modelMatrix.scale(new Vector3f(scale, scale, scale));
        return modelMatrix;
    }
}
