package mini.app.state;

import mini.app.Application;
import mini.renderer.RenderManager;

/**
 * ApplicationState represents continously executing code inside the main loop.
 * <p>
 * An <code>ApplicationState</code> can track when it is attached to the
 * {@link ApplicationStateManager} or when it is detached.
 * <p>
 * <br/><code>ApplicationState</code>s are initialised in the render thread, upon a call to
 * {@link ApplicationState#initialize(ApplicationStateManager, Application)} and are de-initialised
 * upon a call to {@link ApplicationState#cleanup()}.
 * Implementations should return the correct value with a call to
 * {@link ApplicationState#isInitialised()} as specified above.<br/>
 */
public interface ApplicationState {
    /**
     * Called by {@link ApplicationStateManager} when transitioning this {@code ApplicationState}
     * from <i>initializing</i> to <i>running</i>.<br/>
     * This will happen on the next iteration through the update loop after
     * {@link ApplicationStateManager#attach(ApplicationState)} was called.
     * <p>
     * <code>ApplicationStateManager</code> will call this only from the update loop inside the
     * rendering thread. This means is it safe to modify the scene graph from this method.
     *
     * @param manager The state manager
     * @param app     The application
     */
    void initialize(ApplicationStateManager manager, Application app);

    /**
     * @return Whether {@link #initialize(ApplicationStateManager, Application)} was called on the
     * state.
     */
    boolean isInitialized();

    /**
     * Enable or disable the functionality of the {@link ApplicationState}. The effect of this call
     * depends on the implementation. An {@link ApplicationState} starts as being enabled by
     * default. A disabled {@link ApplicationState}'s does not get calls to {@link #update(float)},
     * {@link #render(RenderManager)}, or {@link #postRender()} from its
     * {@link ApplicationStateManager}.
     *
     * @param active activate the {@link ApplicationState} or not
     */
    void setEnabled(boolean active);

    /**
     * @return Whether the <code>ApplicationState</code> is enabled.
     */
    boolean isEnabled();

    /**
     * Called by {@link ApplicationStateManager#attach(ApplicationState)} when transitioning this
     * <code>ApplicationState</code> from <i>detached</i> to <i>initializing</i>.
     * <p>
     * There is no assumption about the thread from which this function is called, therefore it is
     * <b>unsafe</b> to modify the scene graph from this method. Please use
     * {@link #initialize(ApplicationStateManager, Application)} instead.
     *
     * @param stateManager State manager to which the state was attached to.
     */
    void stateAttached(ApplicationStateManager stateManager);

    /**
     * Called by {@link ApplicationStateManager#detach(ApplicationState)} when transitioning this
     * <code>ApplicationState</code> from <i>running</i> to <i>terminating</i>.
     * <p>
     * There is no assumption about the thread from which this function is called, therefore it is
     * <b>unsafe</b> to modify the scene graph from this method. Please use {@link #cleanup()}
     * instead.
     *
     * @param stateManager state manager from which the state was detached from.
     */
    void stateDetached(ApplicationStateManager stateManager);

    /**
     * Called to update the <code>ApplicationState</code>. This method will be called every render
     * pass if the <code>ApplicationState</code> is both attached and enabled.
     */
    void update(float tpf);

    /**
     * Render the state. This method will be called every render pass if the
     * <code>ApplicationState</code> is both attached and enabled.
     *
     * @param renderManager RenderManager
     */
    void render(RenderManager renderManager);

    /**
     * Called after all rendering commands are flushed. This method will be called every render pass
     * if the <code>ApplicationState</code> is both attached and enabled.
     */
    void postRender();

    /**
     * Called by {@link ApplicationStateManager} when transitioning this
     * <code>ApplicationState</code> from <i>terminating</i> to <i>detached</i>. This method is
     * called the following render pass after the <code>ApplicationState</code> has been detached
     * and is always called once more and only once for each time <code>initialize()</code> is
     * called. Either when the <code>ApplicationState</code> is detached or when the application
     * terminates (if it terminates normally).
     */
    void cleanup();
}
