package mini.post;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.renderer.Camera;
import mini.renderer.Caps;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture2D;
import mini.ui.Picture;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A <code>FilterPostProcessor</code> is a processor that can apply several {@link Filter}s to a
 * rendered scene. It manages a list of filters that will be applied in the order in which they have
 * been added to the list.
 */
public class FilterPostProcessor implements SceneProcessor {
    private final AssetManager assetManager;
    private RenderManager renderManager;
    private ViewPort viewPort;
    private float left, right, top, bottom;
    private int originalWidth, originalHeight;
    private int width, height;
    private int lastFilterIndex;
    private int numSamples;
    private List<Filter> filters = new ArrayList<>();
    private FrameBuffer renderFrameBuffer;
    private Texture2D filterTexture;
    private Renderer renderer;
    private FrameBuffer outputBuffer;
    private Picture fsQuad;
    private boolean multiView;
    private boolean cameraInit;
    private boolean computeDepth;

    public FilterPostProcessor(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        renderer = rm.getRenderer();
        viewPort = vp;
        fsQuad = new Picture("filter full screen quad");
        fsQuad.setHeight(1);
        fsQuad.setWidth(1);

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
    public void reshape(ViewPort vp, int width, int height) {
        Camera camera = vp.getCamera();

        // This will have no effect at init call but useful when resizing the canvas with multiple views
        camera.setViewPort(left, right, bottom, top);

        camera.resize(width, height, false);
        left = camera.getViewPortLeft();
        right = camera.getViewPortRight();
        top = camera.getViewPortTop();
        bottom = camera.getViewPortBottom();
        originalHeight = height;
        originalWidth = width;

        // Compute real dimension of the viewport and resize the camera
        this.width = (int) (width * (Math.abs(right - left)));
        this.height = (int) (height * (Math.abs(bottom - top)));
        this.width = Math.max(1, this.width);
        this.height = Math.max(1, this.height);

        if (originalHeight != this.height || originalWidth != this.width) {
            multiView = true;
        }

        cameraInit = true;
        computeDepth = false;

        if (renderFrameBuffer == null) {
            outputBuffer = viewPort.getOutputFrameBuffer();
        }
        Set<Caps> caps = renderer.getCaps();

        if (numSamples > 1 && caps.contains(Caps.FrameBufferMultisample)) {
            throw new UnsupportedOperationException();
        }

        // There is a different setup for more than one samples
        renderFrameBuffer = new FrameBuffer(this.width, this.height, 1);
        renderFrameBuffer.setDepthBuffer(Image.Format.Depth);

        filterTexture = new Texture2D(this.width, this.height, Image.Format.RGB111110F);
        renderFrameBuffer.setColorTexture(filterTexture);

        for (Filter filter : filters) {
            initFilter(filter, vp);
        }

        setupViewportFrameBuffer();
    }

    private void setupViewportFrameBuffer() {
        viewPort.setOutputFrameBuffer(renderFrameBuffer);
    }

    @Override
    public void preFrame(float tpf) {
        if (filters.isEmpty() || lastFilterIndex == -1) {
            if (cameraInit) {
                viewPort.getCamera().setViewPort(left, right, bottom, top);
                viewPort.getCamera().resize(originalWidth, originalHeight, true);
                viewPort.setOutputFrameBuffer(outputBuffer);
                cameraInit = false;
            }
        } else {
            setupViewportFrameBuffer();

            if (multiView) {
                viewPort.getCamera().setViewPort(0, 1, 0, 1);
                viewPort.getCamera().resize(width, height, false);
                viewPort.getCamera().update();
                renderManager.setCamera(viewPort.getCamera(), false);
            }
        }

        for (Filter filter : filters) {
            if (filter.isEnabled()) {
                filter.preFrame(tpf);
            }
        }
    }

    @Override
    public void postQueue(RenderQueue rq) {
        for (Filter filter : filters) {
            if (filter.isEnabled()) {
                filter.postQueue(rq);
            }
        }
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
        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);
            if (!filter.isEnabled()) {
                continue;
            }

            // Post render pass
            filter.postFrame(renderManager, viewPort, buffer, sceneBuffer);

            Material material = filter.getMaterial();

            if (filter.isRequiresSceneTexture()) {
                material.setTexture("Texture", texture);
                if (texture.getImage().getMultiSamples() > 1) {
                    throw new UnsupportedOperationException(); // Setting numSamples in the material definition
                } else {
                    material.clearParam("NumSamples");
                }
            }

            buffer = outputBuffer;
            if (i != lastFilterIndex) {
                buffer = filter.getRenderFrameBuffer();
                texture = filter.getRenderedTexture();
            }
            renderProcessing(renderer, buffer, material);

            filter.postFilter(renderer, buffer);
        }
    }

    private void renderProcessing(Renderer renderer, FrameBuffer buffer, Material material) {
        if (buffer == outputBuffer) {
            viewPort.getCamera().resize(originalWidth, originalHeight, false);
            viewPort.getCamera().setViewPort(left, right, bottom, top);

            renderManager.setCamera(viewPort.getCamera(), false);
            if (material.getAdditionalRenderState().isDepthWrite()) {
                material.getAdditionalRenderState().setDepthTest(false);
                material.getAdditionalRenderState().setDepthWrite(false);
            }
        } else {
            viewPort.getCamera().resize(buffer.getWidth(), buffer.getHeight(), false);
            viewPort.getCamera().setViewPort(0, 1, 0, 1);

            renderManager.setCamera(viewPort.getCamera(), false);
            material.getAdditionalRenderState().setDepthTest(true);
            material.getAdditionalRenderState().setDepthWrite(true);
        }

        fsQuad.setMaterial(material);
        fsQuad.updateGeometricState();

        renderer.setFrameBuffer(buffer);
        renderer.clearBuffers(true, true, true);
        renderManager.renderGeometry(fsQuad);
    }

    @Override
    public void cleanup() {
        if (viewPort == null) {
            return;
        }

        viewPort.getCamera().resize(originalWidth, originalHeight, true);
        viewPort.getCamera().setViewPort(left, right, bottom, top);
        viewPort.setOutputFrameBuffer(outputBuffer);
        viewPort = null;

        if (renderFrameBuffer != null) {
            renderFrameBuffer.dispose();
        }

        filterTexture.getImage().dispose();

        for (Filter filter : filters) {
            filter.cleanup(renderer);
        }
    }

    public void addFilter(Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null");
        }

        filters.add(filter);

        if (isInitialized()) {
            initFilter(filter, viewPort);
        }

        setFilterState(filter, filter.isEnabled());
    }

    protected void setFilterState(Filter filter, boolean enabled) {
        if (filters.contains(filter)) {
            filter.setEnabled(enabled);
            updateLastFilterIndex();
        }
    }

    private void updateLastFilterIndex() {
        lastFilterIndex = -1;
        for (int i = filters.size() - 1; i >= 0; i--) {
            if (filters.get(i).isEnabled()) {
                lastFilterIndex = i;

                // FilterPostProcess is initialized, but the viewport framebuffer is the original
                // out framebuffer, so we must recover from a situation where no filter was enabled.
                // So we set the correct framebuffer on the viewport. Hence the reference equal
                if (isInitialized() && viewPort.getOutputFrameBuffer() == outputBuffer) {
                    setupViewportFrameBuffer();
                }
                return;
            }
        }
        if (isInitialized() && lastFilterIndex == -1) {
            // There is no enabled filter, we restore the original framebuffer to the viewport to
            // bypass the FilterPostProcessor
            viewPort.setOutputFrameBuffer(outputBuffer);
        }
    }

    private void initFilter(Filter filter, ViewPort viewPort) {
        filter.setProcessor(this);
        // TODO: Requires depth texture, shadow mapping
        filter.init(assetManager, renderManager, viewPort, width, height);
    }

    /**
     * @return number of samples used for antialiasing
     */
    public int getNumSamples() {
        return numSamples;
    }

    /**
     * Sets number of samples used for antialiasing
     *
     * @param numSamples the number of samples
     */
    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }
}
