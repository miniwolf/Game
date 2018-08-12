package mini.editor.model.undo.editor.model.undo.impl;

import com.ss.rlib.common.util.ClassUtils;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;
import mini.editor.model.undo.EditorOperation;
import mini.editor.model.undo.UndoableEditor;

public abstract class AbstractEditorOperation<E> implements EditorOperation {
    protected static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    @Override
    @FxThread
    public void redo(UndoableEditor editor) {
        redoImpl(ClassUtils.unsafeCast(editor));
    }

    protected abstract void redoImpl(E editor);
}
