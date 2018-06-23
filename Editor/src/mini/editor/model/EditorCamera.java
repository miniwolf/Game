package mini.editor.model;

import mini.input.CameraInput;
import mini.input.controls.ActionListener;
import mini.input.controls.AnalogListener;
import mini.math.FastMath;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.Spatial;
import mini.scene.control.Control;

public class EditorCamera implements ActionListener, AnalogListener, Control {
    private static final String CHASECAM_TOGGLEROTATE = EditorCamera.class.getSimpleName() + "_"
                                                        + CameraInput.CHASECAM_TOGGLEROTATE;
    private static final String CHASECAM_DOWN = EditorCamera.class.getSimpleName() + "_"
                                                + CameraInput.CHASECAM_DOWN;
    private static final String CHASECAM_UP = EditorCamera.class.getSimpleName() + "_"
                                              + CameraInput.CHASECAM_UP;
    private static final String CHASECAM_MOVELEFT = EditorCamera.class.getSimpleName() + "_"
                                                    + CameraInput.CHASECAM_MOVELEFT;
    private static final String CHASECAM_MOVERIGHT = EditorCamera.class.getSimpleName() + "_"
                                                     + CameraInput.CHASECAM_MOVERIGHT;
    private static final String CHASECAM_ZOOMIN = EditorCamera.class.getSimpleName() + "_"
                                                  + CameraInput.CHASECAM_ZOOMIN;
    private static final String CHASECAM_ZOOMOUT = EditorCamera.class.getSimpleName() + "_"
                                                   + CameraInput.CHASECAM_ZOOMOUT;
    private boolean enabled = true;
    private boolean dragToRotate = true;
    private boolean canRotate;
    private Camera camera;

    private float rotation = 0;
    private float verticalRotation = FastMath.PI / 6;
    private float distance = 20.0f;
    private float maxDistance = 1.0f;
    private float minDistance = 40.0f;

    private float targetRotation = rotation;
    private float targetVRotation = verticalRotation;
    private float targetDistance = distance;

    /**
     * @param camera the application camera
     * @param target the spatial to follow
     */
    public EditorCamera(final Camera camera,
                        final Spatial target) {
        this(camera);
        target.addControl(this);
    }

    private EditorCamera(final Camera camera) {
        this.camera = camera;
    }

    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        if (!enabled || !dragToRotate) {
            return;
        }

        if (keyPressed) {
            canRotate = true;
        } else {
            canRotate = false;
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (!enabled) {
            return;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void update(float tpf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spatial getSpatial() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSpatial(Spatial spatial) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void render(RenderManager renderManager, ViewPort vp) {
        throw new UnsupportedOperationException();
    }

    public void setTargetRotation(final float targetRotation) {
        this.targetRotation = targetRotation;
    }

    public void setTargetVRotation(float targetVRotation) {
        this.targetVRotation = targetVRotation;
    }

    public void setTargetDistance(float targetDistance) {
        this.targetDistance = Math.max(Math.min(targetDistance, maxDistance), minDistance);
    }
}
