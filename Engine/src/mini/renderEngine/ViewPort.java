package mini.renderEngine;

import mini.math.ColorRGBA;
import mini.post.SceneProcessor;
import mini.renderEngine.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Spatial;
import mini.textures.FrameBuffer;
import mini.utils.Camera;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>ViewPort</code> represents a view inside the display
 * window or a {@link FrameBuffer} to which scenes will be rendered.
 * <p>
 * A viewport has a {@link #ViewPort(java.lang.String, Camera) camera}
 * which is used to render a set of {@link #attachScene(Spatial) scenes}.
 * A view port has a location on the screen as set by the
 * {@link Camera#setViewPort(float, float, float, float) } method.
 * By default, a view port does not clear the framebuffer, but it can be
 * set to {@link #setClearFlags(boolean, boolean, boolean) clear the framebuffer}.
 * The background color which the color buffer is cleared to can be specified
 * via the {@link #setBackgroundColor(ColorRGBA)} method.
 * <p>
 * A ViewPort has a list of {@link SceneProcessor}s which can
 * control how the ViewPort is rendered by the {@link RenderManager}.
 *
 * @author minwolf
 * @see RenderManager
 * @see Spatial
 * @see CameraImpl
 */
public class ViewPort {
    protected final String name;
    protected final CameraImpl cam;
    protected final RenderQueue queue = new RenderQueue();
    protected final List<Spatial> sceneList = new ArrayList<>();
    protected final List<SceneProcessor> processors = new ArrayList<>();
    protected FrameBuffer out = null;

    protected final ColorRGBA backColor = new ColorRGBA(0,0,0,0);
    protected boolean clearDepth = false, clearColor = false, clearStencil = false;
    private boolean enabled = true;

    /**
     * Create a new viewport. User code should generally use these methods instead:<br>
     * <ul>
     * <li>{@link RenderManager#createMainView(java.lang.String, CameraImpl)  }</li>
     * </ul>
     *
     * @param name The name of the viewport. Used for debugging only.
     * @param cam The camera through which the viewport is rendered. The camera
     * cannot be swapped to a different one after creating the viewport.
     */
    public ViewPort(String name, CameraImpl cam) {
        this.name = name;
        this.cam = cam;
    }

    /**
     * Returns the name of the viewport as set in the constructor.
     *
     * @return the name of the viewport
     *
     * @see #ViewPort(java.lang.String, CameraImpl)
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link SceneProcessor scene processors} that were
     * added to this <code>ViewPort</code>
     *
     * @return the list of processors attached to this ViewPort
     *
     * @see #addProcessor(SceneProcessor)
     */
    public List<SceneProcessor> getProcessors(){
        return processors;
    }

    /**
     * Adds a {@link SceneProcessor} to this ViewPort.
     * <p>
     * SceneProcessors that are added to the ViewPort will be notified
     * of events as the ViewPort is being rendered by the {@link RenderManager}.
     *
     * @param processor The processor to add
     *
     * @see SceneProcessor
     */
    public void addProcessor(SceneProcessor processor){
        if (processor == null) {
            throw new IllegalArgumentException( "Processor cannot be null." );
        }
        processors.add(processor);
    }

    /**
     * Removes a {@link SceneProcessor} from this ViewPort.
     * <p>
     * The processor will no longer receive events occurring to this ViewPort.
     *
     * @param processor The processor to remove
     *
     * @see SceneProcessor
     */
    public void removeProcessor(SceneProcessor processor){
        if (processor == null) {
            throw new IllegalArgumentException( "Processor cannot be null." );
        }
        processors.remove(processor);
        processor.cleanup();
    }

    /**
     * Removes all {@link SceneProcessor scene processors} from this
     * ViewPort. 
     *
     * @see SceneProcessor
     */
    public void clearProcessors() {
        for (SceneProcessor proc : processors) {
            proc.cleanup();
        }
        processors.clear();
    }

    /**
     * Check if depth buffer clearing is enabled.
     *
     * @return true if depth buffer clearing is enabled.
     *
     * @see #setClearDepth(boolean)
     */
    public boolean isClearDepth() {
        return clearDepth;
    }

    /**
     * Enable or disable clearing of the depth buffer for this ViewPort.
     * <p>
     * By default depth clearing is disabled.
     *
     * @param clearDepth Enable/disable depth buffer clearing.
     */
    public void setClearDepth(boolean clearDepth) {
        this.clearDepth = clearDepth;
    }

    /**
     * Check if color buffer clearing is enabled.
     *
     * @return true if color buffer clearing is enabled.
     *
     * @see #setClearColor(boolean)
     */
    public boolean isClearColor() {
        return clearColor;
    }

    /**
     * Enable or disable clearing of the color buffer for this ViewPort.
     * <p>
     * By default color clearing is disabled.
     *
     * @param clearColor Enable/disable color buffer clearing.
     */
    public void setClearColor(boolean clearColor) {
        this.clearColor = clearColor;
    }

    /**
     * Check if stencil buffer clearing is enabled.
     *
     * @return true if stencil buffer clearing is enabled.
     *
     * @see #setClearStencil(boolean)
     */
    public boolean isClearStencil() {
        return clearStencil;
    }

    /**
     * Enable or disable clearing of the stencil buffer for this ViewPort.
     * <p>
     * By default stencil clearing is disabled.
     *
     * @param clearStencil Enable/disable stencil buffer clearing.
     */
    public void setClearStencil(boolean clearStencil) {
        this.clearStencil = clearStencil;
    }

    /**
     * Set the clear flags (color, depth, stencil) in one call.
     *
     * @param color If color buffer clearing should be enabled.
     * @param depth If depth buffer clearing should be enabled.
     * @param stencil If stencil buffer clearing should be enabled.
     *
     * @see #setClearColor(boolean)
     * @see #setClearDepth(boolean)
     * @see #setClearStencil(boolean)
     */
    public void setClearFlags(boolean color, boolean depth, boolean stencil){
        this.clearColor = color;
        this.clearDepth = depth;
        this.clearStencil = stencil;
    }

    /**
     * Returns the framebuffer where this ViewPort's scenes are
     * rendered to.
     *
     * @return the framebuffer where this ViewPort's scenes are
     * rendered to.
     *
     * @see #setOutputFrameBuffer(FrameBuffer)
     */
    public FrameBuffer getOutputFrameBuffer() {
        return out;
    }

    /**
     * Sets the output framebuffer for the ViewPort.
     * <p>
     * The output framebuffer specifies where the scenes attached
     * to this ViewPort are rendered to. By default this is <code>null</code>
     * which indicates the scenes are rendered to the display window.
     *
     * @param out The framebuffer to render scenes to, or null if to render
     * to the screen.
     */
    public void setOutputFrameBuffer(FrameBuffer out) {
        this.out = out;
    }

    /**
     * Returns the camera which renders the attached scenes.
     *
     * @return the camera which renders the attached scenes.
     *
     * @see CameraImpl
     */
    public CameraImpl getCamera() {
        return cam;
    }

    /**
     * Internal use only.
     */
    public RenderQueue getQueue() {
        return queue;
    }

    /**
     * Attaches a new scene to render in this ViewPort.
     *
     * @param scene The scene to attach
     *
     * @see Spatial
     */
    public void attachScene(Spatial scene){
        if (scene == null) {
            throw new IllegalArgumentException( "Scene cannot be null." );
        }
        sceneList.add(scene);
        if (scene instanceof Geometry) {
            scene.forceRefresh(true, false, true);
        }
    }

    /**
     * Detaches a scene from rendering.
     *
     * @param scene The scene to detach
     *
     * @see #attachScene(Spatial)
     */
    public void detachScene(Spatial scene){
        if (scene == null) {
            throw new IllegalArgumentException( "Scene cannot be null." );
        }
        sceneList.remove(scene);
        if (scene instanceof Geometry) {
            scene.forceRefresh(true, false, true);
        }
    }

    /**
     * Removes all attached scenes.
     *
     * @see #attachScene(Spatial)
     */
    public void clearScenes() {
        sceneList.clear();
    }

    /**
     * Returns a list of all attached scenes.
     *
     * @return a list of all attached scenes.
     *
     * @see #attachScene(Spatial)
     */
    public List<Spatial> getScenes(){
        return sceneList;
    }

    /**
     * Sets the background color.
     * <p>
     * When the ViewPort's color buffer is cleared 
     * (if {@link #setClearColor(boolean) color clearing} is enabled), 
     * this specifies the color to which the color buffer is set to.
     * By default the background color is black without alpha.
     *
     * @param background the background color.
     */
    public void setBackgroundColor(ColorRGBA background){
        backColor.set(background);
    }

    /**
     * Returns the background color of this ViewPort
     *
     * @return the background color of this ViewPort
     *
     * @see #setBackgroundColor(ColorRGBA)
     */
    public ColorRGBA getBackgroundColor(){
        return backColor;
    }

    /**
     * Enable or disable this ViewPort.
     * <p>
     * Disabled ViewPorts are skipped by the {@link RenderManager} when
     * rendering. By default all ViewPorts are enabled.
     *
     * @param enable If the viewport should be disabled or enabled.
     */
    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    /**
     * Returns true if the viewport is enabled, false otherwise.
     * @return true if the viewport is enabled, false otherwise.
     * @see #setEnabled(boolean)
     */
    public boolean isEnabled() {
        return enabled;
    }
}
