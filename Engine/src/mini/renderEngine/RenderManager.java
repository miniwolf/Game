package mini.renderEngine;

import mini.light.DefaultLightFilter;
import mini.light.LightFilter;
import mini.light.LightList;
import mini.material.*;
import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.post.SceneProcessor;
import mini.renderEngine.opengl.GLRenderer;
import mini.renderEngine.queue.GeometryList;
import mini.renderEngine.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.VertexBuffer;
import mini.shaders.ShaderProgram;
import mini.shaders.UniformBinding;
import mini.shaders.UniformBindingManager;
import mini.system.NullRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <code>RenderManager</code> is a high-level rendering interface that is
 * above the Renderer implementation. RenderManager takes care
 * of rendering the scene graphs attached to each viewport and
 * handling SceneProcessors.
 *
 * @see SceneProcessor
 * @see ViewPort
 * @see Spatial
 */
public class RenderManager {

    private final Renderer renderer;
    private final UniformBindingManager uniformBindingManager = new UniformBindingManager();
    private final ArrayList<ViewPort> preViewPorts = new ArrayList<>();
    private final ArrayList<ViewPort> viewPorts = new ArrayList<>();
    private final ArrayList<ViewPort> postViewPorts = new ArrayList<>();
    private Camera prevCam = null;
    private Material forcedMaterial = null;
    private String forcedTechnique = null;
    private RenderState forcedRenderState = null;
    private final List<MatParamOverride> forcedOverrides = new ArrayList<>();
    private int viewX, viewY, viewWidth, viewHeight;
    private final Matrix4f orthoMatrix = new Matrix4f();
    private final LightList filteredLightList = new LightList(null);
    private boolean handleTranlucentBucket = true;
    private LightFilter lightFilter = new DefaultLightFilter();
    private TechniqueDef.LightMode preferredLightMode = TechniqueDef.LightMode.MultiPass;
    private int singlePassLightBatchSize = 1;

    /**
     * Create a high-level rendering interface over the
     * low-level rendering interface.
     *
     * @param renderer
     */
    public RenderManager(Renderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Returns the pre ViewPort with the given name.
     *
     * @param viewName The name of the pre ViewPort to look up
     * @return The ViewPort, or null if not found.
     * @see #createPreView(java.lang.String, mini.renderer.Camera)
     */
    public ViewPort getPreView(String viewName) {
        for (ViewPort preViewPort : preViewPorts) {
            if (preViewPort.getName().equals(viewName)) {
                return preViewPort;
            }
        }
        return null;
    }

    /**
     * Removes the pre ViewPort with the specified name.
     *
     * @param viewName The name of the pre ViewPort to remove
     * @return True if the ViewPort was removed successfully.
     * @see #createPreView(java.lang.String, mini.renderer.Camera)
     */
    public boolean removePreView(String viewName) {
        for (int i = 0; i < preViewPorts.size(); i++) {
            if (preViewPorts.get(i).getName().equals(viewName)) {
                preViewPorts.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified pre ViewPort.
     *
     * @param view The pre ViewPort to remove
     * @return True if the ViewPort was removed successfully.
     * @see #createPreView(java.lang.String, mini.renderer.Camera)
     */
    public boolean removePreView(ViewPort view) {
        return preViewPorts.remove(view);
    }

    /**
     * Returns the main ViewPort with the given name.
     *
     * @param viewName The name of the main ViewPort to look up
     * @return The ViewPort, or null if not found.
     * @see #createMainView(java.lang.String, mini.renderer.Camera)
     */
    public ViewPort getMainView(String viewName) {
        for (ViewPort viewPort : viewPorts) {
            if (viewPort.getName().equals(viewName)) {
                return viewPort;
            }
        }
        return null;
    }

    /**
     * Removes the main ViewPort with the specified name.
     *
     * @param viewName The main ViewPort name to remove
     * @return True if the ViewPort was removed successfully.
     * @see #createMainView(java.lang.String, mini.renderer.Camera)
     */
    public boolean removeMainView(String viewName) {
        for (int i = 0; i < viewPorts.size(); i++) {
            if (viewPorts.get(i).getName().equals(viewName)) {
                viewPorts.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified main ViewPort.
     *
     * @param view The main ViewPort to remove
     * @return True if the ViewPort was removed successfully.
     * @see #createMainView(java.lang.String, mini.renderer.Camera)
     */
    public boolean removeMainView(ViewPort view) {
        return viewPorts.remove(view);
    }

    /**
     * Returns the post ViewPort with the given name.
     *
     * @param viewName The name of the post ViewPort to look up
     * @return The ViewPort, or null if not found.
     * @see #createPostView(java.lang.String, mini.renderer.Camera)
     */
    public ViewPort getPostView(String viewName) {
        for (ViewPort postViewPort : postViewPorts) {
            if (postViewPort.getName().equals(viewName)) {
                return postViewPort;
            }
        }
        return null;
    }

    /**
     * Removes the post ViewPort with the specified name.
     *
     * @param viewName The post ViewPort name to remove
     * @return True if the ViewPort was removed successfully.
     * @see #createPostView(java.lang.String, mini.renderer.Camera)
     */
    public boolean removePostView(String viewName) {
        for (int i = 0; i < postViewPorts.size(); i++) {
            if (postViewPorts.get(i).getName().equals(viewName)) {
                postViewPorts.remove(i);

                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified post ViewPort.
     *
     * @param view The post ViewPort to remove
     * @return True if the ViewPort was removed successfully.
     * @see #createPostView(java.lang.String, mini.renderer.Camera)
     */
    public boolean removePostView(ViewPort view) {
        return postViewPorts.remove(view);
    }

    /**
     * Returns a read-only list of all pre ViewPorts
     *
     * @return a read-only list of all pre ViewPorts
     * @see #createPreView(java.lang.String, mini.renderer.Camera)
     */
    public List<ViewPort> getPreViews() {
        return Collections.unmodifiableList(preViewPorts);
    }

    /**
     * Returns a read-only list of all main ViewPorts
     *
     * @return a read-only list of all main ViewPorts
     * @see #createMainView(java.lang.String, mini.renderer.Camera)
     */
    public List<ViewPort> getMainViews() {
        return Collections.unmodifiableList(viewPorts);
    }

    /**
     * Returns a read-only list of all post ViewPorts
     *
     * @return a read-only list of all post ViewPorts
     * @see #createPostView(java.lang.String, mini.renderer.Camera)
     */
    public List<ViewPort> getPostViews() {
        return Collections.unmodifiableList(postViewPorts);
    }

    /**
     * Creates a new pre ViewPort, to display the given camera's content.
     * <p>
     * The view will be processed before the main and post viewports.
     */
    public ViewPort createPreView(String viewName, Camera cam) {
        ViewPort vp = new ViewPort(viewName, cam);
        preViewPorts.add(vp);
        return vp;
    }

    /**
     * Creates a new main ViewPort, to display the given camera's content.
     * <p>
     * The view will be processed before the post viewports but after
     * the pre viewports.
     */
    public ViewPort createMainView(String viewName, Camera cam) {
        ViewPort vp = new ViewPort(viewName, cam);
        viewPorts.add(vp);
        return vp;
    }

    /**
     * Creates a new post ViewPort, to display the given camera's content.
     * <p>
     * The view will be processed after the pre and main viewports.
     */
    public ViewPort createPostView(String viewName, Camera cam) {
        ViewPort vp = new ViewPort(viewName, cam);
        postViewPorts.add(vp);
        return vp;
    }

    private void notifyReshape(ViewPort vp, int w, int h) {
        List<SceneProcessor> processors = vp.getProcessors();
        for (SceneProcessor proc : processors) {
            if (!proc.isInitialized()) {
                proc.initialize(this, vp);
            } else {
                proc.reshape(vp, w, h);
            }
        }
    }

    /**
     * Internal use only.
     * Updates the resolution of all on-screen cameras to match
     * the given width and height.
     */
    public void notifyReshape(int w, int h) {
        for (ViewPort vp : preViewPorts) {
            if (vp.getOutputFrameBuffer() == null) {
                Camera cam = vp.getCamera();
                cam.resize(w, h, true);
            }
            notifyReshape(vp, w, h);
        }
        for (ViewPort vp : viewPorts) {
            if (vp.getOutputFrameBuffer() == null) {
                Camera cam = vp.getCamera();
                cam.resize(w, h, true);
            }
            notifyReshape(vp, w, h);
        }
        for (ViewPort vp : postViewPorts) {
            if (vp.getOutputFrameBuffer() == null) {
                Camera cam = vp.getCamera();
                cam.resize(w, h, true);
            }
            notifyReshape(vp, w, h);
        }
    }

    /**
     * Set the material to use to render all future objects.
     * This overrides the material set on the geometry and renders
     * with the provided material instead.
     * Use null to clear the material and return renderer to normal
     * functionality.
     *
     * @param mat The forced material to set, or null to return to normal
     */
    public void setForcedMaterial(Material mat) {
        forcedMaterial = mat;
    }

    /**
     * Returns the forced render state previously set with
     * {@link #setForcedRenderState(mini.material.RenderState) }.
     *
     * @return the forced render state
     */
    public RenderState getForcedRenderState() {
        return forcedRenderState;
    }

    /**
     * Set the render state to use for all future objects.
     * This overrides the render state set on the material and instead
     * forces this render state to be applied for all future materials
     * rendered. Set to null to return to normal functionality.
     *
     * @param forcedRenderState The forced render state to set, or null
     *                          to return to normal
     */
    public void setForcedRenderState(RenderState forcedRenderState) {
        this.forcedRenderState = forcedRenderState;
    }

    /**
     * Returns the forced technique name set.
     *
     * @return the forced technique name set.
     * @see #setForcedTechnique(java.lang.String)
     */
    public String getForcedTechnique() {
        return forcedTechnique;
    }

    /**
     * Sets the forced technique to use when rendering geometries.
     * <p>
     * If the specified technique name is available on the geometry's
     * material, then it is used, otherwise, the
     * {@link #setForcedMaterial(mini.material.Material) forced material} is used.
     * If a forced material is not set and the forced technique name cannot
     * be found on the material, the geometry will <em>not</em> be rendered.
     *
     * @param forcedTechnique The forced technique name to use, set to null
     *                        to return to normal functionality.
     * @see #renderGeometry(mini.scene.Geometry)
     */
    public void setForcedTechnique(String forcedTechnique) {
        this.forcedTechnique = forcedTechnique;
    }

    /**
     * Adds a forced material parameter to use when rendering geometries.
     * <p>
     * The provided parameter takes precedence over parameters set on the
     * material or any overrides that exist in the scene graph that have the
     * same name.
     *
     * @param override The override to add
     * @see MatParamOverride
     * @see #removeForcedMatParam(mini.material.MatParamOverride)
     */
    public void addForcedMatParam(MatParamOverride override) {
        forcedOverrides.add(override);
    }

    /**
     * Remove a forced material parameter previously added.
     *
     * @param override The override to remove.
     * @see #addForcedMatParam(mini.material.MatParamOverride)
     */
    public void removeForcedMatParam(MatParamOverride override) {
        forcedOverrides.remove(override);
    }

    /**
     * Get the forced material parameters applied to rendered geometries.
     * <p>
     * Forced parameters can be added via
     * {@link #addForcedMatParam(mini.material.MatParamOverride)} or removed
     * via {@link #removeForcedMatParam(mini.material.MatParamOverride)}.
     *
     * @return The forced material parameters.
     */
    public List<MatParamOverride> getForcedMatParams() {
        return forcedOverrides;
    }

    /**
     * Enable or disable alpha-to-coverage.
     * <p>
     * When alpha to coverage is enabled and the renderer implementation
     * supports it, then alpha blending will be replaced with alpha dissolve
     * if multi-sampling is also set on the renderer.
     * This feature allows avoiding of alpha blending artifacts due to
     * lack of triangle-level back-to-front sorting.
     *
     * @param value True to enable alpha-to-coverage, false otherwise.
     */
    public void setAlphaToCoverage(boolean value) {
        renderer.setAlphaToCoverage(value);
    }

    /**
     * True if the translucent bucket should automatically be rendered
     * by the RenderManager.
     *
     * @return Whether or not the translucent bucket is rendered.
     * @see #setHandleTranslucentBucket(boolean)
     */
    public boolean isHandleTranslucentBucket() {
        return handleTranlucentBucket;
    }

    /**
     * Enable or disable rendering of the
     * {@link Bucket#Translucent translucent bucket}
     * by the RenderManager. The default is enabled.
     *
     * @param handleTranslucentBucket Whether or not the translucent bucket should
     *                                be rendered.
     */
    public void setHandleTranslucentBucket(boolean handleTranslucentBucket) {
        this.handleTranlucentBucket = handleTranslucentBucket;
    }

    /**
     * Internal use only. Sets the world matrix to use for future
     * rendering. This has no effect unless objects are rendered manually
     * using {@link Material#render(mini.scene.Geometry, mini.renderer.RenderManager) }.
     * Using {@link #renderGeometry(mini.scene.Geometry) } will
     * override this value.
     *
     * @param mat The world matrix to set
     */
    public void setWorldMatrix(Matrix4f mat) {
        uniformBindingManager.setWorldMatrix(mat);
    }

    /**
     * Internal use only.
     * Updates the given list of uniforms with {@link UniformBinding uniform bindings}
     * based on the current world state.
     */
    public void updateUniformBindings(ShaderProgram shader) {
        uniformBindingManager.updateUniformBindings(shader);
    }

    /**
     * Renders the given geometry.
     * <p>
     * First the proper world matrix is set, if
     * the geometry's {@link Geometry#setIgnoreTransform(boolean) ignore transform}
     * feature is enabled, the identity world matrix is used, otherwise, the
     * geometry's {@link Geometry#getWorldMatrix() world transform matrix} is used.
     * <p>
     * Once the world matrix is applied, the proper material is chosen for rendering.
     * If a {@link #setForcedMaterial(mini.material.Material) forced material} is
     * set on this RenderManager, then it is used for rendering the geometry,
     * otherwise, the {@link Geometry#getMaterial() geometry's material} is used.
     * <p>
     * If a {@link #setForcedTechnique(java.lang.String) forced technique} is
     * set on this RenderManager, then it is selected automatically
     * on the geometry's material and is used for rendering. Otherwise, one
     * of the {@link MaterialDef#getDefaultTechniques() default techniques} is
     * used.
     * <p>
     * If a {@link #setForcedRenderState(mini.material.RenderState) forced
     * render state} is set on this RenderManager, then it is used
     * for rendering the material, and the material's own render state is ignored.
     * Otherwise, the material's render state is used as intended.
     *
     * @param geom The geometry to render
     * @see Technique
     * @see RenderState
     * @see Material#selectTechnique(java.lang.String, mini.renderer.RenderManager)
     * @see Material#render(mini.scene.Geometry, mini.renderer.RenderManager)
     */
    public void renderGeometry(Geometry geom) {
        if (geom.isIgnoreTransform()) {
            setWorldMatrix(Matrix4f.IDENTITY);
        } else {
            setWorldMatrix(geom.getWorldMatrix());
        }

        // Perform light filtering if we have a light filter.
        LightList lightList = geom.getWorldLightList();

        if (lightFilter != null) {
            filteredLightList.clear();
            lightFilter.filterLights(geom, filteredLightList);
            lightList = filteredLightList;
        }

        Material material = geom.getMaterial();

        //if forcedTechnique we try to force it for render,
        //if it does not exists in the mat def, we check for forcedMaterial and render the geom if not null
        //else the geom is not rendered
        if (forcedTechnique != null) {
            MaterialDef matDef = material.getMaterialDef();
            if (matDef.getTechniqueDefs(forcedTechnique) != null) {

                Technique activeTechnique = material.getActiveTechnique();

                String previousTechniqueName = activeTechnique != null
                                               ? activeTechnique.getDef().getName()
                                               : TechniqueDef.DEFAULT_TECHNIQUE_NAME;

                geom.getMaterial().selectTechnique(forcedTechnique, this);
                //saving forcedRenderState for future calls
                RenderState tmpRs = forcedRenderState;
                if (geom.getMaterial().getActiveTechnique().getDef().getForcedRenderState()
                    != null) {
                    //forcing forced technique renderState
                    forcedRenderState = geom.getMaterial().getActiveTechnique().getDef()
                                            .getForcedRenderState();
                }
                // use geometry's material
                material.render(geom, lightList, this);
                material.selectTechnique(previousTechniqueName, this);

                //restoring forcedRenderState
                forcedRenderState = tmpRs;

                //Reverted this part from revision 6197
                //If forcedTechnique does not exists, and forcedMaterial is not set, the geom MUST NOT be rendered
            } else if (forcedMaterial != null) {
                // use forced material
                forcedMaterial.render(geom, lightList, this);
            }
        } else if (forcedMaterial != null) {
            // use forced material
            forcedMaterial.render(geom, lightList, this);
        } else {
            material.render(geom, lightList, this);
        }
    }

    /**
     * Renders the given GeometryList.
     * <p>
     * For every geometry in the list, the
     * {@link #renderGeometry(mini.scene.Geometry) } method is called.
     *
     * @param gl The geometry list to render.
     * @see GeometryList
     * @see #renderGeometry(mini.scene.Geometry)
     */
    public void renderGeometryList(GeometryList gl) {
        for (int i = 0; i < gl.size(); i++) {
            renderGeometry(gl.get(i));
        }
    }

    /**
     * Preloads a scene for rendering.
     * <p>
     * After invocation of this method, the underlying
     * renderer would have uploaded any textures, shaders and meshes
     * used by the given scene to the video driver.
     * Using this method is useful when wishing to avoid the initial pause
     * when rendering a scene for the first time. Note that it is not
     * guaranteed that the underlying renderer will actually choose to upload
     * the data to the GPU so some pause is still to be expected.
     *
     * @param scene The scene to preload
     */
    public void preloadScene(Spatial scene) {
        if (scene instanceof Node) {
            // recurse for all children
            Node n = (Node) scene;
            List<Spatial> children = n.getChildren();
            for (Spatial child : children) {
                preloadScene(child);
            }
        } else if (scene instanceof Geometry) {
            // add to the render queue
            Geometry gm = (Geometry) scene;
            if (gm.getMaterial() == null) {
                throw new IllegalStateException("No material is set for Geometry: " + gm.getName());
            }

            gm.getMaterial().preload(this);
            Mesh mesh = gm.getMesh();
            if (mesh != null
                && mesh.getVertexCount() != 0
                && mesh.getTriangleCount() != 0) {
                for (VertexBuffer vb : mesh.getBufferList()) {
                    if (vb.getData() != null && vb.getUsage() != VertexBuffer.Usage.CpuOnly) {
                        renderer.updateBufferData(vb);
                    }
                }
            }
        }
    }

    /**
     * Flattens the given scene graph into the ViewPort's RenderQueue,
     * checking for culling as the call goes down the graph recursively.
     * <p>
     * First, the scene is checked for culling based on the <code>Spatial</code>s
     * {@link Spatial#setCullHint(mini.scene.Spatial.CullHint) cull hint},
     * if the camera frustum contains the scene, then this method is recursively
     * called on its children.
     * <p>
     * When the scene's leaves or {@link Geometry geometries} are reached,
     * they are each enqueued into the
     * {@link ViewPort#getQueue() ViewPort's render queue}.
     * <p>
     * In addition to enqueuing the visible geometries, this method
     * also scenes which cast or receive shadows, by putting them into the
     * RenderQueue's
     * {@link RenderQueue#addToShadowQueue(mini.scene.Geometry, mini.renderer.queue.RenderQueue.ShadowMode)
     * shadow queue}. Each Spatial which has its
     * {@link Spatial#setShadowMode(mini.renderer.queue.RenderQueue.ShadowMode) shadow mode}
     * set to not off, will be put into the appropriate shadow queue, note that
     * this process does not check for frustum culling on any
     * {@link ShadowMode#Cast shadow casters}, as they don't have to be
     * in the eye camera frustum to cast shadows on objects that are inside it.
     *
     * @param scene The scene to flatten into the queue
     * @param vp    The ViewPort provides the {@link ViewPort#getCamera() camera}
     *              used for culling and the {@link ViewPort#getQueue() queue} used to
     *              contain the flattened scene graph.
     */
    public void renderScene(Spatial scene, ViewPort vp) {
        //reset of the camera plane state for proper culling (must be 0 for the first note of the scene to be rendered)
        vp.getCamera().setPlaneState(0);
        //rendering the scene
        renderSubScene(scene, vp);
    }

    // recursively renders the scene
    private void renderSubScene(Spatial scene, ViewPort vp) {

        // check culling first.
        if (!scene.checkCulling(vp.getCamera())) {
            return;
        }

        //scene.runControlRender(this, vp);
        if (scene instanceof Node) {
            // Recurse for all children
            Node n = (Node) scene;
            List<Spatial> children = n.getChildren();
            // Saving cam state for culling
            int camState = vp.getCamera().getPlaneState();
            for (Spatial child : children) {
                // Restoring cam state before proceeding children recusively
                vp.getCamera().setPlaneState(camState);
                renderSubScene(child, vp);
            }
        } else if (scene instanceof Geometry) {
            // add to the render queue
            Geometry gm = (Geometry) scene;
            if (gm.getMaterial() == null) {
                throw new IllegalStateException("No material is set for Geometry: " + gm.getName());
            }

            vp.getQueue().addToQueue(gm, scene.getQueueBucket());
        }
    }

    /**
     * Returns the camera currently used for rendering.
     * <p>
     * The camera can be set with {@link #setCamera(mini.renderer.Camera, boolean) }.
     *
     * @return the camera currently used for rendering.
     */
    public Camera getCurrentCamera() {
        return prevCam;
    }

    /**
     * The renderer implementation used for rendering operations.
     *
     * @return The renderer implementation
     * @see #RenderManager(mini.renderer.Renderer)
     * @see Renderer
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Flushes the ViewPort's {@link ViewPort#getQueue() render queue}
     * by rendering each of its visible buckets.
     * By default the queues will automatically be cleared after rendering,
     * so there's no need to clear them manually.
     *
     * @param vp The ViewPort of which the queue will be flushed
     * @see RenderQueue#renderQueue(mini.renderer.queue.RenderQueue.Bucket, mini.renderer.RenderManager, mini.renderer.Camera)
     * @see #renderGeometryList(mini.renderer.queue.GeometryList)
     */
    public void flushQueue(ViewPort vp) {
        renderViewPortQueues(vp, true);
    }

    /**
     * Clears the queue of the given ViewPort.
     * Simply calls {@link RenderQueue#clear() } on the ViewPort's
     * {@link ViewPort#getQueue() render queue}.
     *
     * @param vp The ViewPort of which the queue will be cleared.
     * @see RenderQueue#clear()
     * @see ViewPort#getQueue()
     */
    public void clearQueue(ViewPort vp) {
        vp.getQueue().clear();
    }

    /**
     * Sets the light filter to use when rendering lit Geometries.
     *
     * @param lightFilter The light filter. Set it to null if you want all lights to be rendered.
     * @see LightFilter
     */
    public void setLightFilter(LightFilter lightFilter) {
        this.lightFilter = lightFilter;
    }

    /**
     * Returns the current LightFilter.
     *
     * @return the current light filter
     */
    public LightFilter getLightFilter() {
        return this.lightFilter;
    }

    /**
     * Defines what light mode will be selected when a technique offers several light modes.
     *
     * @param preferredLightMode The light mode to use.
     */
    public void setPreferredLightMode(TechniqueDef.LightMode preferredLightMode) {
        this.preferredLightMode = preferredLightMode;
    }

    /**
     * returns the preferred light mode.
     *
     * @return the light mode.
     */
    public TechniqueDef.LightMode getPreferredLightMode() {
        return preferredLightMode;
    }

    /**
     * returns the number of lights used for each pass when the light mode is single pass.
     *
     * @return the number of lights.
     */
    public int getSinglePassLightBatchSize() {
        return singlePassLightBatchSize;
    }

    /**
     * Sets the number of lights to use for each pass when the light mode is single pass.
     *
     * @param singlePassLightBatchSize the number of lights.
     */
    public void setSinglePassLightBatchSize(int singlePassLightBatchSize) {
        // Ensure the batch size is no less than 1
        this.singlePassLightBatchSize = singlePassLightBatchSize < 1 ? 1 : singlePassLightBatchSize;
    }

    /**
     * Render the given viewport queues.
     * <p>
     * Changes the {@link Renderer#setDepthRange(float, float) depth range}
     * appropriately as expected by each queue and then calls
     * {@link RenderQueue#renderQueue(mini.renderer.queue.RenderQueue.Bucket, mini.renderer.RenderManager, mini.renderer.Camera, boolean) }
     * on the queue. Makes sure to restore the depth range to [0, 1]
     * at the end of the call.
     * Note that the {@link Bucket#Translucent translucent bucket} is NOT
     * rendered by this method. Instead the user should call
     * {@link #renderTranslucentQueue(mini.renderer.ViewPort) }
     * after this call.
     *
     * @param vp    the viewport of which queue should be rendered
     * @param flush If true, the queues will be cleared after
     *              rendering.
     * @see RenderQueue
     * @see #renderTranslucentQueue(mini.renderer.ViewPort)
     */
    public void renderViewPortQueues(ViewPort vp, boolean flush) {
        RenderQueue rq = vp.getQueue();
        Camera cam = vp.getCamera();
        boolean depthRangeChanged = false;

        // render opaque objects with default depth range
        // opaque objects are sorted front-to-back, reducing overdraw
        rq.renderQueue(RenderQueue.Bucket.Opaque, this, cam, flush);

        // render the sky, with depth range set to the farthest
        if (!rq.isQueueEmpty(RenderQueue.Bucket.Sky)) {
            renderer.setDepthRange(1, 1);
            rq.renderQueue(RenderQueue.Bucket.Sky, this, cam, flush);
            depthRangeChanged = true;
        }

        // transparent objects are last because they require blending with the
        // rest of the scene's objects. Consequently, they are sorted
        // back-to-front.
        if (!rq.isQueueEmpty(RenderQueue.Bucket.Transparent)) {
            if (depthRangeChanged) {
                renderer.setDepthRange(0, 1);
                depthRangeChanged = false;
            }

            rq.renderQueue(RenderQueue.Bucket.Transparent, this, cam, flush);
        }

        if (!rq.isQueueEmpty(RenderQueue.Bucket.Gui)) {
            renderer.setDepthRange(0, 0);
            setCamera(cam, true);
            rq.renderQueue(RenderQueue.Bucket.Gui, this, cam, flush);
            setCamera(cam, false);
            depthRangeChanged = true;
        }

        // restore range to default
        if (depthRangeChanged) {
            renderer.setDepthRange(0, 1);
        }
    }

    /**
     * Renders the {@link Bucket#Translucent translucent queue} on the viewPort.
     * <p>
     * This call does nothing unless {@link #setHandleTranslucentBucket(boolean) }
     * is set to true. This method clears the translucent queue after rendering
     * it.
     *
     * @param vp The viewport of which the translucent queue should be rendered.
     * @see #renderViewPortQueues(mini.renderer.ViewPort, boolean)
     * @see #setHandleTranslucentBucket(boolean)
     */
    public void renderTranslucentQueue(ViewPort vp) {
        RenderQueue rq = vp.getQueue();
        if (!rq.isQueueEmpty(RenderQueue.Bucket.Translucent) && handleTranlucentBucket) {
            rq.renderQueue(RenderQueue.Bucket.Translucent, this, vp.getCamera(), true);
        }
    }

    private void setViewPort(Camera cam) {
        // this will make sure to update viewport only if needed
        if (cam != prevCam || cam.isViewportChanged()) {
            viewX = (int) (cam.getViewPortLeft() * cam.getWidth());
            viewY = (int) (cam.getViewPortBottom() * cam.getHeight());
            int viewX2 = (int) (cam.getViewPortRight() * cam.getWidth());
            int viewY2 = (int) (cam.getViewPortTop() * cam.getHeight());
            viewWidth = viewX2 - viewX;
            viewHeight = viewY2 - viewY;
            uniformBindingManager.setViewPort(viewX, viewY, viewWidth, viewHeight);
            renderer.setViewPort(viewX, viewY, viewWidth, viewHeight);
            renderer.setClipRect(viewX, viewY, viewWidth, viewHeight);
            cam.clearViewportChanged();
            prevCam = cam;

//            float translateX = viewWidth == viewX ? 0 : -(viewWidth + viewX) / (viewWidth - viewX);
//            float translateY = viewHeight == viewY ? 0 : -(viewHeight + viewY) / (viewHeight - viewY);
//            float scaleX = viewWidth == viewX ? 1f : 2f / (viewWidth - viewX);
//            float scaleY = viewHeight == viewY ? 1f : 2f / (viewHeight - viewY);
//
//            orthoMatrix.loadIdentity();
//            orthoMatrix.setTranslation(translateX, translateY, 0);
//            orthoMatrix.setScale(scaleX, scaleY, 0);

            orthoMatrix.loadIdentity();
            orthoMatrix.setTranslation(-1f, -1f, 0f);
            orthoMatrix.setScale(2f / cam.getWidth(), 2f / cam.getHeight(), 0f);
        }
    }

    private void setViewProjection(Camera cam, boolean ortho) {
        if (ortho) {
            uniformBindingManager.setCamera(cam, Matrix4f.IDENTITY, orthoMatrix, orthoMatrix);
        } else {
            uniformBindingManager.setCamera(cam, cam.getViewMatrix(), cam.getProjectionMatrix(),
                                            cam.getViewProjectionMatrix());
        }
    }

    /**
     * Set the camera to use for rendering.
     * <p>
     * First, the camera's
     * {@link Camera#setViewPort(float, float, float, float) view port parameters}
     * are applied. Then, the camera's {@link Camera#getViewMatrix() view} and
     * {@link Camera#getProjectionMatrix() projection} matrices are set
     * on the renderer. If <code>ortho</code> is <code>true</code>, then
     * instead of using the camera's view and projection matrices, an ortho
     * matrix is computed and used instead of the view projection matrix.
     * The ortho matrix converts from the range (0 ~ Width, 0 ~ Height, -1 ~ +1)
     * to the clip range (-1 ~ +1, -1 ~ +1, -1 ~ +1).
     *
     * @param cam   The camera to set
     * @param ortho True if to use orthographic projection (for GUI rendering),
     *              false if to use the camera's view and projection matrices.
     */
    public void setCamera(Camera cam, boolean ortho) {
        // Tell the light filter which camera to use for filtering.
        if (lightFilter != null) {
            lightFilter.setCamera(cam);
        }
        setViewPort(cam);
        setViewProjection(cam, ortho);
    }

    /**
     * Draws the viewport but without notifying {@link SceneProcessor scene
     * processors} of any rendering events.
     *
     * @param vp The ViewPort to render
     * @see #renderViewPort(mini.renderer.ViewPort, float)
     */
    public void renderViewPortRaw(ViewPort vp) {
        setCamera(vp.getCamera(), false);
        List<Spatial> scenes = vp.getScenes();
        for (int i = scenes.size() - 1; i >= 0; i--) {
            renderScene(scenes.get(i), vp);
        }
        flushQueue(vp);
    }

    /**
     * Renders the {@link ViewPort}.
     * <p>
     * If the ViewPort is {@link ViewPort#isEnabled() disabled}, this method
     * returns immediately. Otherwise, the ViewPort is rendered by
     * the following process:<br>
     * <ul>
     * <li>All {@link SceneProcessor scene processors} that are attached
     * to the ViewPort are {@link SceneProcessor#initialize(mini.renderer.RenderManager, mini.renderer.ViewPort) initialized}.
     * </li>
     * <li>The SceneProcessors' {@link SceneProcessor#preFrame(float) } method
     * is called.</li>
     * <li>The ViewPort's {@link ViewPort#getOutputFrameBuffer() output framebuffer}
     * is set on the Renderer</li>
     * <li>The camera is set on the renderer, including its view port parameters.
     * (see {@link #setCamera(mini.renderer.Camera, boolean) })</li>
     * <li>Any buffers that the ViewPort requests to be cleared are cleared
     * and the {@link ViewPort#getBackgroundColor() background color} is set</li>
     * <li>Every scene that is attached to the ViewPort is flattened into
     * the ViewPort's render queue
     * (see {@link #renderViewPortQueues(mini.renderer.ViewPort, boolean) })
     * </li>
     * <li>The SceneProcessors' {@link SceneProcessor#postQueue(mini.renderer.queue.RenderQueue) }
     * method is called.</li>
     * <li>The render queue is sorted and then flushed, sending
     * rendering commands to the underlying Renderer implementation.
     * (see {@link #flushQueue(mini.renderer.ViewPort) })</li>
     * <li>The SceneProcessors' {@link SceneProcessor#postFrame(mini.texture.FrameBuffer) }
     * method is called.</li>
     * <li>The translucent queue of the ViewPort is sorted and then flushed
     * (see {@link #renderTranslucentQueue(mini.renderer.ViewPort) })</li>
     * <li>If any objects remained in the render queue, they are removed
     * from the queue. This is generally objects added to the
     * {@link RenderQueue#renderShadowQueue(mini.renderer.queue.RenderQueue.ShadowMode, mini.renderer.RenderManager, mini.renderer.Camera, boolean)
     * shadow queue}
     * which were not rendered because of a missing shadow renderer.</li>
     * </ul>
     *
     * @param vp  View port to render
     * @param tpf Time per frame value
     */
    public void renderViewPort(ViewPort vp, float tpf) {
        if (!vp.isEnabled()) {
            return;
        }

        List<SceneProcessor> processors = vp.getProcessors();
        if (processors.isEmpty()) {
            processors = null;
        }

        if (processors != null) {
            for (SceneProcessor proc : processors) {
                if (!proc.isInitialized()) {
                    proc.initialize(this, vp);
                }
                //proc.preFrame(tpf);
                proc.preFrame();
            }
        }

        renderer.setFrameBuffer(vp.getOutputFrameBuffer());
        setCamera(vp.getCamera(), false);
        if (vp.isClearDepth() || vp.isClearColor() || vp.isClearStencil()) {
            if (vp.isClearColor()) {
                renderer.setBackgroundColor(vp.getBackgroundColor());
            }
            renderer.clearBuffers(vp.isClearColor(),
                                  vp.isClearDepth(),
                                  vp.isClearStencil());
        }

        List<Spatial> scenes = vp.getScenes();
        for (int i = scenes.size() - 1; i >= 0; i--) {
            renderScene(scenes.get(i), vp);
        }

        if (processors != null) {
            for (SceneProcessor proc : processors) {
                proc.postQueue(vp.getQueue());
            }
        }

        flushQueue(vp);

        if (processors != null) {
            for (SceneProcessor proc : processors) {
                proc.postFrame(vp.getOutputFrameBuffer());
            }
        }
        //renders the translucent objects queue after processors have been rendered
        renderTranslucentQueue(vp);
        // clear any remaining spatials that were not rendered.
        clearQueue(vp);
    }

    /**
     * Called by the application to render any ViewPorts
     * added to this RenderManager.
     * <p>
     * Renders any viewports that were added using the following methods:
     * <ul>
     * <li>{@link #createPreView(java.lang.String, mini.renderer.Camera) }</li>
     * <li>{@link #createMainView(java.lang.String, mini.renderer.Camera) }</li>
     * <li>{@link #createPostView(java.lang.String, mini.renderer.Camera) }</li>
     * </ul>
     *
     * @param tpf Time per frame value
     */
    public void render(float tpf, boolean mainFrameBufferActive) {
        if (renderer instanceof NullRenderer) {
            return;
        }

        uniformBindingManager.newFrame();

        for (ViewPort vp : preViewPorts) {
            if (vp.getOutputFrameBuffer() != null || mainFrameBufferActive) {
                renderViewPort(vp, tpf);
            }
        }

        for (ViewPort vp : viewPorts) {
            if (vp.getOutputFrameBuffer() != null || mainFrameBufferActive) {
                renderViewPort(vp, tpf);
            }
        }

        for (ViewPort vp : postViewPorts) {
            if (vp.getOutputFrameBuffer() != null || mainFrameBufferActive) {
                renderViewPort(vp, tpf);
            }
        }
    }
}
