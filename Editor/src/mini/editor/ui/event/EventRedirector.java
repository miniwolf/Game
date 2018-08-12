package mini.editor.ui.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.stage.Stage;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.ui.component.editor.area.EditorAreaComponent;
import mini.editor.util.UIUtils;

import static com.ss.rlib.common.util.ObjectUtils.notNull;

public class EventRedirector {
    private final EditorAreaComponent editorAreaComponent;
    private final Node destination;
    private final Stage stage;
    private final boolean[] mousePressed;
    private double sceneX;
    private double sceneY;

    public EventRedirector(
            final EditorAreaComponent editorAreaComponent,
            final Node destination,
            final Stage stage) {
        this.editorAreaComponent = editorAreaComponent;
        this.destination = destination;
        this.stage = stage;
        this.mousePressed = new boolean[MouseButton.values().length];
        init();
    }

    private void init() {
        stage.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            final EventTarget target = event.getTarget();
            if (target == destination) {
                return;
            }

            final FileEditor currentEditor = editorAreaComponent.getCurrentEditor();
            if (currentEditor == null) {
                return;
            }

            if (!isMousePressed(event.getButton())) {
                return;
            }
            setMousePressed(event.getButton(), false);

            Event.fireEvent(destination, event.copyFor(event.getSource(), destination));
        });

        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            final EventTarget target = event.getTarget();
            if (target == destination) {
                return;
            }

            final FileEditor currentEditor = editorAreaComponent.getCurrentEditor();
            if (currentEditor == null
                    || !currentEditor.isInside(event.getSceneX(), event.getSceneY(), event.getClass())) {
                return;
            }
            setMousePressed(event.getButton(), true);

            Event.fireEvent(destination, event.copyFor(event.getSource(), destination));
        });

        stage.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            final EventTarget target = event.getTarget();
            if (target == destination) {
                return;
            }

            final FileEditor currentEditor = editorAreaComponent.getCurrentEditor();
            if (currentEditor == null
                    || !currentEditor.isInside(event.getSceneX(), event.getSceneY(), event.getClass())) {
                return;
            }

            Event.fireEvent(destination, event.copyFor(event.getSource(), destination));
        });

        stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            final EventTarget target = event.getTarget();
            if (target == destination) {
                return;
            }

            final FileEditor currentEditor = editorAreaComponent.getCurrentEditor();
            if (currentEditor == null) {
                return;
            }
            if (!isMousePressed(event.getButton() )
                    && !currentEditor.isInside(event.getSceneX(), event.getSceneY(), event.getClass())) {
                return;
            }

            Event.fireEvent(destination, event.copyFor(event.getSource(), destination));
        });

        stage.addEventHandler(ScrollEvent.ANY, this::redirect);
        stage.addEventFilter(KeyEvent.KEY_PRESSED, this::redirect);
        stage.addEventFilter(KeyEvent.KEY_RELEASED, this::redirect);
    }

    private void redirect(InputEvent event) {
        final EventTarget target = event.getTarget();
        if (target == destination) {
            return;
        } else if (target instanceof TextInputControl) {
            if (event instanceof KeyEvent && UIUtils.isNotHotKey((KeyEvent) event)) {
                return;
            }
        }

        final EventType<? extends InputEvent> eventType = event.getEventType();
        final FileEditor currentEditor = editorAreaComponent.getCurrentEditor();

        if (currentEditor == null
                || eventType != KeyEvent.KEY_RELEASED
                && !currentEditor.isInside(getSceneX(), getSceneY(), event.getClass())) {
            return;
        }

        Event.fireEvent(destination, event.copyFor(event.getSource(), destination));
    }

    private void setMousePressed(
            final MouseButton button,
            final boolean mousePressed) {
        this.mousePressed[button.ordinal()] = mousePressed;
    }

    private boolean isMousePressed(final MouseButton button) {
        return mousePressed[button.ordinal()];
    }

    public double getSceneX() {
        return sceneX;
    }

    public double getSceneY() {
        return sceneY;
    }
}
