package mini.editor.plugin.api.editor;

import javafx.event.Event;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import mini.editor.annotation.FxThread;
import mini.editor.plugin.api.editor.part3d.Advanced3DEditorPart;
import mini.editor.ui.component.editor.state.impl.Editor3DWithEditorToolEditorState;
import mini.editor.ui.component.splt.pane.EditorToolSplitPane;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.EditorUtil;

public abstract class Advanced3DFileEditorWithRightTool<T extends Advanced3DEditorPart,
        S extends Editor3DWithEditorToolEditorState>
        extends Advanced3DFileEditor<T, S> {
    /**
     * The pane of the editor area.
     */
    private StackPane editorAreaPane;
    /**
     * The pane of the 3D editor area.
     */
    private BorderPane editor3DArea;

    @Override
    protected void createContent(StackPane root) {
        createEditorAreaPane();

        mainSplitContainer = new EditorToolSplitPane(EditorUtil.getFXScene(), root);

        // TODO: missing UI for splitcontainer, editor tool components...
    }

    private void createEditorAreaPane() {
        editorAreaPane = new StackPane();
        editorAreaPane.setOnDragOver(this::handleDragOverEvent);
        editorAreaPane.setOnDragDropped(this::handleDragDroppedEvent);

        editor3DArea = new BorderPane();
        editor3DArea.setOnMousePressed(event -> editor3DArea.requestFocus());
        editor3DArea.setOnKeyReleased(Event::consume);
        editor3DArea.setOnKeyPressed(Event::consume);

        editorAreaPane.getChildren().add(editor3DArea);
        editorAreaPane.getStyleClass().add(CssClasses.FILE_EDITOR_EDITOR_AREA);
    }

    @Override
    @FxThread
    protected void loadState() {
        super.loadState();

        final S editorState = getEditorState();
        if (editorState == null) {
            return;
        }

        // TODO: missing UI for splitcontainer, editor tool components...
    }

    @Override
    @FxThread
    public BorderPane get3DArea() {
        return editor3DArea;
    }

    /**
     * @return the pane of the editor area.
     */
    @FxThread
    public StackPane getEditorAreaPane() {
        return editorAreaPane;
    }

    /**
     * Handle dragging over events.
     */
    @FxThread
    protected abstract void handleDragOverEvent(final DragEvent dragEvent);

    /**
     * Handle dropping events.
     */
    @FxThread
    protected abstract void handleDragDroppedEvent(DragEvent dragEvent);
}
