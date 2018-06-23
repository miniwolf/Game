package mini.editor.input;

import com.ss.rlib.common.util.linkedlist.LinkedList;
import com.ss.rlib.common.util.linkedlist.LinkedListFactory;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import mini.editor.injfx.MiniOffscreenSurfaceContext;
import mini.input.MouseInput;
import mini.input.RawInputListener;
import mini.input.events.MouseButtonEvent;
import mini.input.events.MouseMotionEvent;

import java.util.HashMap;
import java.util.Map;

public class JavaFXMouseInput extends JavaFXInput implements MouseInput {
    /**
     * The scale factor for scrolling.
     */
    private static final int WHEEL_SCALE = 10;
    private static final Map<MouseButton, Integer> MOUSE_BUTTON_TO_MINI = new HashMap<>();

    static {
        MOUSE_BUTTON_TO_MINI.put(MouseButton.PRIMARY, BUTTON_LEFT);
        MOUSE_BUTTON_TO_MINI.put(MouseButton.SECONDARY, BUTTON_MIDDLE);
        MOUSE_BUTTON_TO_MINI.put(MouseButton.MIDDLE, BUTTON_RIGHT);
    }

    private final LinkedList<MouseMotionEvent> mouseMotionEvents;
    private final LinkedList<MouseButtonEvent> mouseButtonEvents;
    private RawInputListener listener;
    private boolean useLocalCoords;
    private boolean inverseYCoord;
    private int mouseX;
    private int mouseY;
    private final EventHandler<MouseEvent> processPressed = this::processPressed;
    private final EventHandler<MouseEvent> processReleased = this::processReleased;
    private int mouseWheel;
    private final EventHandler<MouseEvent> processMotion = this::processMotion;
    private final EventHandler<ScrollEvent> processScroll = this::processScroll;

    public JavaFXMouseInput(MiniOffscreenSurfaceContext context) {
        super(context);
        this.mouseButtonEvents = LinkedListFactory.newLinkedList(MouseMotionEvent.class);
        this.mouseMotionEvents = LinkedListFactory.newLinkedList(MouseButtonEvent.class);
    }

    private void processScroll(ScrollEvent scrollEvent) {
        onWheelScroll(scrollEvent.getDeltaX() * WHEEL_SCALE, scrollEvent.getDeltaY() * WHEEL_SCALE);
    }

    private void onWheelScroll(double ignored, double yOffset) {
        mouseWheel += yOffset;

        var mouseMotionEvent = new MouseMotionEvent(mouseX,
                                                    mouseY,
                                                    0,
                                                    0,
                                                    mouseWheel,
                                                    (int) Math.round(yOffset));

        mouseMotionEvent.setTime(getInputTimeNanos());
        EXECUTOR.addToExecute(() -> mouseMotionEvents.add(mouseMotionEvent));
    }

    private void processReleased(MouseEvent event) {
        onMouseButton(event.getButton(), false);
    }

    private void processPressed(MouseEvent event) {
        onMouseButton(event.getButton(), true);
    }

    private void onMouseButton(MouseButton button, boolean pressed) {
        var mouseButtonEvent = new MouseButtonEvent(convertButton(button),
                                                    pressed,
                                                    mouseX,
                                                    mouseY);
        mouseButtonEvent.setTime(getInputTimeNanos());

        EXECUTOR.addToExecute(() -> mouseButtonEvents.add(mouseButtonEvent));
    }

    private int convertButton(MouseButton button) {
        var result = MOUSE_BUTTON_TO_MINI.get(button);
        return result == null ? 0 : result;
    }

    private void processMotion(MouseEvent event) {
        var sceneX = event.getSceneX();
        var sceneY = event.getSceneY();

        if (!useLocalCoords) {
            onCursorPos(sceneX, sceneY);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void onCursorPos(double xPos, double yPos) {

        int x = (int) Math.round(xPos);
        int y = 0;

        if (inverseYCoord) {
            throw new UnsupportedOperationException();
        } else {
            y = (int) Math.round(yPos);
        }

        if (mouseX == 0) {
            mouseX = x;
        }
        if (mouseY == 0) {
            mouseY = y;
        }

        int xDelta = x - mouseX;
        int yDelta = y - mouseY;

        mouseX = x;
        mouseY = y;

        var mouseMotionEvent = new MouseMotionEvent(x, y, xDelta, yDelta, mouseWheel, 0);
        mouseMotionEvent.setTime(getInputTimeNanos());

        EXECUTOR.addToExecute(() -> mouseMotionEvents.add(mouseMotionEvent));
    }

    @Override
    public void bind(Node node) {
        super.bind(node);

        node.addEventHandler(MouseEvent.MOUSE_MOVED, processMotion);
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, processMotion);
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, processPressed);
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, processReleased);
        node.addEventHandler(ScrollEvent.ANY, processScroll);

        var properties = node.getProperties();
        // TODO: implement useLocalCoords property and inverseYCoords
    }

    @Override
    protected void initializeImplementation() {
    }

    @Override
    protected void updateImpl() {
        var listener = getListener();
        while (!mouseMotionEvents.isEmpty()) {
            listener.onMouseMotionEvent(mouseMotionEvents.poll());
        }

        while (!mouseButtonEvents.isEmpty()) {
            listener.onMouseButtonEvent(mouseButtonEvents.poll());
        }
    }

    @Override
    public void setCursorVisible(boolean visible) {
    }

    @Override
    public int getButtonCount() {
        return 3;
    }
}
