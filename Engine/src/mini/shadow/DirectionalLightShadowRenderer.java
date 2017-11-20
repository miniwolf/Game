package mini.shadow;

import mini.asset.AssetManager;
import mini.light.DirectionalLight;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.renderer.Camera;

import java.util.Arrays;

/**
 * DirectionalLightShadowRenderer renderer use Parallel Split Shadow Mapping technique (PSSM) <br>
 * It splits the view frustum in several parts and compute a shadow map for each one. <br> Splits
 * are distributed so that the closer they are to the camera, the smaller they are to maximize the
 * resolution used of the shadow map. <br> This results in a better quality shadow than standard
 * shadow mapping.<br> <a href="https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch10.html">
 * NVidia article</a>
 */
public class DirectionalLightShadowRenderer extends AbstractShadowRenderer {
    private ColorRGBA splits;
    private Camera shadowCam;
    private Vector3f[] points = new Vector3f[8];
    private DirectionalLight light;
    private float lambda;

    /**
     * Create a DirectionalLightShadowRenderer.
     *
     * @param assetManager  the application asset manager.
     * @param shadowmapSize the size of the renderered shadow maps (512, 1024, 2048, etc...)
     * @param nbSplits      the number of shadow maps renderered (the more shadow maps the more quality,
     *                      the less fps).
     */
    public DirectionalLightShadowRenderer(AssetManager assetManager, int shadowmapSize,
                                          int nbSplits) {
        super(assetManager, shadowmapSize, nbSplits);
        init(nbSplits);
    }

    private void init(int nbSplits) {
        this.nbSplits = Math.max(Math.min(nbSplits, 4), 1);
        if (this.nbSplits != nbSplits) {
            throw new IllegalArgumentException("Number of splits must be between 1 and 4."
                                               + " Given value : " + nbSplits);
        }

        splits = new ColorRGBA();
        float[] splitsArray = new float[nbSplits + 1];
        shadowCam = new Camera(shadowmapSize, shadowmapSize);
        shadowCam.setParallelProjection(true);
        Arrays.setAll(points, i -> new Vector3f());
    }

    /**
     * Sets the light to use to cast shadows
     *
     * @param light a <code>DirectionalLight</code>
     */
    public void setLight(DirectionalLight light) {
        this.light = light;
    }

    /**
     * Adjust the repartition of the different shadow maps in the shadow extend usually goes from
     * 0.0 to 1.0. A low value gives a more linear repartition resulting in a constant quality in
     * shadow over the extends, but near the shadows could look very jagged. A high value gives a
     * more logarithmic repartition resulting in a high quality for near shadows, but the quality
     * quickly decrease over the extend. The default value (theoretical optimal value) is set to
     * 0.65f
     *
     * @param lambda the lambda value
     */
    public void setLambda(float lambda) {
        this.lambda = lambda;
    }
}
