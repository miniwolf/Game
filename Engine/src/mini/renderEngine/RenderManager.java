package mini.renderEngine;

import mini.light.LightList;
import mini.material.Material;
import mini.material.MaterialDef;
import mini.material.RenderState;
import mini.material.Technique;
import mini.material.TechniqueDef;
import mini.math.Matrix4f;
import mini.math.Vector3f;
import mini.post.SceneProcessor;
import mini.renderEngine.opengl.GLRenderer;
import mini.renderEngine.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.shaders.ShaderProgram;
import mini.shaders.UniformBinding;
import mini.shaders.UniformBindingManager;
import mini.utils.Camera;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miniwolf on 30-04-2017.
 */
public class RenderManager {
    private final UniformBindingManager uniformBindingManager = new UniformBindingManager();
    private final List<ViewPort> viewPorts = new ArrayList<>();
    private final List<ViewPort> preViewPorts = new ArrayList<>();
    private final GLRenderer renderer;
    private Camera prevCam = null;
    private int viewX, viewY, viewWidth, viewHeight;
    private final Matrix4f orthoMatrix = new Matrix4f();

    /**
     * Create a high-level rendering interface over the
     * low-level rendering interface.
     *
     * @param renderer
     */
    public RenderManager(GLRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Creates a new main ViewPort, to display the given camera's content.
     * <p>
     * The view will be processed before the post viewports but after
     * the pre viewports.
     */
    public ViewPort createMainView(String viewName, CameraImpl cam) {
        ViewPort vp = new ViewPort(viewName, cam);
        viewPorts.add(vp);
        return vp;
    }

    /**
     * Internal use only. Sets the world matrix to use for future
     * rendering. This has no effect unless objects are rendered manually
     * using {@link Material#render(RenderManager, Geometry) }.
     * Using {@link #renderGeometry(Geometry) } will
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
     * Set the camera to use for rendering.
     * <p>
     * First, the camera's
     * {@link CameraImpl#setViewPort(float, float, float, float) view port parameters}
     * are applied. Then, the camera's {@link CameraImpl#getViewMatrix() view} and
     * {@link CameraImpl#getProjectionMatrix() projection} matrices are set
     * on the renderer. If <code>ortho</code> is <code>true</code>, then
     * instead of using the camera's view and projection matrices, an ortho
     * matrix is computed and used instead of the view projection matrix.
     * The ortho matrix converts from the range (0 ~ Width, 0 ~ Height, -1 ~ +1)
     * to the clip range (-1 ~ +1, -1 ~ +1, -1 ~ +1).
     *
     * @param cam The camera to set
     */
    public void setCamera(CameraImpl cam) {
        // Tell the light filter which camera to use for filtering. TODO
//        if (lightFilter != null) {
//            lightFilter.setCamera(cam);
//        }
        setViewPort(cam);
        setViewProjection(cam);
    }

    private void setViewPort(CameraImpl cam) {
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

    private void setViewProjection(Camera cam) {
        uniformBindingManager.setCamera(cam);
    }

    /**
     * The renderer implementation used for rendering operations.
     *
     * @return The renderer implementation
     * @see #RenderManager(GLRenderer)
     * @see GLRenderer
     */
    public GLRenderer getRenderer() {
        return renderer;
    }

    /**
     * Flushes the ViewPort's {@link ViewPort#getQueue() render queue}
     * by rendering each of its visible buckets.
     * By default the queues will automatically be cleared after rendering,
     * so there's no need to clear them manually.
     *
     * @param vp The ViewPort of which the queue will be flushed
     * @see RenderQueue#renderQueue(RenderQueue.Bucket, RenderManager, CameraImpl)
     * @see #renderGeometryList(GeometryList)
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

    public void setLightDir(Vector3f lightDir) {
        uniformBindingManager.setLightDir(lightDir);
    }

    /**
     * Renders the {@link ViewPort}.
     * <p>
     * If the ViewPort is {@link ViewPort#isEnabled() disabled}, this method
     * returns immediately. Otherwise, the ViewPort is rendered by
     * the following process:<br>
     * <ul>
     * <li>All {@link SceneProcessor scene processors} that are attached
     * to the ViewPort are {@link SceneProcessor#initialize(RenderManager, ViewPort) initialized}.
     * </li>
     * <li>The SceneProcessors' {@link SceneProcessor#preFrame(float) } method
     * is called.</li>
     * <li>The ViewPort's {@link ViewPort#getOutputFrameBuffer() output framebuffer}
     * is set on the Renderer</li>
     * <li>The camera is set on the renderer, including its view port parameters.
     * (see {@link #setCamera(CameraImpl) })</li>
     * <li>Any buffers that the ViewPort requests to be cleared are cleared
     * and the {@link ViewPort#getBackgroundColor() background color} is set</li>
     * <li>Every scene that is attached to the ViewPort is flattened into
     * the ViewPort's render queue
     * (see {@link #renderViewPortQueues(ViewPort) })
     * </li>
     * <li>The SceneProcessors' {@link SceneProcessor#postQueue(RenderQueue) }
     * method is called.</li>
     * <li>The render queue is sorted and then flushed, sending
     * rendering commands to the underlying Renderer implementation.
     * (see {@link #flushQueue(ViewPort) })</li>
     * <li>The SceneProcessors' {@link SceneProcessor#postFrame(FrameBuffer) }
     * method is called.</li>
     * <li>The translucent queue of the ViewPort is sorted and then flushed
     * (see {@link #renderTranslucentQueue(ViewPort) })</li>
     * <li>If any objects remained in the render queue, they are removed
     * from the queue. This is generally objects added to the
     * {@link RenderQueue#renderShadowQueue(RenderQueue.ShadowMode, RenderManager, CameraImpl, boolean)
     * shadow queue}
     * which were not rendered because of a missing shadow renderer.</li>
     * </ul>
     *
     * @param vp  View port to render
     * @param tpf Time per frame value
     */
    public void renderViewPort(ViewPort vp) {
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
                proc.preFrame();
            }
        }

        renderer.setFrameBuffer(vp.getOutputFrameBuffer());
        setCamera(vp.getCamera());
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
        // TODO: Translucent
        //renderTranslucentQueue(vp);
        // clear any remaining spatials that were not rendered.
        clearQueue(vp);
    }

    /**
     * Called by the application to render any ViewPorts
     * added to this RenderManager.
     * <p>
     * Renders any viewports that were added using the following methods:
     * <ul>
     * <li>{@link #createMainView(java.lang.String, Camera) }</li>
     * </ul>
     */
    public void render(boolean mainFrameBufferActive) {
//        for (ViewPort vp : preViewPorts) {
//            if (vp.getOutputFrameBuffer() != null || mainFrameBufferActive) {
//                renderViewPort(vp);
//            }
//        }

        for (ViewPort vp : viewPorts) {
            if (vp.getOutputFrameBuffer() != null || mainFrameBufferActive) {
                renderViewPort(vp);
            }
        }

//        for (int i = 0; i < postViewPorts.size(); i++) {
//            ViewPort vp = postViewPorts.get(i);
//            if (vp.getOutputFrameBuffer() != null || mainFrameBufferActive) {
//                renderViewPort(vp);
//            }
//        }
    }

    /**
     * Render the given viewport queues.
     * <p>
     * Changes the {@link GLRenderer#setDepthRange(float, float) depth range}
     * appropriately as expected by each queue and then calls
     * {@link RenderQueue#renderQueue(RenderQueue.Bucket, RenderManager, Camera, boolean) }
     * on the queue. Makes sure to restore the depth range to [0, 1]
     * at the end of the call.
     * Note that the {@link RenderQueue.Bucket#Translucent translucent bucket} is NOT
     * rendered by this method. Instead the user should call
     * {@link #renderTranslucentQueue(ViewPort) }
     * after this call.
     *
     * @param vp    the viewport of which queue should be rendered
     * @param flush If true, the queues will be cleared after
     *              rendering.
     * @see RenderQueue
     * @see #renderTranslucentQueue(ViewPort)
     */
    public void renderViewPortQueues(ViewPort vp, boolean flush) {
        RenderQueue rq = vp.getQueue();
        CameraImpl cam = vp.getCamera();
        boolean depthRangeChanged = false;

        // render opaque objects with default depth range
        // opaque objects are sorted front-to-back, reducing overdraw
        rq.renderQueue(RenderQueue.Bucket.Opaque, this, cam, flush);

        // render the sky, with depth range set to the farthest
        // TODO: SKy rendering
//        if (!rq.isQueueEmpty(RenderQueue.Bucket.Sky)) {
//            renderer.setDepthRange(1, 1);
//            rq.renderQueue(RenderQueue.Bucket.Sky, this, cam, flush);
//            depthRangeChanged = true;
//        }

        // transparent objects are last because they require blending with the
        // rest of the scene's objects. Consequently, they are sorted
        // back-to-front.
        // TODO: Transparent
//        if (!rq.isQueueEmpty(RenderQueue.Bucket.Transparent)) {
//            if (depthRangeChanged) {
//                renderer.setDepthRange(0, 1);
//                depthRangeChanged = false;
//            }
//
//            rq.renderQueue(Bucket.Transparent, this, cam, flush);
//        }

        // TODO: GUI
//        if (!rq.isQueueEmpty(Bucket.Gui)) {
//            renderer.setDepthRange(0, 0);
//            setCamera(cam, true);
//            rq.renderQueue(Bucket.Gui, this, cam, flush);
//            setCamera(cam, false);
//            depthRangeChanged = true;
//        }

        // restore range to default
        if (depthRangeChanged) {
            renderer.setDepthRange(0, 1);
        }
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
     * If a {@link #setForcedMaterial(Material) forced material} is
     * set on this RenderManager, then it is used for rendering the geometry,
     * otherwise, the {@link Geometry#getMaterial() geometry's material} is used.
     * <p>
     * If a {@link #setForcedTechnique(java.lang.String) forced technique} is
     * set on this RenderManager, then it is selected automatically
     * on the geometry's material and is used for rendering. Otherwise, one
     * of the {@link MaterialDef#getDefaultTechniques() default techniques} is
     * used.
     * <p>
     *
     * @param geom The geometry to render
     * @see Technique
     * @see RenderState
     * @see Material#render(RenderManager, Geometry)
     */
    public void renderGeometry(Geometry geom) {
        setWorldMatrix(geom.getWorldMatrix());

        // Perform light filtering if we have a light filter.
        LightList lightList = geom.getWorldLightList();

        // TODO: LightFilter
//        if (lightFilter != null) {
//            filteredLightList.clear();
//            lightFilter.filterLights(geom, filteredLightList);
//            lightList = filteredLightList;
//        }

        Material material = geom.getMaterial();

        material.render(geom, lightList, this);
    }

    /**
     * Flattens the given scene graph into the ViewPort's RenderQueue,
     * checking for culling as the call goes down the graph recursively.
     * <p>
     * First, the scene is checked for culling based on the <code>Spatial</code>s
     * {@link Spatial#setCullHint(Spatial.CullHint) cull hint},
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
     * {@link RenderQueue#addToShadowQueue(Geometry, RenderQueue.ShadowMode)
     * shadow queue}. Each Spatial which has its
     * {@link Spatial#setShadowMode(RenderQueue.ShadowMode) shadow mode}
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

        if (scene instanceof Node) {
            // Recurse for all children
            Node n = (Node) scene;
            List<Spatial> children = n.getChildren();
            // Saving cam state for culling
            int camState = vp.getCamera().getPlaneState();
            for (Spatial aChildren : children) {
                // Restoring cam state before proceeding children recursively
                vp.getCamera().setPlaneState(camState);
                renderSubScene(aChildren, vp);
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
}
