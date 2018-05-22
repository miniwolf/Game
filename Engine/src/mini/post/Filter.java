package mini.post;

import mini.asset.AssetManager;
import mini.export.Savable;
import mini.material.Material;
import mini.renderer.Caps;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture2D;

import java.util.Collection;

/**
 * Filters are 2D effects applied to the rendered scene. The filter is fed with the rendered scene
 * image rendered in an offscreen frame buffer. This texture is applied on a full-screen quad with a
 * special material. This material uses a shader that applies the desired effect to the scene
 * texture.
 * <p>
 * This class is abstract, any <code>Filter</code> must extend it. Any filter holds a
 * <code>FrameBuffer</code> and a <code>Texture</code>.
 */
public abstract class Filter implements Savable {
    protected Material material;
    protected Pass defaultPass;
    private String name;
    protected boolean enabled = true;
    private FilterPostProcessor processor;

    public Filter(String name) {
        this.name = name;
    }

    public void setProcessor(FilterPostProcessor processor) {
        this.processor = processor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable this filter
     *
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        if (processor != null) {
            processor.setFilterState(this, enabled);
        } else {
            this.enabled = enabled;
        }
    }

    /**
     * Makes it possible to make a pass after the frame has been rendered and just before the filter
     * is rendered
     */
    public abstract void postFrame(RenderManager renderManager, ViewPort viewPort,
                                   FrameBuffer buffer, FrameBuffer sceneBuffer);

    /**
     * this method is called every frame
     *
     * @return the material used for this filter
     */
    public Material getMaterial() {
        return material;
    }

    public FrameBuffer getRenderFrameBuffer() {
        return defaultPass.renderFrameBuffer;
    }

    public Texture2D getRenderedTexture() {
        return defaultPass.renderedTexture;
    }

    /**
     * @return false if your <code>Filter</code> does not need the scene texture
     */
    public boolean isRequiresSceneTexture() {
        return true;
    }

    /**
     * Initialization of sub classes filters.
     * <p>
     * Call this method when the filter is added to the <code>FilterPostProcessor</code>.
     * It should contain <code>Material</code> initialization and extra passes initialization.
     *
     * @param viewPort ViewPort where this filter is rendered
     * @param width    width of the filter
     * @param height   height of the filter
     */
    protected abstract void initFilter(AssetManager manager, RenderManager renderManager,
                                       ViewPort viewPort, int width, int height);

    /**
     * Clean up filter
     */
    protected abstract void cleanupFilter(Renderer renderer);

    /**
     * This method is called after the filter has been rendered to the framebuffer.
     * NOTE: The buffer will be null if the filter is the last one on the stack and has been
     * rendered to screen.
     *
     * @param renderer
     * @param buffer   Framebuffer on which the filter has been rendered
     */
    public abstract void postFilter(Renderer renderer, FrameBuffer buffer);

    /**
     * This method is called pre pass, before the actual rendering of the frame.
     */
    public abstract void postQueue(RenderQueue queue);

    /**
     * Modify parameters according to tpf before the rendering of the frame. This is useful for
     * animated filters. Also it can be placed to render pre passes.
     *
     * @param tpf the time used to render the previous frame
     */
    public abstract void preFrame(float tpf);

    protected final void init(AssetManager manager, RenderManager renderManager, ViewPort viewPort,
                              int width, int height) {
        defaultPass = new Pass();
        defaultPass.init(renderManager.getRenderer(), width, height, getDefaultPassTextureFormat(),
                         getDefaultPassDepthFormat());
        initFilter(manager, renderManager, viewPort, width, height);
    }

    private Image.Format getDefaultPassDepthFormat() {
        return Image.Format.Depth;
    }

    private Image.Format getDefaultPassTextureFormat() {
        return Image.Format.RGB111110F;
    }

    protected final void cleanup(Renderer renderer) {
        processor = null;
        if (defaultPass != null) {
            defaultPass.cleanup(renderer);
        }
        cleanupFilter(renderer);
    }

    /**
     * returns the name of the filter
     *
     * @return the Filter's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the filter
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Inner class Pass
     * Pass are like filters in filters.
     * Some filters will need multiple passes before the final render
     */
    public class Pass {
        protected FrameBuffer renderFrameBuffer;
        protected Texture2D renderedTexture;
        protected Texture2D depthTexture;
        protected Material passMaterial;
        protected String name;

        public Pass(String name) {
            this.name = name;
        }

        public Pass() {
        }

        /**
         * init the pass called internally
         *
         * @param renderer
         * @param width
         * @param height
         * @param textureFormat
         * @param depthBufferFormat
         * @param numSamples
         */
        public void init(Renderer renderer, int width, int height, Image.Format textureFormat,
                         Image.Format depthBufferFormat, int numSamples, boolean renderDepth) {
            Collection<Caps> caps = renderer.getCaps();
            if (numSamples > 1 && caps.contains(Caps.FrameBufferMultisample) && caps
                    .contains(Caps.OpenGL31)) {
                renderFrameBuffer = new FrameBuffer(width, height, numSamples);
                renderedTexture = new Texture2D(width, height, numSamples, textureFormat);
                renderFrameBuffer.setDepthBuffer(depthBufferFormat);
                if (renderDepth) {
                    depthTexture = new Texture2D(width, height, numSamples, depthBufferFormat);
                    renderFrameBuffer.setDepthTexture(depthTexture);
                }
            } else {
                renderFrameBuffer = new FrameBuffer(width, height, 1);
                renderedTexture = new Texture2D(width, height, textureFormat);
                renderFrameBuffer.setDepthBuffer(depthBufferFormat);
                if (renderDepth) {
                    depthTexture = new Texture2D(width, height, depthBufferFormat);
                    renderFrameBuffer.setDepthTexture(depthTexture);
                }
            }

            renderFrameBuffer.setColorTexture(renderedTexture);

        }

        /**
         * init the pass called internally
         *
         * @param renderer
         * @param width
         * @param height
         * @param textureFormat
         * @param depthBufferFormat
         */
        public void init(Renderer renderer, int width, int height, Image.Format textureFormat,
                         Image.Format depthBufferFormat) {
            init(renderer, width, height, textureFormat, depthBufferFormat, 1);
        }

        public void init(Renderer renderer, int width, int height, Image.Format textureFormat,
                         Image.Format depthBufferFormat, int numSamples) {
            init(renderer, width, height, textureFormat, depthBufferFormat, numSamples, false);
        }

        /**
         * init the pass called internally
         *
         * @param renderer
         * @param width
         * @param height
         * @param textureFormat
         * @param depthBufferFormat
         * @param numSample
         * @param material
         */
        public void init(Renderer renderer, int width, int height, Image.Format textureFormat,
                         Image.Format depthBufferFormat, int numSample, Material material) {
            init(renderer, width, height, textureFormat, depthBufferFormat, numSample);
            passMaterial = material;
        }

        public boolean requiresSceneAsTexture() {
            return false;
        }

        public boolean requiresDepthAsTexture() {
            return false;
        }

        public void beforeRender() {
        }

        public FrameBuffer getRenderFrameBuffer() {
            return renderFrameBuffer;
        }

        public void setRenderFrameBuffer(FrameBuffer renderFrameBuffer) {
            this.renderFrameBuffer = renderFrameBuffer;
        }

        public Texture2D getDepthTexture() {
            return depthTexture;
        }

        public Texture2D getRenderedTexture() {
            return renderedTexture;
        }

        public void setRenderedTexture(Texture2D renderedTexture) {
            this.renderedTexture = renderedTexture;
        }

        public Material getPassMaterial() {
            return passMaterial;
        }

        public void setPassMaterial(Material passMaterial) {
            this.passMaterial = passMaterial;
        }

        public void cleanup(Renderer r) {
            renderFrameBuffer.dispose();
            renderedTexture.getImage().dispose();
            if (depthTexture != null) {
                depthTexture.getImage().dispose();
            }
        }

        @Override
        public String toString() {
            return name == null ? super.toString() : name;
        }
    }
}
