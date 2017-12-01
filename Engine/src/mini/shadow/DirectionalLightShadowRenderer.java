package mini.shadow;

import mini.asset.AssetManager;
import mini.light.DirectionalLight;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.renderer.queue.GeometryList;
import mini.renderer.queue.RenderQueue;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.utils.clone.Cloner;

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
    private float[] splitsArray;
    private Vector3f[] points = new Vector3f[8];
    private DirectionalLight light;
    private float lambda;

    /**
     * Holding the info for fading shadows in the far distance
     */
    private boolean stabilize = true;

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

    @Override
    protected Camera getShadowCam(int shadowMapIndex) {
        return shadowCam;
    }

    @Override
    protected GeometryList getOccludersToRender(int shadowMapIndex,
                                                GeometryList shadowMapOccluders) {
        ShadowUtils.updateFrustumPoints(viewPort.getCamera(), splitsArray[shadowMapIndex],
                                        splitsArray[shadowMapIndex + 1], 1.0f, points);

        if (lightReceivers.size() == 0) {
            for (Spatial scene : viewPort.getScenes()) {
                ShadowUtils.getGeometriesInCamFrustum(scene, viewPort.getCamera(),
                                                      RenderQueue.ShadowMode.Receive,
                                                      lightReceivers);
            }
        }
        ShadowUtils
                .updateShadowCamera(viewPort, lightReceivers, shadowCam, points, shadowMapOccluders,
                                    stabilize ? shadowmapSize : 0);

        return shadowMapOccluders;
    }

    @Override
    protected void displayFrustumDebug(int shadowMapIndex) {
        Node node = (Node) viewPort.getScenes().get(0);
        node.attachChild(createFrustum(points, shadowMapIndex));
        ShadowUtils.updateFrustumPoints(shadowCam, points);
        node.attachChild(createFrustum(points, shadowMapIndex));
    }

    /**
     * Directional light are always in the view frustum
     */
    @Override
    protected boolean checkCulling(Camera camera) {
        return true;
    }

    @Override
    protected void updateShadowCams(Camera viewCam) {
        if (light == null) {
            System.err.println("Warning: The light cannot be null for a " + getClass().getName());
            return;
        }

        float zFar = zFarOverride;
        if (zFar == 0) {
            zFar = viewCam.getFrustumFar();
        }

        // Prevent computing the frustum points and splits with zeroed or negative near clip values
        float frustumNear = Math.max(viewCam.getFrustumNear(), 0.0001f);
        ShadowUtils.updateFrustumPoints(viewCam, frustumNear, zFar, 1.0f, points);

        shadowCam.setFrustumFar(zFar);
        shadowCam.getRotation().lookAt(light.getDirection(), shadowCam.getUp());

        shadowCam.update();
        shadowCam.updateViewProjection();

        PSSMShadowUtils.updateFrustumSplit(splitsArray, frustumNear, zFar, lambda);

        if (viewCam.isParallelProjection()) {
            for (int i = 0; i < nbSplits; i++) {
                splitsArray[i] /= (zFar - frustumNear);
            }
        }

        switch (splitsArray.length) {
            case 5:
                splits.a = splitsArray[4];
            case 4:
                splits.b = splitsArray[3];
            case 3:
                splits.g = splitsArray[2];
            case 2:
            case 1:
                splits.r = splitsArray[1];
                break;
        }
    }

    @Override
    protected void clearMaterialParameters(Material material) {
        material.clearParam("Splits");
        material.clearParam("LightDir");
    }

    @Override
    protected void setMaterialParameters(Material material) {
        material.setColor("Splits", splits);
        material.setVector3("LightDir", light == null ? new Vector3f() : light.getDirection());
        // FadeInfo
    }

    @Override
    protected void getReceivers(GeometryList lightReceivers) {
        if (lightReceivers.size() != 0) {
            return;
        }

        for (Spatial scene : viewPort.getScenes()) {
            ShadowUtils.getGeometriesInCamFrustum(scene, viewPort.getCamera(),
                                                  RenderQueue.ShadowMode.Receive, lightReceivers);
        }
    }

    private void init(int nbSplits) {
        this.nbSplits = Math.max(Math.min(nbSplits, 4), 1);
        if (this.nbSplits != nbSplits) {
            throw new IllegalArgumentException("Number of splits must be between 1 and 4."
                                               + " Given value : " + nbSplits);
        }

        splits = new ColorRGBA();
        splitsArray = new float[nbSplits + 1];
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

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        light = cloner.clone(light);
        init(nbSplits);
        super.cloneFields(cloner, original);
    }
}
