package mini.editor.ui.component.editor;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;

import java.util.concurrent.Callable;

public class EditorDescription {
    private final Array<String> extensions;

    private String editorName;
    private Callable<FileEditor> constructor;
    private String editorId;

    public EditorDescription() {
        extensions = ArrayFactory.newArray(String.class);
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(final String editorName) {
        this.editorName = editorName;
    }

    public Callable<FileEditor> getConstructor() {
        return constructor;
    }

    public void setConstructor(final Callable<FileEditor> constructor) {
        this.constructor = constructor;
    }

    public String getEditorId() {
        return editorId;
    }

    public void setEditorId(final String editorId) {
        this.editorId = editorId;
    }

    public void addExtension(final String extension) {
        extensions.add(extension);
    }

    public Array<String> getExtensions() {
        return extensions;
    }
}
