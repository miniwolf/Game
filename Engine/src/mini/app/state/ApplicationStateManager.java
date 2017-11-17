package mini.app.state;

import mini.app.Application;
import mini.renderer.RenderManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationStateManager {
    /**
     * Holds the active states once they are initialized
     */
    private List<ApplicationState> states = new ArrayList<>();

    /**
     * Holds the attached application states that are pending initialization. Once initialized they
     * will be added to the running application states.
     */
    private List<ApplicationState> initializing = new ArrayList<>();

    /**
     * Holds the detached application states that are pending cleanup.
     */
    private List<ApplicationState> terminating = new ArrayList<>();

    private Application application;
    private Object termininating;

    public ApplicationStateManager(Application application) {
        this.application = application;
    }

    /**
     * Attach a state to the ApplicationStateManager, the same state cannot be attached twice.
     *
     * @param state The state to attach
     * @return True if the state was sucessfully attached, false if the state was already attached.
     */
    public boolean attach(ApplicationState state) {
        if (states.contains(state) || initializing.contains(state)) {
            return false;
        }

        state.stateAttached(this);
        initializing.add(state);
        return true;
    }

    /**
     * Detaches the state from the ApplicationStateManager.
     *
     * @param state The state to detach
     * @return True if the state was detached successfully, false if the state was not attached in
     * the first place.
     */
    public boolean detach(ApplicationState state) {
        if (states.contains(state)) {
            state.stateDetached(this);
            states.remove(state);
            terminating.add(state);
            return true;
        } else if (initializing.contains(state)) {
            state.stateDetached(this);
            initializing.remove(state);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calls update for attached states, do not call directly.
     */
    public void update() {
        // Cleanup any states pending
        terminatePending();

        // Initialize any states pending
        initializePending();

        // Update enabled states
        for (ApplicationState state : getStates()) {
            if (state.isEnabled()) {
                state.update();
            }
        }
    }

    /**
     * Calls render for attached and initialized states, do not call directly.
     *
     * @param renderManager The RenderManager
     */
    public void render(RenderManager renderManager) {
        for (ApplicationState state : getStates()) {
            if (state.isEnabled()) {
                state.render(renderManager);
            }
        }
    }

    /**
     * Calls postRender for attached and initialized states, do not call directly.
     *
     * @param renderManager The RenderManager
     */
    public void postRender() {
        for (ApplicationState state : getStates()) {
            if (state.isEnabled()) {
                state.postRender();
            }
        }
    }

    private void initializePending() {
        ApplicationState[] array = getInitializing();
        if (array.length == 0) {
            return;
        }

        // Move the states that will be initialized into the active array. In all but one case the
        // order doesn't matter but if we do this here then a state can detach itself in
        // initialize(). If we did it after then it couldn't
        List<ApplicationState> transfer = Arrays.asList(array);
        states.addAll(transfer);
        initializing.removeAll(transfer);

        for (ApplicationState state : array) {
            state.initialize(this, application);
        }
    }

    private void terminatePending() {
        ApplicationState[] array = getTermininating();
        if (array.length == 0) {
            return;
        }

        for (ApplicationState state : array) {
            state.cleanup();
        }
        // Remove just the states that were terminated...
        // which might now be a subset of the total terminating list.
        terminating.removeAll(Arrays.asList(array));
    }

    public <T extends ApplicationState> T getState(Class<T> stateClass) {
        ApplicationState[] array = getStates();
        for (ApplicationState state : array) {
            if (stateClass.isAssignableFrom(state.getClass())) {
                return (T) state;
            }
        }

        // This may be more trouble than it is worth, but I think it's necessary for proper
        // decoupling of states and provides similar behaviour to before where a state could be
        // looked up even if it wasn't initialized.
        array = getInitializing();
        for (ApplicationState state : array) {
            if (stateClass.isAssignableFrom(state.getClass())) {
                return (T) state;
            }
        }
        return null;
    }

    protected ApplicationState[] getStates() {
        ApplicationState[] appStates = new ApplicationState[states.size()];
        return states.toArray(appStates);
    }

    public ApplicationState[] getInitializing() {
        ApplicationState[] appStates = new ApplicationState[initializing.size()];
        return initializing.toArray(appStates);
    }

    public ApplicationState[] getTermininating() {
        ApplicationState[] appStates = new ApplicationState[terminating.size()];
        return terminating.toArray(appStates);
    }
}
