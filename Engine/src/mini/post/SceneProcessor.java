package mini.post;

import mini.renderEngine.RenderManager;
import mini.renderEngine.ViewPort;
import mini.renderEngine.queue.RenderQueue;
import mini.textures.FrameBuffer;

/**
 * Scene processors are used to compute/render things before and after the classic render of the
 * scene. They have to be added to a viewport and are rendered in the order they've been added.
 *
 * @author miniwolf
 */
public interface SceneProcessor {
    /**
     * Called in the render thread to initialize the scene processor.
     *
     * @param rm The render manager to which the SP was added to
     * @param vp The viewport to which the SP is assigned
     */
    void initialize(RenderManager rm, ViewPort vp);

    /**
     * @return True if initialize() has been called on this SceneProcessor,
     * false if otherwise.
     */
    boolean isInitialized();

    /**
     * Called when the resolution of the viewport has been changed.
     * @param vp
     */
    void reshape(ViewPort vp, int w, int h);

    /**
     * Called before a frame
     */
    void preFrame();

    /**
     * Called after the scene graph has been queued, but before it is flushed.
     *
     * @param rq The render queue
     */
    void postQueue(RenderQueue rq);

    /**
     * Called after a frame has been rendered and the queue flushed.
     *
     * @param out The FB to which the scene was rendered.
     */
    void postFrame(FrameBuffer out);

    /**
     * Called when the SP is removed from the RM.
     */
    void cleanup();
}
