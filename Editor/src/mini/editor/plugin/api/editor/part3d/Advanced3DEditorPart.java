package mini.editor.plugin.api.editor.part3d;

import mini.editor.annotation.FromAnyThread;
import mini.editor.part3d.editor.impl.AdvancedAbstractEditor3DPart;
import mini.editor.plugin.api.editor.Advanced3DFileEditor;
import mini.scene.Node;

public class Advanced3DEditorPart<T extends Advanced3DFileEditor>
        extends AdvancedAbstractEditor3DPart<T> {

    /**
     * The node on which the camera is looking.
     */
    private Node cameraNode;

    @Override
    @FromAnyThread
    protected Node getNodeForCamera() {
        if (cameraNode == null) {
            cameraNode = new Node("CameraNode");
        }
        return cameraNode;
    }
}
