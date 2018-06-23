package mini.editor.ui.component.editor;

import mini.editor.manager.ExecutorManager;
import mini.editor.part3d.editor.impl.AdvancedAbstractEditor3DPart;
import mini.editor.ui.component.editor.state.impl.Editor3DEditorState;
import mini.math.Vector3f;

public class FileEditorUtils {
    protected static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    public static void loadCameraState(
            final Editor3DEditorState editorState,
            final AdvancedAbstractEditor3DPart editor3DPart) {
        final Vector3f cameraLocation = editorState.getCameraLocation();

        final float hRotation = editorState.getCameraHRotation();
        final float vRotation = editorState.getCameraVRotation();
        final float tDistance = editorState.getCameraTDistance();
        final float cameraSpeed = editorState.getCameraSpeed();

        EXECUTOR_MANAGER.addEditorTask(() -> editor3DPart
                .updateCameraSettings(cameraLocation, hRotation, vRotation, tDistance,
                                      cameraSpeed));
    }
}
