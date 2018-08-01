package mini.editor.ui.component.editor.impl.scene;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import mini.editor.FileExtensions;
import mini.editor.Messages;
import mini.editor.model.undo.EditorOperation;
import mini.editor.part3d.editor.impl.AbstractEditor3DPart;
import mini.editor.ui.component.editor.EditorDescription;
import mini.editor.ui.component.editor.state.EditorState;
import mini.editor.ui.component.tab.ScrollableEditorToolComponent;

import java.nio.file.Path;
import java.util.function.Supplier;

public class SceneFileEditor extends AbstractSceneFileEditor {

    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setEditorName(Messages.SCENE_FILE_EDITOR_NAME);
        DESCRIPTION.setConstructor(SceneFileEditor::new);
        DESCRIPTION.setEditorId(SceneFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(FileExtensions.MINI_SCENE);
    }

    @Override
    protected AbstractEditor3DPart create3DEditorPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void createContent(Pane root) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doOpenFile(Path file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(EditorOperation operation) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Supplier<EditorState> getEditorStateFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void incrementChange() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void processChangeTool(Number oldValue, Number newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void createToolComponents(ScrollableEditorToolComponent container, StackPane root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EditorDescription getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyShowed() {
        throw new UnsupportedOperationException();
    }
}
