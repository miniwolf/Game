package mini.editor.part3d.editor.impl;

import mini.app.state.ApplicationStateManager;
import mini.editor.annotation.MiniThread;
import mini.editor.model.EditorCamera;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.ui.component.editor.impl.model.ModelFileEditor;
import mini.editor.util.EditorUtil;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.scene.Node;

public abstract class AdvancedAbstractEditor3DPart<T extends FileEditor> extends AbstractEditor3DPart<T> {

    private final EditorCamera editorCamera;

    public AdvancedAbstractEditor3DPart(T fileEditor) {
        super(fileEditor);
        editorCamera = needEditorCamera() ? createEditorCamera() : null;
    }

    /**
     * @return the created editor camera
     */
    @MiniThread
    protected EditorCamera createEditorCamera() {
        final Camera camera = EditorUtil.getGlobalCamera();

        final EditorCamera editorCamera = new EditorCamera(camera, getNodeForCamera());
        // TODO: Setup parameters, near and far
        return editorCamera;
    }

    /**
     * @return the node for the camera
     */
    @MiniThread
    protected Node getNodeForCamera() {
        return getStateNode();
    }

    /**
     * @return Whether an editor camera is needed.
     */
    protected boolean needEditorCamera() {
        return false;
    }

    /**
     * Update the editor camera settings
     */
    public void updateCameraSettings(final Vector3f cameraLocation,
                                     final float hRotation,
                                     final float vRotation,
                                     final float targetDistance,
                                     final float cameraSpeed) {
        EditorCamera editorCamera = getEditorCamera();

        if (editorCamera == null) {
            return;
        }

        editorCamera.setTargetRotation(hRotation);
        editorCamera.setTargetVRotation(vRotation);
        editorCamera.setTargetDistance(targetDistance);

        getNodeForCamera().setLocalTranslation(cameraLocation);

        editorCamera.update(1f);
    }

    /**
     * @return the editor camera
     */
    @MiniThread
    protected EditorCamera getEditorCamera() {
        return editorCamera;
    }

    @Override
    @MiniThread
    public void update(float tpf) {
        preCameraUpdate();
        cameraUpdate(tpf);
        postCameraUpdate();
    }

    @MiniThread
    protected void postCameraUpdate() {
        // TODO: Lighting for camera
    }

    @MiniThread
    private void cameraUpdate(float tpf) {
        final EditorCamera editorCamera = getEditorCamera();
        if (editorCamera == null) {
            return;
        }

        editorCamera.update(tpf);
        // TODO: Update camera controls
    }

    @MiniThread
    protected abstract void preCameraUpdate();
}
