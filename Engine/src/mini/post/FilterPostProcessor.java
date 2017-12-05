package mini.post;

import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture2D;
import mini.ui.Picture;

/**
 * A <code>FilterPostProcessor</code> is a processor that can apply several {@link Filter}s to a
 * rendered scene. It manages a list of filters that will be applied in the order in which they have
 * been added to the list.
 */
public class FilterPostProcessor implements SceneProcessor {
    private RenderManager renderManager;
    private ViewPort viewPort;
    private float left, right, top, bottom;
    private int originalWidth, originalHeight;
    private int width, height;
    private FrameBuffer renderFrameBuffer;
    private Texture2D filterTexture;
    private Renderer renderer;
    private FrameBuffer outputBuffer;

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        renderer = rm.getRenderer();
        viewPort = vp;

        Picture fullScreenQuad = new Picture("filter full screen quad");
        fullScreenQuad.setWidth(1);
        fullScreenQuad.setHeight(1);

        Camera camera = vp.getCamera();

        left = camera.getViewPortLeft();
        right = camera.getViewPortRight();
        top = camera.getViewPortTop();
        bottom = camera.getViewPortBottom();

        originalWidth = camera.getWidth();
        originalHeight = camera.getHeight();

        reshape(vp, camera.getWidth(), camera.getHeight());
    }

    @Override
    public boolean isInitialized() {
        return viewPort != null;
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        Camera camera = vp.getCamera();

        // This will have no effect at init call but useful when resizing the canvas with multiple views
        camera.setViewPort(left, right, bottom, top);

        camera.resize(w, h, false);
        left = camera.getViewPortLeft();
        right = camera.getViewPortRight();
        top = camera.getViewPortTop();
        bottom = camera.getViewPortBottom();
        originalHeight = h;
        originalWidth = w;

        // Compute real dimension of the viewport and resize the camera
        width = (int) (w * (Math.abs(right - left)));
        height = (int) (h * (Math.abs(bottom - top)));
        width = Math.max(1, width);
        height = Math.max(1, height);

        // There is a different setup for more than one samples
        renderFrameBuffer = new FrameBuffer(width, height, 1);
        renderFrameBuffer.setDepthBuffer(Image.Format.Depth);

        filterTexture = new Texture2D(width, height, Image.Format.RGB111110F);
        renderFrameBuffer.setColorTexture(filterTexture);

        setupViewportFrameBuffer();
    }

    private void setupViewportFrameBuffer() {
        viewPort.setOutputFrameBuffer(renderFrameBuffer);
    }

    @Override
    public void preFrame(float tpf) {
        setupViewportFrameBuffer();

        // TODO: Multiview situation

        // TODO: Filters
    }

    @Override
    public void postQueue(RenderQueue rq) {
        // TODO: Filters
    }

    @Override
    public void postFrame(FrameBuffer out) {
        FrameBuffer sceneBuffer = renderFrameBuffer;
        renderFilterChain(renderer, sceneBuffer);

        renderer.setFrameBuffer(outputBuffer);

        if (viewPort != null) {
            renderManager.setCamera(viewPort.getCamera(), false);
        }
    }

    /**
     * Iterator through the filter list and renders filters
     *
     * @param renderer
     * @param sceneBuffer
     */
    private void renderFilterChain(Renderer renderer, FrameBuffer sceneBuffer) {
        Texture2D texture = this.filterTexture;
        FrameBuffer buffer = sceneBuffer;
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {

    }
}
