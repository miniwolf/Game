package mini.editor.ui.component.editor.impl.model;

import mini.editor.FileExtensions;
import mini.editor.Messages;
import mini.editor.part3d.editor.impl.model.ModelEditor3DPart;
import mini.editor.ui.component.editor.EditorDescription;
import mini.editor.ui.component.editor.impl.scene.AbstractSceneFileEditor;
import mini.editor.ui.component.editor.state.impl.EditorModelEditorState;
import mini.scene.Spatial;

public class ModelFileEditor
        extends AbstractSceneFileEditor<Spatial, ModelEditor3DPart, EditorModelEditorState> {
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setEditorName(Messages.MODEL_FILE_EDITOR_NAME);
        DESCRIPTION.setConstructor(ModelFileEditor::new);
        DESCRIPTION.setEditorId(ModelFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(FileExtensions.MINI_OBJECT);
    }
}
