package mini.input;

import mini.input.controls.ActionListener;
import mini.input.controls.AnalogListener;
import mini.input.controls.InputListener;
import mini.input.controls.KeyTrigger;
import mini.input.controls.MouseAxisTrigger;
import mini.input.controls.MouseButtonTrigger;
import mini.input.controls.Trigger;
import mini.input.events.InputEvent;
import mini.input.events.KeyInputEvent;
import mini.input.events.MouseButtonEvent;
import mini.input.events.MouseMotionEvent;
import mini.math.FastMath;
import mini.math.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The <code>InputManager</code> is responsible for converting input events
 * received from the Key, Mouse and Joy Input implementations into an
 * abstract, input device independent representation that user code can use.
 * <p>
 * By default an <code>InputManager</code> is included with every Application instance for use
 * in user code to query input, unless the Application is created as headless
 * or with input explicitly disabled.
 * <p>
 * The input manager has two concepts, a {@link Trigger} and a mapping.
 * A trigger represents a specific input trigger, such as a key button,
 * or a mouse axis. A mapping represents a link onto one or several triggers,
 * when the appropriate trigger is activated (e.g. a key is pressed), the
 * mapping will be invoked. Any listeners registered to receive an event
 * from the mapping will have an event raised.
 * <p>
 * There are two types of events that {@link InputListener input listeners}
 * can receive, one is {@link ActionListener#onAction(java.lang.String, boolean, float) action}
 * events and another is {@link AnalogListener#onAnalog(java.lang.String, float, float) analog}
 * events.
 * <p>
 * <code>onAction</code> events are raised when the specific input
 * activates or deactivates. For a digital input such as key press, the <code>onAction()</code>
 * event will be raised with the <code>isPressed</code> argument equal to true,
 * when the key is released, <code>onAction</code> is called again but this time
 * with the <code>isPressed</code> argument set to false.
 * For analog inputs, the <code>onAction</code> method will be called any time
 * the input is non-zero, however an exception to this is for joystick axis inputs,
 * which are only called when the input is above the {@link InputManager#setAxisDeadZone(float) dead zone}.
 * <p>
 * <code>onAnalog</code> events are raised every frame while the input is activated.
 * For digital inputs, every frame that the input is active will cause the
 * <code>onAnalog</code> method to be called, the argument <code>value</code>
 * argument will equal to the frame's time per frame (TPF) value but only
 * for digital inputs. For analog inputs however, the <code>value</code> argument
 * will equal the actual analog value.
 */
public class InputManager implements RawInputListener {
    private final KeyInput keys;
    private final MouseInput mouse;
    private float frameTPF;
    private long lastLastUpdateTime = 0;
    private long lastUpdateTime = 0;
    private long frameDelta = 0;
    private long firstTime = 0;
    private boolean eventsPermitted = false;
    private boolean mouseVisible = true;
    private boolean safeMode = false;
    private float globalAxisDeadZone = 0.05f;
    private final Vector2f cursorPos = new Vector2f();
    private final Map<Integer, ArrayList<Mapping>> bindings = new HashMap<>();
    private final HashMap<String, Mapping> mappings = new HashMap<>();
    private final Map<Integer, Long> pressedButtons = new HashMap<>();
    private final Map<Integer, Float> axisValues = new HashMap<>();
    private final List<RawInputListener>
            rawListeners = new ArrayList<>();
    private final List<InputEvent> inputQueue = new ArrayList<>();

    private static class Mapping {
        private final String name;
        private final List<Integer> triggers = new ArrayList<>();
        private final List<InputListener> listeners = new ArrayList<>();

        public Mapping(String name) {
            this.name = name;
        }
    }

    /**
     * Initializes the InputManager.
     *
     * <p>This should only be called internally in {@link Application}.
     *
     * @param mouse
     * @param keys
     * @param joystick
     * @param touch
     * @throws IllegalArgumentException If either mouseInput or keyInput are null.
     */
    public InputManager(MouseInput mouse, KeyInput keys) {
        if (keys == null || mouse == null) {
            throw new IllegalArgumentException("Mouse or keyboard cannot be null");
        }

        this.keys = keys;
        this.mouse = mouse;

        keys.setInputListener(this);
        mouse.setInputListener(this);

        firstTime = keys.getInputTimeNanos();
    }

    private void invokeActions(int hash, boolean pressed) {
        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null) {
            return;
        }

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--) {
            Mapping mapping = maps.get(i);
            List<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; j >= 0; j--) {
                InputListener listener = listeners.get(j);
                if (listener instanceof ActionListener) {
                    ((ActionListener) listener).onAction(mapping.name, pressed, frameTPF);
                }
            }
        }
    }

    private float computeAnalogValue(long timeDelta) {
        if (safeMode || frameDelta == 0) {
            return 1f;
        } else {
            return FastMath.clamp((float) timeDelta / (float) frameDelta, 0, 1);
        }
    }

    private void invokeTimedActions(int hash, long time, boolean pressed) {
        if (!bindings.containsKey(hash)) {
            return;
        }

        if (pressed) {
            pressedButtons.put(hash, time);
        } else {
            Long pressTimeObj = pressedButtons.remove(hash);
            if (pressTimeObj == null) {
                return; // under certain circumstances it can be null, ignore
            }                        // the event then.

            long pressTime = pressTimeObj;
            long lastUpdate = lastLastUpdateTime;
            long releaseTime = time;
            long timeDelta = releaseTime - Math.max(pressTime, lastUpdate);

            if (timeDelta > 0) {
                invokeAnalogs(hash, computeAnalogValue(timeDelta), false);
            }
        }
    }

    private void invokeUpdateActions() {
        for (Map.Entry<Integer, Long> pressedButton : pressedButtons.entrySet()) {
            int hash = pressedButton.getKey();

            long pressTime = pressedButton.getValue();
            long timeDelta = lastUpdateTime - Math.max(lastLastUpdateTime, pressTime);

            if (timeDelta > 0) {
                invokeAnalogs(hash, computeAnalogValue(timeDelta), false);
            }
        }

        for (Map.Entry<Integer, Float> axisValue : axisValues.entrySet()) {
            int hash = axisValue.getKey();
            float value = axisValue.getValue();
            invokeAnalogs(hash, value * frameTPF, true);
        }
    }

    private void invokeAnalogs(int hash, float value, boolean isAxis) {
        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null) {
            return;
        }

        if (!isAxis) {
            value *= frameTPF;
        }

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--) {
            Mapping mapping = maps.get(i);
            List<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; j >= 0; j--) {
                InputListener listener = listeners.get(j);
                if (listener instanceof AnalogListener) {
                    // NOTE: multiply by TPF for any button bindings
                    ((AnalogListener) listener).onAnalog(mapping.name, value, frameTPF);
                }
            }
        }
    }

    private void invokeAnalogsAndActions(int hash, float value, float effectiveDeadZone, boolean applyTpf) {
        if (value < effectiveDeadZone) {
            invokeAnalogs(hash, value, !applyTpf);
            return;
        }

        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null) {
            return;
        }

        boolean valueChanged = !axisValues.containsKey(hash);
        if (applyTpf) {
            value *= frameTPF;
        }

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--) {
            Mapping mapping = maps.get(i);
            List<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; j >= 0; j--) {
                InputListener listener = listeners.get(j);

                if (listener instanceof ActionListener && valueChanged) {
                    ((ActionListener) listener).onAction(mapping.name, true, frameTPF);
                }

                if (listener instanceof AnalogListener) {
                    ((AnalogListener) listener).onAnalog(mapping.name, value, frameTPF);
                }

            }
        }
    }

    /**
     * Callback from RawInputListener. Do not use.
     */
    @Override
    public void beginInput() {
    }

    /**
     * Callback from RawInputListener. Do not use.
     */
    @Override
    public void endInput() {
    }

    private void onMouseMotionEventQueued(MouseMotionEvent evt) {
//        for (int i = 0; i < rawListeners.size(); i++){
//            rawListeners.get(i).onMouseMotionEvent(evt);
//        }

        if (evt.getDX() != 0) {
            float val = Math.abs(evt.getDX()) / 1024f;
            invokeAnalogsAndActions(
                    MouseAxisTrigger.mouseAxisHash(MouseInput.AXIS_X, evt.getDX() < 0), val, globalAxisDeadZone, false);
        }
        if (evt.getDY() != 0) {
            float val = Math.abs(evt.getDY()) / 1024f;
            invokeAnalogsAndActions(MouseAxisTrigger.mouseAxisHash(MouseInput.AXIS_Y, evt.getDY() < 0), val, globalAxisDeadZone, false);
        }
        if (evt.getDeltaWheel() != 0) {
            float val = Math.abs(evt.getDeltaWheel()) / 100f;
            invokeAnalogsAndActions(MouseAxisTrigger.mouseAxisHash(MouseInput.AXIS_WHEEL, evt.getDeltaWheel() < 0), val, globalAxisDeadZone, false);
        }
    }

    /**
     * Callback from RawInputListener. Do not use.
     */
    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {
        if (!eventsPermitted) {
            throw new UnsupportedOperationException("MouseInput has raised an event at an illegal time.");
        }

        cursorPos.set(evt.getX(), evt.getY());
        inputQueue.add(evt);
    }

    private void onMouseButtonEventQueued(MouseButtonEvent evt) {
        int hash = MouseButtonTrigger.mouseButtonHash(evt.getButtonIndex());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    /**
     * Callback from RawInputListener. Do not use.
     */
    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (!eventsPermitted) {
            throw new UnsupportedOperationException("MouseInput has raised an event at an illegal time.");
        }
        //updating cursor pos on click, so that non android touch events can properly update cursor position.
        cursorPos.set(evt.getX(), evt.getY());
        inputQueue.add(evt);
    }

    private void onKeyEventQueued(KeyInputEvent evt) {
        if (evt.isRepeating()) {
            return; // repeat events not used for bindings
        }

        int hash = KeyTrigger.keyHash(evt.getKeyCode());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    /**
     * Callback from RawInputListener. Do not use.
     */
    @Override
    public void onKeyEvent(KeyInputEvent evt) {
        if (!eventsPermitted) {
            throw new UnsupportedOperationException("KeyInput has raised an event at an illegal time.");
        }

        inputQueue.add(evt);
    }

    /**
     * Set the deadzone for joystick axes.
     *
     * <p>{@link ActionListener#onAction(java.lang.String, boolean, float) }
     * events will only be raised if the joystick axis value is greater than
     * the <code>deadZone</code>.
     *
     * @param deadZone the deadzone for joystick axes.
     */
    public void setAxisDeadZone(float deadZone) {
        this.globalAxisDeadZone = deadZone;
    }

    /**
     * Returns the deadzone for joystick axes.
     *
     * @return the deadzone for joystick axes.
     */
    public float getAxisDeadZone() {
        return globalAxisDeadZone;
    }

    /**
     * Adds a new listener to receive events on the given mappings.
     *
     * <p>The given InputListener will be registered to receive events
     * on the specified mapping names. When a mapping raises an event, the
     * listener will have its appropriate method invoked, either
     * {@link ActionListener#onAction(java.lang.String, boolean, float) }
     * or {@link AnalogListener#onAnalog(java.lang.String, float, float) }
     * depending on which interface the <code>listener</code> implements.
     * If the listener implements both interfaces, then it will receive the
     * appropriate event for each method.
     *
     * @param listener The listener to register to receive input events.
     * @param mappingNames The mapping names which the listener will receive
     * events from.
     *
     * @see InputManager#removeListener(mini.input.controls.InputListener)
     */
    public void addListener(InputListener listener, String... mappingNames) {
        for (String mappingName : mappingNames) {
            Mapping mapping = mappings.get(mappingName);
            if (mapping == null) {
                mapping = new Mapping(mappingName);
                mappings.put(mappingName, mapping);
            }
            if (!mapping.listeners.contains(listener)) {
                mapping.listeners.add(listener);
            }
        }
    }

    /**
     * Removes a listener from receiving events.
     *
     * <p>This will unregister the listener from any mappings that it
     * was previously registered with via
     * {@link InputManager#addListener(mini.input.controls.InputListener, java.lang.String[]) }.
     *
     * @param listener The listener to unregister.
     *
     * @see InputManager#addListener(mini.input.controls.InputListener, java.lang.String[])
     */
    public void removeListener(InputListener listener) {
        for (Mapping mapping : mappings.values()) {
            mapping.listeners.remove(listener);
        }
    }

    /**
     * Create a new mapping to the given triggers.
     *
     * <p>
     * The given mapping will be assigned to the given triggers, when
     * any of the triggers given raise an event, the listeners
     * registered to the mappings will receive appropriate events.
     *
     * @param mappingName The mapping name to assign.
     * @param triggers The triggers to which the mapping is to be registered.
     *
     * @see InputManager#deleteMapping(java.lang.String)
     */
    public void addMapping(String mappingName, Trigger... triggers) {
        Mapping mapping = mappings.get(mappingName);
        if (mapping == null) {
            mapping = new Mapping(mappingName);
            mappings.put(mappingName, mapping);
        }

        for (Trigger trigger : triggers) {
            int hash = trigger.triggerHashCode();
            ArrayList<Mapping> names = bindings.get(hash);
            if (names == null) {
                names = new ArrayList<Mapping>();
                bindings.put(hash, names);
            }
            if (!names.contains(mapping)) {
                names.add(mapping);
                mapping.triggers.add(hash);
            } else {
                System.err.println("Attempted to add mapping \"" + mappingName + "\" twice to trigger.");
            }
        }
    }

    /**
     * Returns true if this InputManager has a mapping registered
     * for the given mappingName.
     *
     * @param mappingName The mapping name to check.
     *
     * @see InputManager#addMapping(java.lang.String, mini.input.controls.Trigger[])
     * @see InputManager#deleteMapping(java.lang.String)
     */
    public boolean hasMapping(String mappingName) {
        return mappings.containsKey(mappingName);
    }

    /**
     * Deletes a mapping from receiving trigger events.
     *
     * <p>
     * The given mapping will no longer be assigned to receive trigger
     * events.
     *
     * @param mappingName The mapping name to unregister.
     *
     * @see InputManager#addMapping(java.lang.String, mini.input.controls.Trigger[])
     */
    public void deleteMapping(String mappingName) {
        Mapping mapping = mappings.remove(mappingName);
        if (mapping == null) {
            //throw new IllegalArgumentException("Cannot find mapping: " + mappingName);
            System.err.println("Cannot find mapping to be removed, skipping: " + mappingName);
            return;
        }

        List<Integer> triggers = mapping.triggers;
        for (int i = triggers.size() - 1; i >= 0; i--) {
            int hash = triggers.get(i);
            ArrayList<Mapping> maps = bindings.get(hash);
            maps.remove(mapping);
        }
    }

    /**
     * Deletes a specific trigger registered to a mapping.
     *
     * <p>
     * The given mapping will no longer receive events raised by the
     * trigger.
     *
     * @param mappingName The mapping name to cease receiving events from the
     * trigger.
     * @param trigger The trigger to no longer invoke events on the mapping.
     */
    public void deleteTrigger(String mappingName, Trigger trigger) {
        Mapping mapping = mappings.get(mappingName);
        if (mapping == null) {
            throw new IllegalArgumentException("Cannot find mapping: " + mappingName);
        }

        ArrayList<Mapping> maps = bindings.get(trigger.triggerHashCode());
        maps.remove(mapping);

    }

    /**
     * Clears all the input mappings from this InputManager.
     * Consequently, also clears all of the
     * InputListeners as well.
     */
    public void clearMappings() {
        mappings.clear();
        bindings.clear();
        reset();
    }

    /**
     * Do not use.
     * Called to reset pressed keys or buttons when focus is restored.
     */
    public void reset() {
        pressedButtons.clear();
        axisValues.clear();
    }

    /**
     * Returns whether the mouse cursor is visible or not.
     *
     * <p>By default the cursor is visible.
     *
     * @return whether the mouse cursor is visible or not.
     *
     * @see InputManager#setCursorVisible(boolean)
     */
    public boolean isCursorVisible() {
        return mouseVisible;
    }

    /**
     * Set whether the mouse cursor should be visible or not.
     *
     * @param visible whether the mouse cursor should be visible or not.
     */
    public void setCursorVisible(boolean visible) {
        if (mouseVisible != visible) {
            mouseVisible = visible;
            mouse.setCursorVisible(mouseVisible);
        }
    }

    /**
     * Returns the current cursor position. The position is relative to the
     * bottom-left of the screen and is in pixels.
     *
     * @return the current cursor position
     */
    public Vector2f getCursorPosition() {
        return cursorPos;
    }

    /**
     * Adds a {@link RawInputListener} to receive raw input events.
     *
     * <p>
     * Any raw input listeners registered to this <code>InputManager</code>
     * will receive raw input events first, before they get handled
     * by the <code>InputManager</code> itself. The listeners are
     * each processed in the order they were added, e.g. FIFO.
     * <p>
     * If a raw input listener has handled the event and does not wish
     * other listeners down the list to process the event, it may set the
     * {@link InputEvent#setConsumed() consumed flag} to indicate the
     * event was consumed and shouldn't be processed any further.
     * The listener may do this either at each of the event callbacks
     * or at the {@link RawInputListener#endInput() } method.
     *
     * @param listener A listener to receive raw input events.
     *
     * @see RawInputListener
     */
    public void addRawInputListener(RawInputListener listener) {
        rawListeners.add(listener);
    }

    /**
     * Removes a {@link RawInputListener} so that it no longer
     * receives raw input events.
     *
     * @param listener The listener to cease receiving raw input events.
     *
     * @see InputManager#addRawInputListener(mini.input.RawInputListener)
     */
    public void removeRawInputListener(RawInputListener listener) {
        rawListeners.remove(listener);
    }

    /**
     * Clears all {@link RawInputListener}s.
     *
     * @see InputManager#addRawInputListener(mini.input.RawInputListener)
     */
    public void clearRawInputListeners() {
        rawListeners.clear();
    }

    /**
     * @deprecated Use isSimulateMouse
     * Returns state of simulation of mouse events. Used for touchscreen input only.
     *
     */
    public boolean getSimulateMouse() {
        return false;
    }

    /**
     * Returns state of simulation of mouse events. Used for touchscreen input only.
     *
     */
    public boolean isSimulateMouse() {
        return false;
    }

    /**
     * Returns state of simulation of key events. Used for touchscreen input only.
     *
     */
    public boolean isSimulateKeyboard() {
        return false;
    }

    private void processQueue() {
        int queueSize = inputQueue.size();
        RawInputListener[] array = (RawInputListener[]) rawListeners.toArray();

        for (RawInputListener listener : array) {
            listener.beginInput();

            for (InputEvent event : inputQueue) {
                if (event.isConsumed()) {
                    continue;
                }

                if (event instanceof MouseMotionEvent) {
                    listener.onMouseMotionEvent((MouseMotionEvent) event);
                } else if (event instanceof KeyInputEvent) {
                    listener.onKeyEvent((KeyInputEvent) event);
                } else if (event instanceof MouseButtonEvent) {
                    listener.onMouseButtonEvent((MouseButtonEvent) event);
                } else {
                    assert false;
                }
            }

            listener.endInput();
        }

        for (InputEvent event : inputQueue) {
            if (event.isConsumed()) {
                continue;
            }

            if (event instanceof MouseMotionEvent) {
                onMouseMotionEventQueued((MouseMotionEvent) event);
            } else if (event instanceof KeyInputEvent) {
                onKeyEventQueued((KeyInputEvent) event);
            } else if (event instanceof MouseButtonEvent) {
                onMouseButtonEventQueued((MouseButtonEvent) event);
            } else {
                assert false;
            }
            // larynx, 2011.06.10 - flag event as reusable because
            // the android input uses a non-allocating ringbuffer which
            // needs to know when the event is not anymore in inputQueue
            // and therefor can be reused.
            event.setConsumed();
        }

        inputQueue.clear();
    }

    /**
     * Updates the <code>InputManager</code>.
     * This will query current input devices and send
     * appropriate events to registered listeners.
     *
     * @param tpf Time per frame value.
     */
    public void update(float tpf) {
        frameTPF = tpf;

        // Activate safemode if the TPF value is so small
        // that rounding errors are inevitable
        safeMode = tpf < 0.015f;

        long currentTime = keys.getInputTimeNanos();
        frameDelta = currentTime - lastUpdateTime;

        eventsPermitted = true;

        keys.update();
        mouse.update();

        eventsPermitted = false;

        processQueue();
        invokeUpdateActions();

        lastLastUpdateTime = lastUpdateTime;
        lastUpdateTime = currentTime;
    }
}
