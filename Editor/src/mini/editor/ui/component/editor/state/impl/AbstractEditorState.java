package mini.editor.ui.component.editor.state.impl;

import mini.editor.annotation.FxThread;
import mini.editor.ui.component.editor.state.EditorState;

public class AbstractEditorState implements EditorState {
    protected static final EditorState[] EMPTY_STATES = new EditorState[0];

    protected transient volatile Runnable changeHandler;

    private EditorState[] additionalStates;

    public AbstractEditorState() {
        additionalStates = EMPTY_STATES;
    }

    @Override
    public void setChangeHandler(final Runnable changeHandler) {
        this.changeHandler = changeHandler;

        for (EditorState additionalState: additionalStates) {
            additionalState.setChangeHandler(changeHandler);
        }
    }

    @FxThread
    protected void notifyChanged() {
        if (changeHandler != null) {
            changeHandler.run();
        }
    }
}
