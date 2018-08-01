package mini.editor.ui.component.editor.state.impl;

import mini.editor.ui.component.editor.state.EditorToolConfig;

public class Editor3DWithEditorToolEditorState
        extends Editor3DEditorState
        implements EditorToolConfig {

    /**
     * Opened editor tool
     */
    private volatile int openedTool;
    private transient volatile Runnable changeHandler;

    public void setOpenedTool(final int openedTool) {
        final boolean changed = getOpenedTool() != openedTool;
        this.openedTool = openedTool;
        final Runnable changeHandler = getChangeHandler();
        if (changed && changeHandler != null) {
            changeHandler.run();
        }
    }

    public int getOpenedTool() {
        return openedTool;
    }

    public Runnable getChangeHandler() {
        return changeHandler;
    }
}
