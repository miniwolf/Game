package mini.shadow;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.post.Filter;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.textures.FrameBuffer;

public abstract class AbstractShadowFilter<T extends AbstractShadowRenderer> extends Filter {
    protected final T shadowRenderer;
    protected ViewPort viewPort;

    /**
     * @param assetManager   the application <code>AssetManager</code>
     * @param shadowmapSize  the size of the rendered shadowmaps (512, 1024, 2048, etc...)
     * @param shadowRenderer the shadowRenderer to use for this filter
     */
    public AbstractShadowFilter(AssetManager assetManager, int shadowmapSize, T shadowRenderer) {
        super("Post Shadow");
        material = new Material(assetManager, "MatDefs/Shadow/PostShadowFilter.minid");
        this.shadowRenderer = shadowRenderer;
        this.shadowRenderer.setPostShadowMaterial(material);

        // Back face shadows
        this.shadowRenderer.setRenderBackFacesShadows(true);
    }

    /**
     * Set the shadowIntensity, the value should be between 0 and 1, a 0 value gives a bright and
     * invisible shadow, a 1 value a pitch black shadow, default value is 0.7.
     *
     * @param shadowIntensity the darkness of the shadow
     */
    final public void setShadowIntensity(float shadowIntensity) {
        shadowRenderer.setShadowIntensity(shadowIntensity);
    }

    /**
     * Sets the filtering mode for shadow edges see {@link EdgeFilteringMode} for more info.
     *
     * @param filteringMode
     */
    final public void setEdgeFilteringMode(EdgeFilteringMode filteringMode) {
        shadowRenderer.setEdgeFilteringMode(filteringMode);
    }

    @Override
    protected void initFilter(AssetManager manager, RenderManager renderManager, ViewPort viewPort,
                              int width, int height) {
        shadowRenderer.needsfallBackMaterial = true;
        shadowRenderer.initialize(renderManager, viewPort);
        this.viewPort = viewPort;
    }

    @Override
    public void postFrame(RenderManager renderManager, ViewPort viewPort, FrameBuffer buffer,
                          FrameBuffer sceneBuffer) {
        if (!shadowRenderer.skipPostPass) {
            shadowRenderer.setPostShadowParams();
        }
    }
}
