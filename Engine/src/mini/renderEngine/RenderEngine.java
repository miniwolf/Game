package mini.renderEngine;

import mini.entityRenderers.EntityRenderer;
import mini.environmentMapRenderer.EnvironMapRenderer;
import mini.gui.GuiRenderer;
import mini.math.Vector3f;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Scene;
import mini.shinyRenderer.ShinyRenderer;
import mini.skybox.SkyboxRenderer;
import mini.textures.Texture;
import mini.utils.DisplayManager;
import mini.water.WaterFrameBuffers;
import mini.water.WaterRenderer;

public class RenderEngine {
    private DisplayManager display;
    private MasterRenderer renderer;

    private RenderEngine(DisplayManager display, MasterRenderer renderer) {
        this.display = display;
        this.renderer = renderer;
    }

    public void update() {
        display.update();
    }

    public void renderScene(Scene scene) {
        renderer.renderScene(scene);
    }

    public void renderEnvironmentMap(Texture enviroMap, Scene scene, Vector3f center) {
        EnvironMapRenderer.renderEnvironmentMap(enviroMap, scene, center, renderer);
    }

    public void close() {
        renderer.cleanUp();
        display.closeDisplay();
    }

    public static RenderEngine init() {
        DisplayManager display = DisplayManager.createDisplay();
        EntityRenderer basicRenderer = new EntityRenderer();
        WaterFrameBuffers waterFbos = new WaterFrameBuffers();
        SkyboxRenderer skyRenderer = new SkyboxRenderer();
        WaterRenderer waterRenderer = new WaterRenderer(waterFbos);
        ShinyRenderer shinyRenderer = new ShinyRenderer();
        GuiRenderer guiRenderer = new GuiRenderer();
        GLRenderer glRenderer = new GLRenderer();
        MasterRenderer renderer = new MasterRenderer(basicRenderer, skyRenderer, waterRenderer,
                                                     waterFbos, shinyRenderer, guiRenderer,
                                                     glRenderer);
        glRenderer.initialize();
        return new RenderEngine(display, renderer);
    }

}
