package mini.shadow;

import mini.asset.AssetManager;
import mini.light.DirectionalLight;
import mini.renderer.Renderer;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;

/**
 * This Filter does basically the same as a <code>DirectionalLightShadowRenderer</code> except it
 * renders the post shadow pass as a fullscreen quad pass instead of a geometry pass. It's mostly
 * faster than PSSM as long as you have more than approx. ten shadow receiving objects. The expense
 * is the draw back that the shadowReceiveMode set on spatial is ignored. So basically all and only
 * objects that render depth in the scene receive shadows.
 */
public class DirectionalLightShadowFilter
        extends AbstractShadowFilter<DirectionalLightShadowRenderer> {

    /**
     * Creates a <code>DirectionalLightShadowFilter</code>. More info on the technique at
     * <a href="https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch10.html">https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch10.html</a>
     *
     * @param assetManager  the application <code>AssetManager</code>
     * @param shadowmapSize the size of the rendered shadowmaps (512, 1024, 2048, etc...)
     * @param nbSplits      the number of shadow maps rendered (the more shadow maps the more quality,
     *                      the less fps)
     */
    public DirectionalLightShadowFilter(AssetManager assetManager, int shadowmapSize,
                                        int nbSplits) {
        super(assetManager, shadowmapSize,
              new DirectionalLightShadowRenderer(assetManager, shadowmapSize, nbSplits));
    }

    /**
     * Set the light to use to cast shadows
     *
     * @param light a DirectionalLight
     */
    public void setLight(DirectionalLight light) {
        shadowRenderer.setLight(light);
    }

    /**
     * Adjust the repartition of the different shadow maps in the shadow extend usually goes from
     * 0.0 to 1.0 a low value gives a more linear repartition resulting in a constant quality in the
     * shadow over the extends, but near shadows could look very jagged. A high value gives a more
     * logarithmic repartition decrease over the extend. The default value is set to 0.65f
     * (theoretical optimal value).
     *
     * @param lamba the lambda value.
     */
    public void setLambda(float lamba) {
        shadowRenderer.setLambda(lamba);
    }

    @Override
    public void postFilter(Renderer renderer, FrameBuffer buffer) {
    }

    @Override
    public void postQueue(RenderQueue queue) {
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    protected void cleanupFilter(Renderer renderer) {
    }
}
