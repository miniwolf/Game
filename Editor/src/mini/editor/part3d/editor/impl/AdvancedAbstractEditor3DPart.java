package mini.editor.part3d.editor.impl;

import mini.app.Application;
import mini.app.state.ApplicationStateManager;
import mini.editor.annotation.EditorThread;
import mini.editor.model.EditorCamera;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.util.EditorUtil;
import mini.input.InputManager;
import mini.input.controls.ActionListener;
import mini.input.controls.InputListener;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.scene.Node;

public abstract class AdvancedAbstractEditor3DPart<T extends FileEditor> extends AbstractEditor3DPart<T> {
    private static final String MOUSE_RIGHT_CLICK = "MiniEditor.editorState.mouseRightClick";
    private static final String MOUSE_LEFT_CLICK = "MiniEditor.editorState.mouseLeftClick";
    private static final String MOUSE_MIDDLE_CLICK = "MiniEditor.editorState.mouseMiddleClick";

    private final EditorCamera editorCamera;
    private ActionListener actionListener;

    public AdvancedAbstractEditor3DPart(T fileEditor) {
        super(fileEditor);
        editorCamera = needEditorCamera() ? createEditorCamera() : null;

        actionListener = this::onActionImpl;
    }

    private void onActionImpl(
            final String name,
            final boolean isPressed,
            final float tpf) {

    }

    @Override
    public void initialize(ApplicationStateManager manager, Application app) {
        super.initialize(manager, app);

        final Node rootNode = EditorUtil.getGlobalRootNode();
        rootNode.attachChild(getStateNode());

        final EditorCamera editorCamera = getEditorCamera();
        final InputManager inputManager = EditorUtil.getInputManager();

        registerActionListener(inputManager);
        if (editorCamera != null) {
            editorCamera.setEnabled(true);
            editorCamera.registerInput(inputManager);
        }
    }

    @EditorThread
    private void registerActionListener(final InputManager inputManager) {
        inputManager.addListener(actionListener, MOUSE_RIGHT_CLICK, MOUSE_LEFT_CLICK, MOUSE_MIDDLE_CLICK);
    }

    /**
     * @return the created editor camera
     */
    @EditorThread
    protected EditorCamera createEditorCamera() {
        final Camera camera = EditorUtil.getGlobalCamera();

        final EditorCamera editorCamera = new EditorCamera(camera, getNodeForCamera());
        // TODO: Setup parameters, near and far
        return editorCamera;
    }

    /**
     * @return the node for the camera
     */
    @EditorThread
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
    @EditorThread
    protected EditorCamera getEditorCamera() {
        return editorCamera;
    }

    @Override
    @EditorThread
    public void update(float tpf) {
        preCameraUpdate();
        cameraUpdate(tpf);
        postCameraUpdate();
    }

    @EditorThread
    protected void postCameraUpdate() {
        // TODO: Lighting for camera
    }

    @EditorThread
    private void cameraUpdate(float tpf) {
        final EditorCamera editorCamera = getEditorCamera();
        if (editorCamera == null) {
            return;
        }

        editorCamera.update(tpf);
        // TODO: Update camera controls
    }

    @EditorThread
    protected abstract void preCameraUpdate();
}
