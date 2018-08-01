package mini.editor.part3d.editor.impl;

import mini.app.state.AbstractApplicationState;
import mini.editor.manager.ExecutorManager;
import mini.editor.part3d.editor.Editor3DPart;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.ui.component.editor.impl.model.ModelFileEditor;
import mini.scene.Node;

public abstract class AbstractEditor3DPart<T extends FileEditor>
        extends AbstractApplicationState
        implements Editor3DPart {

    protected static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    /**
     * The root node
     */
    private final Node stateNode;
    /**
     * The owner editor
     */
    private final T fileEditor;

    public AbstractEditor3DPart(final T fileEditor) {
        this.fileEditor = fileEditor;
        this.stateNode = new Node(getClass().getSimpleName());
    }

    protected Node getStateNode() {
        return stateNode;
    }

    /**
     * @return the owner editor
     */
    protected T getFileEditor() {
        return fileEditor;
    }
}
