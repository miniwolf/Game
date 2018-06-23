package mini.editor.plugin.api.editor;

import mini.editor.plugin.api.editor.part3d.Advanced3DEditorPart;
import mini.editor.ui.component.editor.state.impl.Editor3DWithEditorToolEditorState;

public abstract class Advanced3DFileEditorWithSplitRightTool<T extends Advanced3DEditorPart,
        S extends Editor3DWithEditorToolEditorState>
        extends Advanced3DFileEditorWithRightTool<T, S> {
}
