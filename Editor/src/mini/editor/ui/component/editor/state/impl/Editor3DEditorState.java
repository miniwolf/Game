package mini.editor.ui.component.editor.state.impl;

import mini.editor.annotation.FxThread;
import mini.math.Vector3f;

import java.util.Objects;

public class Editor3DEditorState extends AbstractEditorState {

    private volatile Vector3f cameraLocation;

    private volatile float cameraHRotation;

    private volatile float cameraVRotation;

    private volatile float cameraTDistance;
    private volatile float cameraSpeed;

    public Editor3DEditorState() {
        this.cameraLocation = new Vector3f();
    }

    public Vector3f getCameraLocation() {
        if (cameraLocation == null) {
            cameraLocation = new Vector3f();
        }
        return cameraLocation;
    }

    public void setCameraLocation(Vector3f cameraLocation) {
        final boolean changed = Objects.equals(getCameraLocation(), cameraLocation);
        getCameraLocation().set(cameraLocation);
        if (changed) {
            notifyChanged();
        }
    }

    /**
     * @return the horizontal camera rotation.
     */
    @FxThread
    public float getCameraHRotation() {
        return cameraHRotation;
    }

    /**
     * @return the vertical camera rotation
     */
    @FxThread
    public float getCameraVRotation() {
        return cameraVRotation;
    }

    /**
     * @return the camera zoom.
     */
    @FxThread
    public float getCameraTDistance() {
        return cameraTDistance;
    }

    /**
     * @return the camera speed.
     */
    @FxThread
    public float getCameraSpeed() {
        return cameraSpeed;
    }
}
