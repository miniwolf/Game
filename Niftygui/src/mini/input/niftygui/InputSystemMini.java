package mini.input.niftygui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyInputConsumer;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.spi.input.InputSystem;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;
import mini.input.InputManager;
import mini.input.KeyboardKey;
import mini.input.RawInputListener;
import mini.input.SoftTextDialogInput;
import mini.input.events.InputEvent;
import mini.input.events.KeyInputEvent;
import mini.input.events.MouseButtonEvent;
import mini.input.events.MouseMotionEvent;
import mini.system.ApplicationSystem;

import java.util.ArrayList;
import java.util.List;

public class InputSystemMini implements InputSystem, RawInputListener {
    private final List<InputEvent> inputList = new ArrayList<>();
    private boolean[] niftyOwnsDragging = new boolean[3];
    private int height;
    private Nifty nifty;
    private boolean shiftDown;
    private boolean ctrlDown;
    private InputManager inputManager;

    public InputSystemMini(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    /**
     * Used to convert bottom-left origin to upper-left origin.
     *
     * @param height Height of the viewport.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Needed to forward nifty events correctly.
     *
     * @param nifty
     */
    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
    }

    @Override
    public void setResourceLoader(NiftyResourceLoader niftyResourceLoader) {
    }

    @Override
    public void forwardEvents(NiftyInputConsumer niftyInputConsumer) {
        for (InputEvent event : inputList) {
            if (event instanceof MouseMotionEvent) {
                onMouseMotionEvent((MouseMotionEvent) event, niftyInputConsumer);
            } else if (event instanceof MouseButtonEvent) {
                onMouseButtonEvent((MouseButtonEvent) event, niftyInputConsumer);
            } else if (event instanceof KeyInputEvent) {
                onKeyEvent((KeyInputEvent) event, niftyInputConsumer);
            }
        }
    }

    @Override
    public void setMousePosition(int i, int i1) {
        // TODO: Is this used?
    }

    @Override
    public void beginInput() {
    }

    @Override
    public void endInput() {
        nifty.update();
    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {
        if (inputManager.isCursorVisible() && (evt.getDX() != 0 || evt.getDY() != 0
                                               || evt.getDeltaWheel() != 0)) {
            inputList.add(evt);
        }
    }

    private void onMouseMotionEvent(MouseMotionEvent event, NiftyInputConsumer niftyInputConsumer) {
        int wheel = event.getDeltaWheel() / 120;
        int x = event.getX();
        int y = height - event.getY();

        niftyInputConsumer.processMouseEvent(x, y, wheel, -1, false);
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (evt.getButtonIndex() >= 0 && evt.getButtonIndex() <= 2
            && (evt.isReleased() || inputManager.isCursorVisible())) {
            inputList.add(evt);
        }
    }

    private void onMouseButtonEvent(MouseButtonEvent event, NiftyInputConsumer niftyInputConsumer) {
        int x = event.getX();
        int y = height - event.getY();
        int button = event.getButtonIndex();
        boolean isPressed = event.isPressed();

        boolean consumed = niftyInputConsumer.processMouseEvent(x, y, 0, button, isPressed);
        if (isPressed) {
            consumed |= nifty.getCurrentScreen().isMouseOverElement();
            niftyOwnsDragging[button] = consumed;
            if (consumed) {
                event.setConsumed();
            }
        } else {
            if (niftyOwnsDragging[button] && consumed) {
                event.setConsumed();
                processSoftKeyboard();
            }

            niftyOwnsDragging[button] = false;
        }
    }

    private void processSoftKeyboard() {
        SoftTextDialogInput softTextDialogInput = ApplicationSystem.getSoftTextDialogInput();
        if (softTextDialogInput == null) {
            return;
        }

        // Android stuff from OGLESContext
    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {
        inputList.add(evt);
    }

    private void onKeyEvent(KeyInputEvent event, NiftyInputConsumer niftyInputConsumer) {
        KeyboardKey code = event.getKey();
        if (code == KeyboardKey.KEY_LEFT_SHIFT || code == KeyboardKey.KEY_RIGHT_SHIFT) {
            shiftDown = event.isPressed();
        } else if (code == KeyboardKey.KEY_LEFT_CONTROL || code == KeyboardKey.KEY_RIGHT_CONTROL) {
            ctrlDown = event.isPressed();
        }

        KeyboardInputEvent keyEvent = new KeyboardInputEvent(code.getValue(), event.getKeyChar(),
                                                             event.isPressed(), shiftDown,
                                                             ctrlDown);

        if (niftyInputConsumer.processKeyboardEvent(keyEvent)) {
            event.setConsumed();
        }
    }

    public void reset() {
        for (int i = 0; i < niftyOwnsDragging.length; i++) {
            niftyOwnsDragging[i] = false;
        }
        shiftDown = false;
        ctrlDown = false;
    }
}
