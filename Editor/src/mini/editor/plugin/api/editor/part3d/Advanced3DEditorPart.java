package mini.editor.plugin.api.editor.part3d;

import mini.editor.annotation.FromAnyThread;
import mini.editor.part3d.editor.impl.AdvancedAbstractEditor3DPart;
import mini.editor.plugin.api.editor.Advanced3DFileEditor;
import mini.editor.util.ObjectsUtil;
import mini.scene.Node;

public abstract class Advanced3DEditorPart<T extends Advanced3DFileEditor>
        extends AdvancedAbstractEditor3DPart<T> {

    /**
     * The node on which the camera is looking.
     */
    private Node cameraNode;

    public Advanced3DEditorPart(T fileEditor) {
        super(fileEditor);
        getStateNode().attachChild(getCameraNode());
    }

    @Override
    @FromAnyThread
    protected Node getNodeForCamera() {
        if (cameraNode == null) {
            cameraNode = new Node("CameraNode");
        }
        return cameraNode;
    }

    public Node getCameraNode() {
        return ObjectsUtil.notNull(cameraNode);
    }
}
