package mini.post;

import mini.material.Material;
import mini.renderer.Caps;
import mini.renderer.Renderer;
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
public class Filter {
    protected Material material;
    private String name;
    private boolean enabled;

    public Filter(String name) {
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
         * @param renderer
         * @param width
         * @param height
         * @param textureFormat
         * @param depthBufferFormat
         * @param numSamples
         */
        public void init(Renderer renderer, int width, int height, Image.Format textureFormat, Image.Format depthBufferFormat, int numSamples, boolean renderDepth) {
            Collection<Caps> caps = renderer.getCaps();
            if (numSamples > 1 && caps.contains(Caps.FrameBufferMultisample) && caps.contains(Caps.OpenGL31)) {
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
         *  init the pass called internally
         * @param renderer
         * @param width
         * @param height
         * @param textureFormat
         * @param depthBufferFormat
         */
        public void init(Renderer renderer, int width, int height, Image.Format textureFormat, Image.Format depthBufferFormat) {
            init(renderer, width, height, textureFormat, depthBufferFormat, 1);
        }

        public void init(Renderer renderer, int width, int height, Image.Format textureFormat, Image.Format depthBufferFormat, int numSamples) {
            init(renderer, width, height, textureFormat, depthBufferFormat, numSamples, false);
        }

        /**
         *  init the pass called internally
         * @param renderer
         * @param width
         * @param height
         * @param textureFormat
         * @param depthBufferFormat
         * @param numSample
         * @param material
         */
        public void init(Renderer renderer, int width, int height, Image.Format textureFormat, Image.Format depthBufferFormat, int numSample, Material material) {
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
            if(depthTexture!=null){
                depthTexture.getImage().dispose();
            }
        }

        @Override
        public String toString() {
            return name == null ? super.toString() : name;
        }
    }

    /**
     * Enable or disable this filter
     *
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * returns the name of the filter
     * @return the Filter's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the filter
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
