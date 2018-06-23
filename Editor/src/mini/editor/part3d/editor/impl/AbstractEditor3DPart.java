package mini.editor.part3d.editor.impl;

import mini.app.state.AbstractApplicationState;
import mini.editor.manager.ExecutorManager;
import mini.editor.part3d.editor.Editor3DPart;
import mini.editor.ui.component.editor.FileEditor;
import mini.scene.Node;

public class AbstractEditor3DPart<T extends FileEditor>
        extends AbstractApplicationState
        implements Editor3DPart {

    protected static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    /**
     * The root node
     */
    private final Node stateNode;

    public AbstractEditor3DPart() {
        this.stateNode = new Node(getClass().getSimpleName());
    }

    protected Node getStateNode() {
        return stateNode;
    }
}
