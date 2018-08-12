package mini.editor.plugin.api.editor;

import javafx.event.Event;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.manager.WorkspaceManager;
import mini.editor.model.undo.EditorOperation;
import mini.editor.model.undo.EditorOperationControl;
import mini.editor.model.undo.UndoableEditor;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.model.workspace.Workspace;
import mini.editor.ui.component.editor.impl.AbstractFileEditor;
import mini.editor.ui.component.editor.state.EditorState;
import mini.editor.util.ObjectsUtil;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public abstract class BaseFileEditor<S extends EditorState>
        extends AbstractFileEditor<StackPane>
        implements ChangeConsumer, UndoableEditor {
    private final EditorOperationControl operationControl;
    private final AtomicInteger changeCounter;

    /**
     * The state of this editor
     */
    private S editorState;
    private boolean ignoreListeners;

    protected BaseFileEditor() {
        operationControl = new EditorOperationControl(this);
        changeCounter = new AtomicInteger();
    }

    @FromAnyThread
    protected void setIgnoreListeners(final boolean ignoreListeners) {
        this.ignoreListeners = ignoreListeners;
    }

    @FromAnyThread
    protected boolean isIgnoreListeners() {
        return ignoreListeners;
    }

    @Override
    @FxThread
    public void incrementChange() {
        final int result = changeCounter.incrementAndGet();
        setDirty(result != 0);
    }

    @Override
    @FxThread
    protected StackPane createRoot() {
        return new StackPane();
    }

    @Override
    protected void handleExternalChanges() {
    }

    @Override
    @FxThread
    public void openFile(Path file) {
        super.openFile(file);

        doOpenFile(file);

        EXECUTOR_MANAGER.addFXTask(this::loadState);
    }

    /**
     * Loading a state of this editor
     */
    @FxThread
    protected void loadState() {
        final Supplier<EditorState> stateFactory = getEditorStateFactory();
        if (stateFactory == null) {
            return;
        }

        final WorkspaceManager workspaceManager = WorkspaceManager.getInstance();
        final Workspace currentWorkSpace = ObjectsUtil
                .notNull(workspaceManager.getCurrentWorkspace());
        editorState = currentWorkSpace.getEditorState(getEditFile(), stateFactory);
    }

    @Override
    @FromAnyThread
    public void execute(final EditorOperation operation) {
        operationControl.execute(operation);
    }

    /**
     * @return the factory to make an editor state.
     */
    protected abstract Supplier<EditorState> getEditorStateFactory();

    /**
     * Do main activities to open the file.
     *
     * @param file the file to open.
     */
    protected abstract void doOpenFile(Path file);

    @Override
    @FxThread
    public BorderPane get3DArea() {
        return null;
    }

    /**
     * @return the editor state
     */
    @FromAnyThread
    protected S getEditorState() {
        return editorState;
    }

    @Override
    @FxThread
    public boolean isInside(final double sceneX, final double sceneY,
                            final Class<? extends Event> eventType) {
        return false;
    }
}
