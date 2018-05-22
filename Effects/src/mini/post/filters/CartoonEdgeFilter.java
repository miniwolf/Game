package mini.post.filters;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.post.Filter;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;
import mini.textures.Image;

public class CartoonEdgeFilter extends Filter {
    private RenderManager renderManager;
    private ViewPort viewPort;
    private Pass normalPass;
    private float edgeWidth = 1.0f;
    private float edgeIntensity = 1.0f;
    private float normalThreshold = 0.5f;
    private float depthThreshold = 0.1f;
    private float normalSensitivity = 1.0f;
    private float depthSensitivity = 10.0f;
    private ColorRGBA edgeColor = new ColorRGBA(0, 0, 0, 1);

    public CartoonEdgeFilter() {
        super("CartoonEdgeFilter");
    }

    @Override
    public void postFrame(RenderManager renderManager, ViewPort viewPort, FrameBuffer buffer,
                          FrameBuffer sceneBuffer) {
    }

    @Override
    public void postFilter(Renderer renderer, FrameBuffer buffer) {
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue ignored) {
        Renderer renderer = renderManager.getRenderer();
        renderer.setFrameBuffer(normalPass.getRenderFrameBuffer());
        renderer.clearBuffers(true, true, true);
        renderManager.setForcedTechnique("PreNormalPass");
        renderManager.renderViewPortQueues(viewPort, false);
        renderManager.setForcedTechnique(null);
        assert renderer == renderManager.getRenderer();
        renderer.setFrameBuffer(viewPort.getOutputFrameBuffer());
    }

    @Override
    protected void initFilter(AssetManager manager, RenderManager renderManager, ViewPort viewPort,
                              int width, int height) {
        this.renderManager = renderManager;
        this.viewPort = viewPort;
        normalPass = new Pass();
        normalPass.init(renderManager.getRenderer(), width, height, Image.Format.RGBA8,
                        Image.Format.Depth);
        material = new Material(manager, "MatDefs/Post/CartoonEdge.minid");
        material.setFloat("EdgeWidth", edgeWidth);
        material.setFloat("EdgeIntensity", edgeIntensity);
        material.setFloat("NormalThreshold", normalThreshold);
        material.setFloat("DepthThreshold", depthThreshold);
        material.setFloat("NormalSensitivity", normalSensitivity);
        material.setFloat("DepthSensitivity", depthSensitivity);
        material.setColor("EdgeColor", edgeColor);
    }

    @Override
    protected void cleanupFilter(Renderer renderer) {
        normalPass.cleanup(renderer);
    }

    public float getEdgeWidth() {
        return edgeWidth;
    }

    public void setEdgeWidth(float edgeWidth) {
        this.edgeWidth = edgeWidth;
        if (material != null) {
            material.setFloat("EdgeWidth", edgeWidth);
        }
    }

    public float getEdgeIntensity() {
        return edgeIntensity;
    }

    public void setEdgeIntensity(float edgeIntensity) {
        this.edgeIntensity = edgeIntensity;
        if (material != null) {
            material.setFloat("EdgeIntensity", edgeIntensity);
        }
    }

    public float getNormalThreshold() {
        return normalThreshold;
    }

    public void setNormalThreshold(float normalThreshold) {
        this.normalThreshold = normalThreshold;
        if (material != null) {
            material.setFloat("NormalThreshold", normalThreshold);
        }
    }

    public float getDepthThreshold() {
        return depthThreshold;
    }

    public void setDepthThreshold(float depthThreshold) {
        this.depthThreshold = depthThreshold;
        if (material != null) {
            material.setFloat("DepthThreshold", depthThreshold);
        }
    }

    public float getNormalSensitivity() {
        return normalSensitivity;
    }

    public void setNormalSensitivity(float normalSensitivity) {
        this.normalSensitivity = normalSensitivity;
        if (material != null) {
            material.setFloat("NormalSensitivity", normalSensitivity);
        }
    }

    public float getDepthSensitivity() {
        return depthSensitivity;
    }

    public void setDepthSensitivity(float depthSensitivity) {
        this.depthSensitivity = depthSensitivity;
        if (material != null) {
            material.setFloat("DepthSensitivity", depthSensitivity);
        }
    }

    public ColorRGBA getEdgeColor() {
        return edgeColor;
    }

    public void setEdgeColor(ColorRGBA edgeColor) {
        this.edgeColor = edgeColor;
        if (material != null) {
            material.setColor("EdgeColor", edgeColor);
        }
    }
}
