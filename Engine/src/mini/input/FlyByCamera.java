package mini.input;

import mini.input.controls.ActionListener;
import mini.input.controls.AnalogListener;
import mini.input.controls.KeyTrigger;
import mini.input.controls.MouseAxisTrigger;
import mini.input.controls.MouseButtonTrigger;
import mini.math.FastMath;
import mini.math.Matrix3f;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.renderer.Camera;

/**
 * A first person view camera controller.
 * After creation, you must register the camera controller with the
 * dispatcher using #registerWithDispatcher().
 * <p>
 * Controls:
 * - Move the mouse to rotate the camera
 * - Mouse wheel for zooming in or out
 * - WASD keys for moving forward/backward and strafing
 * - QZ keys raise or lower the camera
 */
public class FlyByCamera implements AnalogListener, ActionListener {

    private static String[] mappings = new String[]{
            CameraInput.FLYCAM_LEFT,
            CameraInput.FLYCAM_RIGHT,
            CameraInput.FLYCAM_UP,
            CameraInput.FLYCAM_DOWN,

            CameraInput.FLYCAM_STRAFELEFT,
            CameraInput.FLYCAM_STRAFERIGHT,
            CameraInput.FLYCAM_FORWARD,
            CameraInput.FLYCAM_BACKWARD,

            CameraInput.FLYCAM_ZOOMIN,
            CameraInput.FLYCAM_ZOOMOUT,
            CameraInput.FLYCAM_ROTATEDRAG,

            CameraInput.FLYCAM_RISE,
            CameraInput.FLYCAM_LOWER,

            CameraInput.FLYCAM_INVERTY
    };

    protected Camera cam;
    protected Vector3f initialUpVec;
    protected float rotationSpeed = 1f;
    protected float moveSpeed = 3f;
    protected float zoomSpeed = 1f;
    protected boolean enabled = true;
    protected boolean dragToRotate = false;
    protected boolean canRotate = false;
    protected boolean invertY = false;
    protected InputManager inputManager;

    /**
     * Creates a new FlyByCamera to control the given Camera object.
     *
     * @param cam
     */
    public FlyByCamera(Camera cam) {
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
    }

    /**
     * Sets the up vector that should be used for the camera.
     *
     * @param upVec
     */
    public void setUpVector(Vector3f upVec) {
        initialUpVec.set(upVec);
    }

    /**
     * Sets the move speed. The speed is given in world units per second.
     *
     * @param moveSpeed
     */
    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    /**
     * Gets the move speed. The speed is given in world units per second.
     *
     * @return moveSpeed
     */
    public float getMoveSpeed() {
        return moveSpeed;
    }

    /**
     * Sets the rotation speed.
     *
     * @param rotationSpeed
     */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Gets the move speed. The speed is given in world units per second.
     *
     * @return rotationSpeed
     */
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    /**
     * Sets the zoom speed.
     *
     * @param zoomSpeed
     */
    public void setZoomSpeed(float zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    /**
     * Gets the zoom speed.  The speed is a multiplier to increase/decrease
     * the zoom rate.
     *
     * @return zoomSpeed
     */
    public float getZoomSpeed() {
        return zoomSpeed;
    }

    /**
     * @param enable If false, the camera will ignore input.
     */
    public void setEnabled(boolean enable) {
        if (enabled && !enable) {
            if (inputManager != null && (!dragToRotate || (dragToRotate && canRotate))) {
                inputManager.setCursorVisible(true);
            }
        }
        enabled = enable;
    }

    /**
     * @return If enabled
     * @see FlyByCamera#setEnabled(boolean)
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return If drag to rotate feature is enabled.
     * @see FlyByCamera#setDragToRotate(boolean)
     */
    public boolean isDragToRotate() {
        return dragToRotate;
    }

    /**
     * Set if drag to rotate mode is enabled.
     * <p>
     * When true, the user must hold the mouse button
     * and drag over the screen to rotate the camera, and the cursor is
     * visible until dragged. Otherwise, the cursor is invisible at all times
     * and holding the mouse button is not needed to rotate the camera.
     * This feature is disabled by default.
     *
     * @param dragToRotate True if drag to rotate mode is enabled.
     */
    public void setDragToRotate(boolean dragToRotate) {
        this.dragToRotate = dragToRotate;
        if (inputManager != null) {
            inputManager.setCursorVisible(dragToRotate);
        }
    }

    /**
     * Registers the FlyByCamera to receive input events from the provided
     * Dispatcher.
     *
     * @param inputManager
     */
    public void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;

        // both mouse and button - rotation of cam
        inputManager
                .addMapping(CameraInput.FLYCAM_LEFT, new MouseAxisTrigger(MouseInput.AXIS_X, true),
                            new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping(CameraInput.FLYCAM_RIGHT,
                                new MouseAxisTrigger(MouseInput.AXIS_X, false),
                                new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager
                .addMapping(CameraInput.FLYCAM_UP, new MouseAxisTrigger(MouseInput.AXIS_Y, false),
                            new KeyTrigger(KeyInput.KEY_UP));

        inputManager
                .addMapping(CameraInput.FLYCAM_DOWN, new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                            new KeyTrigger(KeyInput.KEY_DOWN));

        // mouse only - zoom in/out with wheel, and rotate drag
        inputManager.addMapping(CameraInput.FLYCAM_ZOOMIN,
                                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(CameraInput.FLYCAM_ZOOMOUT,
                                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping(CameraInput.FLYCAM_ROTATEDRAG,
                                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // keyboard only WASD for movement and WZ for rise/lower height
        inputManager.addMapping(CameraInput.FLYCAM_STRAFELEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(CameraInput.FLYCAM_STRAFERIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(CameraInput.FLYCAM_FORWARD, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(CameraInput.FLYCAM_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(CameraInput.FLYCAM_RISE, new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(CameraInput.FLYCAM_LOWER, new KeyTrigger(KeyInput.KEY_Z));

        inputManager.addListener(this, mappings);
        inputManager.setCursorVisible(dragToRotate || !isEnabled());
    }

    /**
     * Unregisters the FlyByCamera from the event Dispatcher.
     */
    public void unregisterInput() {

        if (inputManager == null) {
            return;
        }

        for (String s : mappings) {
            if (inputManager.hasMapping(s)) {
                inputManager.deleteMapping(s);
            }
        }

        inputManager.removeListener(this);
        inputManager.setCursorVisible(!dragToRotate);
    }

    protected void rotateCamera(float value, Vector3f axis) {
        if (dragToRotate) {
            if (canRotate) {
//                value = -value;
            } else {
                return;
            }
        }

        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(rotationSpeed * value, axis);

        Vector3f up = cam.getUp();
        Vector3f left = cam.getLeft();
        Vector3f dir = cam.getDirection();

        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);

        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalizeLocal();

        cam.setAxes(q);
    }

    protected void zoomCamera(float value) {
        // derive fovY value
        float h = cam.getFrustumTop();
        float w = cam.getFrustumRight();
        float aspect = w / h;

        float near = cam.getFrustumNear();

        float fovY = FastMath.atan(h / near)
                     / (FastMath.DEG_TO_RAD * .5f);
        float newFovY = fovY + value * 0.1f * zoomSpeed;
        if (newFovY > 0f) {
            // Don't let the FOV go zero or negative.
            fovY = newFovY;
        }

        h = FastMath.tan(fovY * FastMath.DEG_TO_RAD * .5f) * near;
        w = h * aspect;

        cam.setFrustumTop(h);
        cam.setFrustumBottom(-h);
        cam.setFrustumLeft(-w);
        cam.setFrustumRight(w);
    }

    protected void riseCamera(float value) {
        Vector3f vel = new Vector3f(0, value * moveSpeed, 0);
        Vector3f pos = cam.getLocation().clone();

        pos.addLocal(vel);

        cam.setLocation(pos);
    }

    protected void moveCamera(float value, boolean sideways) {
        Vector3f vel = new Vector3f();
        Vector3f pos = cam.getLocation().clone();

        if (sideways) {
            cam.getLeft(vel);
        } else {
            cam.getDirection(vel);
        }
        vel.multLocal(value * moveSpeed);

        pos.addLocal(vel);

        cam.setLocation(pos);
    }

    public void onAnalog(String name, float value, float tpf) {
        if (!enabled) {
            return;
        }

        switch (name) {
            case CameraInput.FLYCAM_LEFT:
                rotateCamera(value, initialUpVec);
                break;
            case CameraInput.FLYCAM_RIGHT:
                rotateCamera(-value, initialUpVec);
                break;
            case CameraInput.FLYCAM_UP:
                rotateCamera(-value * (invertY ? -1 : 1), cam.getLeft());
                break;
            case CameraInput.FLYCAM_DOWN:
                rotateCamera(value * (invertY ? -1 : 1), cam.getLeft());
                break;
            case CameraInput.FLYCAM_FORWARD:
                moveCamera(value, false);
                break;
            case CameraInput.FLYCAM_BACKWARD:
                moveCamera(-value, false);
                break;
            case CameraInput.FLYCAM_STRAFELEFT:
                moveCamera(value, true);
                break;
            case CameraInput.FLYCAM_STRAFERIGHT:
                moveCamera(-value, true);
                break;
            case CameraInput.FLYCAM_RISE:
                riseCamera(value);
                break;
            case CameraInput.FLYCAM_LOWER:
                riseCamera(-value);
                break;
            case CameraInput.FLYCAM_ZOOMIN:
                zoomCamera(value);
                break;
            case CameraInput.FLYCAM_ZOOMOUT:
                zoomCamera(-value);
                break;
        }
    }

    public void onAction(String name, boolean value, float tpf) {
        if (!enabled) {
            return;
        }

        if (name.equals(CameraInput.FLYCAM_ROTATEDRAG) && dragToRotate) {
            canRotate = value;
            inputManager.setCursorVisible(!value);
        } else if (name.equals(CameraInput.FLYCAM_INVERTY)) {
            // Toggle on the up.
            if (!value) {
                invertY = !invertY;
            }
        }
    }

}

