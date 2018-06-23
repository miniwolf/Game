package mini.editor.ui.component.editor.impl.scene;

import mini.editor.FileExtensions;
import mini.editor.Messages;
import mini.editor.ui.component.editor.EditorDescription;

public class SceneFileEditor extends AbstractSceneFileEditor {

    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setEditorName(Messages.SCENE_FILE_EDITOR_NAME);
        DESCRIPTION.setConstructor(SceneFileEditor::new);
        DESCRIPTION.setEditorId(SceneFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(FileExtensions.MINI_SCENE);
    }
}
