package mini.editor.plugin.api.editor;

import mini.editor.plugin.api.editor.part3d.Advanced3DEditorPart;
import mini.editor.ui.component.editor.FileEditorUtils;
import mini.editor.ui.component.editor.state.impl.Editor3DEditorState;

public abstract class Advanced3DFileEditor<T extends Advanced3DEditorPart, S extends Editor3DEditorState>
        extends Base3DFileEditor<T, S> {

    @Override
    protected void loadState() {
        super.loadState();

        final S editorState = getEditorState();
        if (editorState != null) {
            FileEditorUtils.loadCameraState(editorState, getEditor3DPart());
        }
    }
}
