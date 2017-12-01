package mini.shadow;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.post.SceneProcessor;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.renderer.queue.GeometryList;
import mini.renderer.queue.OpaqueComparator;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Spatial;
import mini.scene.debug.WireFrustum;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.Texture2D;
import mini.ui.Picture;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractShadowRenderer implements SceneProcessor, MiniCloneable, Cloneable {
    protected final int shadowmapSize;
    private final AssetManager assetManager;
    protected int nbSplits;
    private float edgeThickness = 1.0f;
    private float shadowIntensity = 0.7f;
    private boolean renderBackFacesShadows = true;
    private boolean debug;
    private boolean debugfrustrums;
    private String postTechniqueName;

    private Texture2D[] shadowMaps;
    private Matrix4f[] lightViewProjectionMatrices;
    private FrameBuffer[] shadowFrameBuffer;
    private Picture[] displayPictures;

    private Material preshadowMaterial;
    private Material postshadowMaterial;
    protected ViewPort viewPort;
    private EdgeFilteringMode edgeFilteringMode = EdgeFilteringMode.Bilinear;
    private RenderState forcedRenderState = new RenderState();
    private RenderManager renderManager;
    /**
     * List of materials for post shadow queue geometries
     */
    protected GeometryList lightReceivers = new GeometryList(new OpaqueComparator());
    protected GeometryList shadowMapOccluders = new GeometryList(new OpaqueComparator());
    /**
     * fade shadows at distance
     */
    protected float zFarOverride = 0;
    private CompareMode shadowCompareMode = CompareMode.Hardware;
    private List<Material> materialCache = new ArrayList<>();
    private String[] shadowMapStringCache;
    private String[] lightViewStringCache;
    private Camera frustumCam;
    /**
     * Should the fallback material be used
     */
    private boolean needsfallBackMaterial;

    /**
     * Create a DirectionalLightShadowRenderer.
     *
     * @param assetManager  the application asset manager.
     * @param shadowmapSize the size of the renderered shadow maps (512, 1024, 2048, etc...)
     * @param nbSplits      the number of shadow maps renderered (the more shadow maps the more quality,
     *                      the less fps).
     */
    public AbstractShadowRenderer(AssetManager assetManager, int shadowmapSize, int nbSplits) {

        this.assetManager = assetManager;
        this.shadowmapSize = shadowmapSize;
        this.nbSplits = nbSplits;
        init();
    }

    private void init() {
        this.postshadowMaterial = new Material(assetManager, "MatDefs/Shadow/PostShadow.minid");
        postshadowMaterial.setFloat("ShadowMapSize", shadowmapSize);

        shadowFrameBuffer = new FrameBuffer[nbSplits];
        shadowMaps = new Texture2D[nbSplits];
        displayPictures = new Picture[nbSplits];

        lightViewProjectionMatrices = new Matrix4f[nbSplits];
        shadowMapStringCache = new String[nbSplits];
        lightViewStringCache = new String[nbSplits];

        preshadowMaterial = new Material(assetManager, "MatDefs/Shadow/PreShadow.minid");
        for (int i = 0; i < nbSplits; i++) {
            lightViewProjectionMatrices[i] = new Matrix4f();
            shadowFrameBuffer[i] = new FrameBuffer(shadowmapSize, shadowmapSize, 1);
            shadowMaps[i] = new Texture2D(shadowmapSize, shadowmapSize, Image.Format.Depth);

            shadowFrameBuffer[i].setDepthTexture(shadowMaps[i]);

            shadowMapStringCache[i] = "ShadowMap" + i;
            lightViewStringCache[i] = "LightViewProjectionMatrix" + i;

            // Debugging idea
            displayPictures[i] = new Picture("Picture" + i);
            displayPictures[i].setTexture(assetManager, shadowMaps[i], false);
        }

        setShadowCompareMode(shadowCompareMode);
        setEdgeFilteringMode(edgeFilteringMode);
        setShadowIntensity(shadowIntensity);
        initForcedRenderState();
        setRenderBackFacesShadows(isRenderBackFacesShadows());
    }

    private void initForcedRenderState() {
        forcedRenderState.setFaceCullMode(RenderState.FaceCullMode.Front);
        forcedRenderState.setColorWrite(false);
        forcedRenderState.setDepthWrite(true);
        forcedRenderState.setDepthTest(true);
    }

    /**
     * If this processor renders back faces shadows
     *
     * @return Whether this processor renders back faces shadows
     */
    public boolean isRenderBackFacesShadows() {
        return renderBackFacesShadows;
    }

    /**
     * Set to true if you want back faces shadows on geometries. Note that back face shadows will be
     * blended over dark lighten areas and may produce overly dark lightning.
     * <p>
     * Also note that setting this parameter will override this parameter for ALL materials in the
     * scene. You can alternately change this parameter on a single material using
     * {@link Material#setBoolean(String, boolean)}.
     * <p>
     * This will also automatically adjust the faceCullMode and the PolyOffset of the pre shadow
     * pass. You can modify them by using {@link #getPreShadowForcedRenderState()}.
     *
     * @param renderBackFacesShadows true or false
     */
    public void setRenderBackFacesShadows(boolean renderBackFacesShadows) {
        this.renderBackFacesShadows = renderBackFacesShadows;
        if (renderBackFacesShadows) {
            getPreShadowForcedRenderState().setPolyOffset(5, 3);
            getPreShadowForcedRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
        } else {
            getPreShadowForcedRenderState().setPolyOffset(0, 0);
            getPreShadowForcedRenderState().setFaceCullMode(RenderState.FaceCullMode.Front);
        }
    }

    /**
     * Set the shadow intensity. The value should be between 0 and 1. A 0 value gives a bright and
     * invisible shadow, a 1 value gives a pitch black shadow. The default value is 0.7
     *
     * @param shadowIntensity the darkness of the shadow
     */
    public void setShadowIntensity(float shadowIntensity) {
        this.shadowIntensity = shadowIntensity;
        postshadowMaterial.setFloat("ShadowIntensity", shadowIntensity);
    }

    /**
     * Sets the filtering mode for the shadow edges.
     *
     * @param filteringMode the desired filter mode
     * @throws IllegalArgumentException if filteringMode is null
     * @see {@link EdgeFilteringMode} for more info.
     */
    public void setEdgeFilteringMode(EdgeFilteringMode filteringMode) {
        if (filteringMode == null) {
            throw new IllegalArgumentException("Edge filtering mode cannot be null");
        }

        this.edgeFilteringMode = filteringMode;
        postshadowMaterial.setInt("FilterMode", filteringMode.getMaterialParamValue());
        postshadowMaterial.setFloat("PCFEdge", edgeThickness);
        if (shadowCompareMode != CompareMode.Hardware) {
            return;
        }

        Arrays.stream(shadowMaps).forEach(shadowMap -> setTextureFilters(filteringMode, shadowMap));
    }

    private void setTextureFilters(EdgeFilteringMode filteringMode, Texture2D shadowMap) {
        if (filteringMode == EdgeFilteringMode.Bilinear) {
            shadowMap.setMagFilter(Texture.MagFilter.Bilinear);
            shadowMap.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        } else {
            shadowMap.setMagFilter(Texture.MagFilter.Nearest);
            shadowMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        }
    }

    /**
     * Sets the shadow compare mode.
     *
     * @param compareMode the desired compare mode
     * @throws IllegalArgumentException if compareMode is null
     * @see {@link CompareMode} for more info.
     */
    public void setShadowCompareMode(CompareMode compareMode) {
        if (compareMode == null) {
            throw new IllegalArgumentException("Shadow compare mode cannot be null");
        }

        this.shadowCompareMode = compareMode;
        for (Texture2D shadowMap : shadowMaps) {
            if (compareMode == CompareMode.Hardware) {
                shadowMap.setShadowCompareMode(Texture.ShadowCompareMode.LessOrEqual);
                setTextureFilters(edgeFilteringMode, shadowMap);
            } else {
                // Do software basically shadowcompare off and nearest
                throw new NotImplementedException();
            }
        }
        postshadowMaterial.setBoolean("HardwareShadows", compareMode == CompareMode.Hardware);
    }

    /**
     * @return
     */
    public RenderState getPreShadowForcedRenderState() {
        return forcedRenderState;
    }

    public void displayDebug() {
        debug = true;
    }

    public void displayFrustum() {
        debugfrustrums = true;
    }

    /**
     * Initializes the shadow renderer prior to its first update.
     *
     * @param rm The render manager to which the SP was added to
     * @param vp The viewport to which the SP is assigned
     */
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        viewPort = vp;
        postTechniqueName = "PostShadow";
        if (zFarOverride > 0 && frustumCam == null) {
            // TODO: Initialize the frustum cam
            throw new NotImplementedException();
        }
    }

    /**
     * @param shadowMapIndex the index of the shadow map being rendered
     * @return the shadow camera to use for rendering the shadow map according to the given index
     */
    protected abstract Camera getShadowCam(int shadowMapIndex);

    /**
     * @param shadowMapIndex the index of the shadow map being rendered.
     * @param shadowMapOccluders the list of occluders
     * @return a subclass-specific geometryList containing the occluders to be rendered in the
     * shadow map.
     */
    protected abstract GeometryList getOccludersToRender(int shadowMapIndex,
                                                         GeometryList shadowMapOccluders);

    /**
     * responsible for displaying the frustum of the shadow cam for debug purpose
     *
     * @param shadowMapIndex
     */
    protected abstract void displayFrustumDebug(int shadowMapIndex);

    /**
     * @return true if the light source bounding box is in the view frustum
     */
    protected abstract boolean checkCulling(Camera camera);

    /**
     * Invoked once per frame to update the shadow cams according to the light view.
     *
     * @param viewCam the scene cam
     */
    protected abstract void updateShadowCams(Camera viewCam);

    /**
     * @return Whether this shadow renderer has been initialized
     */
    @Override
    public boolean isInitialized() {
        return viewPort != null;
    }

    @Override
    public void postQueue(RenderQueue rq) {
        lightReceivers.clear();
        if (!checkCulling(viewPort.getCamera())) {
            // TODO: SkipPostPass = true;
            return;
        }

        updateShadowCams(viewPort.getCamera());

        Renderer renderer = renderManager.getRenderer();
        renderManager.setForcedMaterial(preshadowMaterial);
        renderManager.setForcedTechnique("PreShadow");

        for (int shadowMapIndex = 0; shadowMapIndex < nbSplits; shadowMapIndex++) {
            if (debugfrustrums) {
                displayFrustumDebug(shadowMapIndex);
            }
            renderShadowMap(shadowMapIndex);
        }
        debugfrustrums = false;

        // restore settings for future rendering
        renderer.setFrameBuffer(viewPort.getOutputFrameBuffer());
        renderManager.setForcedMaterial(null);
        renderManager.setForcedTechnique(null);
        renderManager.setCamera(viewPort.getCamera(), false);
    }

    private void renderShadowMap(int shadowMapIndex) {
        shadowMapOccluders = getOccludersToRender(shadowMapIndex, shadowMapOccluders);
        Camera shadowCam = getShadowCam(shadowMapIndex);

        // Saving light view projection matrix for this split
        lightViewProjectionMatrices[shadowMapIndex].set(shadowCam.getViewProjectionMatrix());
        renderManager.setCamera(shadowCam, false);

        Renderer renderer = renderManager.getRenderer();
        renderer.setFrameBuffer(shadowFrameBuffer[shadowMapIndex]);
        renderer.clearBuffers(true, true, true);

        renderManager.setForcedRenderState(forcedRenderState);
        viewPort.getQueue().renderShadowQueue(shadowMapOccluders, renderManager, shadowCam, true);
        renderManager.setForcedRenderState(null);
    }

    @Override
    public void postFrame(FrameBuffer out) {
        if (debug) {
            displayShadowMap();
        }

        getReceivers(lightReceivers);

        if (lightReceivers.size() == 0) {
            return;
        }

        setMaterialParameters(lightReceivers);

        Camera cam = viewPort.getCamera();

        // Some materials in the scene does not have a post shadow technique so we're using the
        // fall back material
        if (needsfallBackMaterial) {
            renderManager.setForcedMaterial(postshadowMaterial);
        }

        renderManager.setForcedTechnique(postTechniqueName);
        viewPort.getQueue().renderShadowQueue(lightReceivers, renderManager, cam, false);

        // Resetting rendermanager settings
        renderManager.setForcedTechnique(null);
        renderManager.setForcedMaterial(null);
        renderManager.setCamera(cam, false);

        clearMaterialParameters();
    }

    private void clearMaterialParameters() {
        for (Material material : materialCache) {
            for (int i = 0; i < nbSplits; i++) {
                material.clearParam(lightViewStringCache[i]);
                material.clearParam(shadowMapStringCache[i]);
            }
            // Clear FadeInfo?
            clearMaterialParameters(material);
        }
    }

    private void setMaterialParameters(GeometryList list) {
        buildMatCache(list);

        for (Material material : materialCache) {
            material.setFloat("ShadowMapSize", shadowmapSize);
            for (int i = 0; i < nbSplits; i++) {
                material.setMatrix4(lightViewStringCache[i], lightViewProjectionMatrices[i]);
                material.setTexture(shadowMapStringCache[i], shadowMaps[i]);
            }

            material.setBoolean("HardwareShadows", shadowCompareMode == CompareMode.Hardware);
            material.setBoolean("BackfaceShadows", renderBackFacesShadows);
            material.setInt("FilterMode", edgeFilteringMode.getMaterialParamValue());
            material.setFloat("PCFEdge", edgeThickness);
            material.setFloat("ShadowIntensity", shadowIntensity);

            // FadeInfo?

            setMaterialParameters(material);
        }

        if (needsfallBackMaterial) {
            setPostShadowParams();
        }
    }

    private void setPostShadowParams() {
        setMaterialParameters(postshadowMaterial);
        for (int i = 0; i < nbSplits; i++) {
            postshadowMaterial.setMatrix4(lightViewStringCache[i], lightViewProjectionMatrices[i]);
            postshadowMaterial.setTexture(shadowMapStringCache[i], shadowMaps[i]);
        }

        // FadeInfo?
        postshadowMaterial.setBoolean("BackfaceShadows", renderBackFacesShadows);
    }

    /**
     * This method is called once per frame and is responsible for clearing any material parameters
     * that subclasses may need to clear on the post material.
     *
     * @param material the material that was used for post shadow pass
     */
    protected abstract void clearMaterialParameters(Material material);

    /**
     * This method is called once per frame and is reponsible for setting material parameters that
     * subclasses may need to set on the post material.
     *
     * @param material the material to use for the post shadow pass
     */
    protected abstract void setMaterialParameters(Material material);

    protected abstract void getReceivers(GeometryList lightReceivers);

    /**
     * Debug function to create a visible frustum
     */
    protected Geometry createFrustum(Vector3f[] points, int index) {
        WireFrustum frustum = new WireFrustum(points);
        Geometry frustumModel = new Geometry("f", frustum);
        frustumModel.setCullHint(Spatial.CullHint.Never);
        frustumModel.setShadowMode(RenderQueue.ShadowMode.Off);
        Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        material.getAdditionalRenderState().setWireframe(true);
        frustumModel.setMaterial(material);

        switch (index) {
            case 0:
                material.setColor("Color", ColorRGBA.Pink);
                break;
            case 1:
                material.setColor("Color", ColorRGBA.Red);
                break;
            case 2:
                material.setColor("Color", ColorRGBA.Green);
                break;
            case 3:
                material.setColor("Color", ColorRGBA.Blue);
                break;
            default:
                material.setColor("Color", ColorRGBA.White);
        }

        frustumModel.updateGeometricState();
        return frustumModel;
    }

    private void buildMatCache(GeometryList list) {
        materialCache.clear();
        for (int i = 0; i < list.size(); i++) {
            Material mat = list.get(i).getMaterial();
            if (mat.getMaterialDef().getTechniqueDefs(postTechniqueName) != null) {
                if (!materialCache.contains(mat)) {
                    materialCache.add(mat);
                }
            } else {
                needsfallBackMaterial = true;
            }
        }
    }

    /**
     * For debugging purposes, display depth shadow maps.
     */
    private void displayShadowMap() {
        Camera cam = viewPort.getCamera();
        renderManager.setCamera(cam, true);
        int height = cam.getHeight();
        for (int i = 0; i < displayPictures.length; i++) {
            Picture displayPicture = displayPictures[i];
            displayPicture.setPosition((128 * i) + (150 + 64 * (i + 1)), height / 20f);
            displayPicture.setWidth(128);
            displayPicture.setHeight(128);
            displayPicture.updateGeometricState();
            renderManager.renderGeometry(displayPicture);
        }
        renderManager.setCamera(cam, false);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
    }

    @Override
    public void preFrame() {
    }

    @Override
    public Object miniClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void cloneFields(final Cloner cloner, final Object original) {
        forcedRenderState = cloner.clone(forcedRenderState);
        init();
    }
}
