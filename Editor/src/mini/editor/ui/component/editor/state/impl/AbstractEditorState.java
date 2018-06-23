package mini.editor.ui.component.editor.state.impl;

import mini.editor.annotation.FxThread;
import mini.editor.ui.component.editor.state.EditorState;

public class AbstractEditorState implements EditorState {
    protected transient volatile Runnable changeHandler;

    private EditorState[] additionalStates;

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
