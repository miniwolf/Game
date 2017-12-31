package mini.voxelization;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.post.SceneProcessor;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.shape.Box;
import mini.scene.shape.Quad;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture2D;
import mini.textures.Texture3D;
import mini.textures.image.ColorSpace;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VoxelizationRenderer implements SceneProcessor {
    private static final int voxelTextureSize = 64; // divisible by 2

    private RenderManager renderManager;
    private Material worldPositionMaterial;
    private Material voxelVisualizationMaterial;
    private Material voxelizationMaterial;
    private ViewPort viewPort;
    private RenderState forcedRenderState = new RenderState();
    private FrameBuffer frontFB, backFB;
    private Texture2D frontDepthTex, backDepthTex;
    private Texture3D voxelTexture;
    private AssetManager assetManager;
    private Geometry cube;
    private Geometry quad;
    private boolean voxelizationQueued = true;
    private boolean automaticallyVoxelize = true;
    private int voxelizationSparsity = 1;
    private int ticksSinceLastVoxelization = voxelizationSparsity;

    public VoxelizationRenderer(AssetManager assetManager) {
        this.assetManager = assetManager;
        init(assetManager);
    }

    private void init(AssetManager assetManager) {
        worldPositionMaterial = new Material(assetManager, "MatDefs/Voxelization/Visualization/WorldPosShader.minid");
        voxelVisualizationMaterial = new Material(assetManager, "MatDefs/Voxelization/Visualization/VoxelVisual.minid");
        voxelizationMaterial = new Material(assetManager, "MatDefs/Light/Lighting.minid");

        cube = makeCube();
        Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        material.setColor("Color", ColorRGBA.Red);
        cube.setMaterial(material);
        quad = makeQuad();
    }

    private Geometry makeCube() {
        Box box = new Box(1, 1, 1);
        return new Geometry("Cube", box);
    }

    private Geometry makeQuad() {
        Quad quad = new Quad(1, 1);
        return new Geometry("Quad", quad);
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        viewPort = vp;
        Camera camera = vp.getCamera();
        frontFB = new FrameBuffer(camera.getWidth(), camera.getHeight(), 1);
        frontDepthTex = new Texture2D(camera.getWidth(), camera.getHeight(), Image.Format.Depth);
        frontFB.setDepthTexture(frontDepthTex);

        backFB = new FrameBuffer(camera.getWidth(), camera.getHeight(), 1);
        backDepthTex = new Texture2D(camera.getWidth(), camera.getHeight(), Image.Format.Depth);
        backFB.setDepthTexture(backDepthTex);

        voxelTexture = initVoxelization(voxelTextureSize);

        voxelVisualizationMaterial.setTexture("textureBack", backDepthTex);
        voxelVisualizationMaterial.setTexture("textureFront", frontDepthTex);
        voxelVisualizationMaterial.setTexture("texture3D", voxelTexture);

        voxelizationMaterial.selectTechnique("Voxelization", renderManager);
    }

    private Texture3D initVoxelization(int voxelTextureSize) {
        List<ByteBuffer> data = new ArrayList<>(1);
        //all data must be inside one buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * voxelTextureSize * voxelTextureSize * voxelTextureSize);
        data.add(bb);
        return new Texture3D(new Image(Image.Format.RGBA8, voxelTextureSize, voxelTextureSize, voxelTextureSize, data, null, ColorSpace.Linear));
    }

    @Override
    public boolean isInitialized() {
        return viewPort != null;
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue rq) {
        boolean voxelizeNow = voxelizationQueued || (automaticallyVoxelize && voxelizationSparsity > 0 && ++ticksSinceLastVoxelization >= voxelizationSparsity);
        if (voxelizeNow) {
            voxelize();
            ticksSinceLastVoxelization = 0;
            voxelizationQueued = false;
        }

        renderScene();
        //renderVisualization();

        renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());
        renderManager.setForcedMaterial(null);
        renderManager.setForcedRenderState(null);
    }

    private void voxelize() {
        voxelTexture.getImage().getData(0).clear();

        RenderState voxelizationState = new RenderState();
        voxelizationState.setColorWrite(false);
        voxelizationState.setFaceCullMode(RenderState.FaceCullMode.Off);
        voxelizationState.setDepthWrite(false);
        voxelizationState.setDepthTest(false);
        voxelizationState.setBlendMode(RenderState.BlendMode.Off);

        voxelizationMaterial.setTexture("Texture3D", voxelTexture);
        voxelizationMaterial.setBoolean("Voxelization", true);
        renderManager.setForcedMaterial(voxelizationMaterial);
        renderManager.setForcedRenderState(voxelizationState);

        viewPort.getQueue().renderQueue(RenderQueue.Bucket.Opaque, renderManager, viewPort.getCamera());

        renderManager.getRenderer().clearBuffers(true, false, false);
    }

    private void renderScene() {
        renderManager.getRenderer().clearBuffers(true, true, true);

        RenderState renderState = new RenderState();
        renderState.setDepthTest(true);
        renderState.setFaceCullMode(RenderState.FaceCullMode.Back);
        renderState.setBlendMode(RenderState.BlendMode.Alpha); // Implicit ONE_MINUS_SRC_ALPHA

        renderManager.setForcedRenderState(renderState);
        renderManager.setForcedMaterial(voxelizationMaterial);
        viewPort.getQueue().renderQueue(RenderQueue.Bucket.Opaque, renderManager, viewPort.getCamera());
    }

    private void renderVisualization() {
        renderVoxelCube();
        //render3DTexture();
    }

    private void render3DTexture() {
        forcedRenderState.setColorWrite(true);
        forcedRenderState.setDepthTest(false);
        forcedRenderState.setDepthWrite(false);

        renderManager.getRenderer().setFrameBuffer(viewPort.getOutputFrameBuffer());
        renderManager.setForcedMaterial(voxelVisualizationMaterial);
        renderManager.setForcedRenderState(forcedRenderState);

        renderManager.getRenderer().clearBuffers(false, true, true);
        //viewPort.getQueue().renderQueue(RenderQueue.Bucket.Opaque, renderManager, viewPort.getCamera(), false);
        renderManager.renderGeometry(quad);

    }

    private void renderVoxelCube() {
        renderManager.getRenderer().clearBuffers(true, true, true);

//        forcedRenderState.setColorWrite(false);
//        forcedRenderState.setDepthTest(true);
//        forcedRenderState.setDepthWrite(true);
        // Assume correct viewport.

        // Back.
        forcedRenderState.setFaceCullMode(RenderState.FaceCullMode.Front);
        //renderManager.getRenderer().setFrameBuffer(backFB);
        renderManager.setForcedMaterial(worldPositionMaterial);
        renderManager.setForcedRenderState(forcedRenderState);
        renderManager.getRenderer().clearBuffers(false, true, true);
        //viewPort.getQueue().renderQueue(RenderQueue.Bucket.Opaque, renderManager, viewPort.getCamera(), false); //TODO: Same as rq?

        renderManager.renderGeometry(cube);

//        // Front.
        forcedRenderState.setFaceCullMode(RenderState.FaceCullMode.Back);
//        renderManager.getRenderer().setFrameBuffer(frontFB);
//        renderManager.setForcedMaterial(worldPositionMaterial);
//        renderManager.setForcedRenderState(forcedRenderState);
        renderManager.getRenderer().clearBuffers(false, true, true);
//        //viewPort.getQueue().renderQueue(RenderQueue.Bucket.Opaque, renderManager, viewPort.getCamera(), false); //TODO: Same as rq?
            renderManager.renderGeometry(cube);
    }

    @Override
    public void postFrame(FrameBuffer out) {
    }

    @Override
    public void cleanup() {
    }
}
