package mini.renderEngine;

import mini.entityRenderers.EntityRenderer;
import mini.gui.GuiRenderer;
import mini.math.Vector4f;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Scene;
import mini.shaders.UniformBindingManager;
import mini.shinyRenderer.ShinyRenderer;
import mini.skybox.SkyboxRenderer;
import mini.textures.GUITexture;
import mini.utils.Camera;
import mini.water.WaterFrameBuffers;
import mini.water.WaterRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class MasterRenderer {
    private static final Vector4f NO_CLIP = new Vector4f(0, 0, 0, 1);

    private EntityRenderer entityRenderer;
    private ShinyRenderer shinyRenderer;
    private SkyboxRenderer skyRenderer;
    private WaterRenderer waterRenderer;
    private WaterFrameBuffers waterFbos;
    private GuiRenderer guiRenderer;
    private RenderManager renderManager;
    private UniformBindingManager uniformBindingManager;

    protected MasterRenderer(EntityRenderer entityRenderer, SkyboxRenderer skyRenderer,
                             WaterRenderer waterRenderer, WaterFrameBuffers waterFbos,
                             ShinyRenderer shinyRenderer, GuiRenderer guiRenderer,
                             GLRenderer glRenderer) {
        this.entityRenderer = entityRenderer;
        this.skyRenderer = skyRenderer;
        this.waterRenderer = waterRenderer;
        this.waterFbos = waterFbos;
        this.shinyRenderer = shinyRenderer;
        this.guiRenderer = guiRenderer;
        renderManager = new RenderManager(glRenderer);
    }

    public void renderLowQualityScene(Scene scene, Camera cubeMapCamera) {
        prepare();
        entityRenderer.render(scene.getImportantEntities(), cubeMapCamera,
                              scene.getLightDirection(), NO_CLIP, renderManager);
        // TODO: This should be fixed using GLREnderer
        //skyRenderer.render(scene.getSky(), cubeMapCamera, uniformBindingManager);
    }

    protected void renderScene(Scene scene) {
        GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
        //renderWaterRefractionPass(scene);
        //renderWaterReflectionPass(scene);
        GL11.glDisable(GL30.GL_CLIP_DISTANCE0);

        renderMainPass(scene);
    }

    protected void cleanUp() {
        entityRenderer.cleanUp();
        skyRenderer.cleanUp();
        waterRenderer.cleanUp();
        shinyRenderer.cleanUp();
        guiRenderer.cleanUp();
    }

    private void prepare() {
        GL11.glClearColor(1, 1, 1, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    private void renderWaterReflectionPass(Scene scene) {
        waterFbos.bindReflectionFrameBuffer();
        prepare();
        scene.getCamera().reflect(scene.getWaterHeight());
        entityRenderer.render(scene.getReflectedEntities(), scene.getCamera(),
                              scene.getLightDirection(), new Vector4f(0, 1, 0, 0.1f),
                              renderManager);
        skyRenderer.render(scene.getSky(), scene.getCamera(), uniformBindingManager);
        waterFbos.unbindCurrentFrameBuffer();
        scene.getCamera().reflect(scene.getWaterHeight());
    }

    private void renderWaterRefractionPass(Scene scene) {
        waterFbos.bindRefractionFrameBuffer();
        prepare();
        entityRenderer.render(scene.getUnderwaterEntities(), scene.getCamera(),
                              scene.getLightDirection(), new Vector4f(0, -1, 0, 0), renderManager);
        waterFbos.unbindCurrentFrameBuffer();
    }

    private void renderMainPass(Scene scene) {
        prepare();
        entityRenderer.render(scene.getAllEntities(), scene.getCamera(), scene.getLightDirection(),
                              NO_CLIP, renderManager);
        // TODO: Implement render texture and framebuffers in GLRenderer
        //shinyRenderer.render(scene.getShinyEntities(), scene.getEnvironmentMap(), scene.getCamera(),
        //                     scene.getLightDirection(), glRenderer, uniformBindingManager);
        //skyRenderer.render(scene.getSky(), scene.getCamera(), uniformBindingManager);
        // TODO: Fix this with GLRenderer
        //waterRenderer.render(scene.getWater(), scene.getCamera(), scene.getLightDirection()
//                , uniformBindingManager);
        //guiRenderer.render(uniformBindingManager, scene.getGUI());
    }
}
