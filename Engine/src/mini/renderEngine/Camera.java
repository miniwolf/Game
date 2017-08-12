package mini.renderEngine;

import mini.input.KeyboardKey;
import mini.input.KeyboardListener;
import mini.input.MouseButton;
import mini.input.MouseListener;
import mini.math.FastMath;
import mini.math.Matrix4f;
import mini.math.Plane;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.utils.TempVars;

/**
 * This camera is implementing the techniques of a follow camera.
 */
public class Camera implements MouseListener, KeyboardListener {
    /**
     * The <code>FrustumIntersect</code> enum is returned as a result
     * of a culling check operation,
     * see {@link #contains(bounding.BoundingVolume) }
     */
    public enum FrustumIntersect {

        /**
         * defines a constant assigned to spatials that are completely outside
         * of this camera's view frustum.
         */
        Outside,
        /**
         * defines a constant assigned to spatials that are completely inside
         * the camera's view frustum.
         */
        Inside,
        /**
         * defines a constant assigned to spatials that are intersecting one of
         * the six planes that define the view frustum.
         */
        Intersects
    }
    /**
     * LEFT_PLANE represents the left plane of the camera frustum.
     */
    private static final int LEFT_PLANE = 0;
    /**
     * RIGHT_PLANE represents the right plane of the camera frustum.
     */
    private static final int RIGHT_PLANE = 1;
    /**
     * BOTTOM_PLANE represents the bottom plane of the camera frustum.
     */
    private static final int BOTTOM_PLANE = 2;
    /**
     * TOP_PLANE represents the top plane of the camera frustum.
     */
    private static final int TOP_PLANE = 3;
    /**
     * FAR_PLANE represents the far plane of the camera frustum.
     */
    private static final int FAR_PLANE = 4;
    /**
     * NEAR_PLANE represents the near plane of the camera frustum.
     */
    private static final int NEAR_PLANE = 5;
    /**
     * FRUSTUM_PLANES represents the number of planes of the camera frustum.
     */
    private static final int FRUSTUM_PLANES = 6;
    /**
     * MAX_WORLD_PLANES holds the maximum planes allowed by the system.
     */
    private static final int MAX_WORLD_PLANES = 6;
    /**
     * Camera's location
     */
    protected Vector3f location;
    /**
     * The orientation of the camera.
     */
    protected Quaternion rotation;
    /**
     * Distance from camera to near frustum plane.
     */
    protected float frustumNear;
    /**
     * Distance from camera to far frustum plane.
     */
    protected float frustumFar;
    /**
     * Distance from camera to left frustum plane.
     */
    protected float frustumLeft;
    /**
     * Distance from camera to right frustum plane.
     */
    protected float frustumRight;
    /**
     * Distance from camera to top frustum plane.
     */
    protected float frustumTop;
    /**
     * Distance from camera to bottom frustum plane.
     */
    protected float frustumBottom;
    //Temporary values computed in onFrustumChange that are needed if a
    //call is made to onFrameChange.
    protected float[] coeffLeft;
    protected float[] coeffRight;
    protected float[] coeffBottom;
    protected float[] coeffTop;
    //view port coordinates
    /**
     * Percent value on display where horizontal viewing starts for this camera.
     * Default is 0.
     */
    protected float viewPortLeft;
    /**
     * Percent value on display where horizontal viewing ends for this camera.
     * Default is 1.
     */
    protected float viewPortRight;
    /**
     * Percent value on display where vertical viewing ends for this camera.
     * Default is 1.
     */
    protected float viewPortTop;
    /**
     * Percent value on display where vertical viewing begins for this camera.
     * Default is 0.
     */
    protected float viewPortBottom;
    /**
     * Array holding the planes that this camera will check for culling.
     */
    protected Plane[] worldPlane;
    /**
     * A mask value set during contains() that allows fast culling of a Node's
     * children.
     */
    private int planeState;
    protected int width;
    protected int height;
    protected boolean viewportChanged = true;
    /**
     * store the value for field parallelProjection
     */
    private boolean parallelProjection = true;
    protected Matrix4f projectionMatrixOverride = new Matrix4f();
    private boolean overrideProjection;
    protected Matrix4f viewMatrix = new Matrix4f();
    protected Matrix4f projectionMatrix = new Matrix4f();
    protected Matrix4f viewProjectionMatrix = new Matrix4f();
    /** The camera's name. */
    protected String name;

//    public Camera() {
//        this.projectionMatrix = createProjectionMatrix();
//        Mouse.addMouseListener(this);
//        Keyboard.addListener(this);
//    }

    /**
     * Serialization only. Do not use.
     */
    public Camera() {
        worldPlane = new Plane[MAX_WORLD_PLANES];
        for (int i = 0; i < MAX_WORLD_PLANES; i++) {
            worldPlane[i] = new Plane();
        }
    }

    /**
     * Constructor instantiates a new <code>Camera</code> object. All
     * values of the camera are set to default.
     */
    public Camera(int width, int height) {
        this();
        location = new Vector3f();
        rotation = new Quaternion();

        frustumNear = 1.0f;
        frustumFar = 2.0f;
        frustumLeft = -0.5f;
        frustumRight = 0.5f;
        frustumTop = 0.5f;
        frustumBottom = -0.5f;

        coeffLeft = new float[2];
        coeffRight = new float[2];
        coeffBottom = new float[2];
        coeffTop = new float[2];

        viewPortLeft = 0.0f;
        viewPortRight = 1.0f;
        viewPortTop = 1.0f;
        viewPortBottom = 0.0f;

        this.width = width;
        this.height = height;

        onFrustumChange();
        onViewPortChange();
        onFrameChange();
    }

    /**
     * Resizes this camera's view with the given width and height. This is
     * similar to constructing a new camera, but reusing the same Object. This
     * method is called by an associated {@link RenderManager} to notify the camera of
     * changes in the display dimensions.
     *
     * @param width the view width
     * @param height the view height
     * @param fixAspect If true, the camera's aspect ratio will be recomputed.
     * Recomputing the aspect ratio requires changing the frustum values.
     */
    public void resize(int width, int height, boolean fixAspect) {
        this.width = width;
        this.height = height;
        onViewPortChange();

        if (fixAspect /*&& !parallelProjection*/) {
            frustumRight = frustumTop * ((float) width / height);
            frustumLeft = -frustumRight;
            onFrustumChange();
        }
    }

    /**
     * <code>lookAt</code> is a convenience method for auto-setting the frame
     * based on a world position the user desires the camera to look at. It
     * repoints the camera towards the given position using the difference
     * between the position and the current camera location as a direction
     * vector and the worldUpVector to compute up and left camera vectors.
     *
     * @param pos           where to look at in terms of world coordinates
     * @param worldUpVector a normalized vector indicating the up direction of the world.
     *                      (typically {0, 1, 0} in jME.)
     */
    public void lookAt(Vector3f pos, Vector3f worldUpVector) {
        TempVars vars = TempVars.get();
        Vector3f newDirection = vars.vect1;
        Vector3f newUp = vars.vect2;
        Vector3f newLeft = vars.vect3;

        newDirection.set(pos).subtractLocal(location).normalizeLocal();

        newUp.set(worldUpVector).normalizeLocal();
        if (newUp.equals(Vector3f.ZERO)) {
            newUp.set(Vector3f.UNIT_Y);
        }

        newLeft.set(newUp).crossLocal(newDirection).normalizeLocal();
        if (newLeft.equals(Vector3f.ZERO)) {
            if (newDirection.x != 0) {
                newLeft.set(newDirection.y, -newDirection.x, 0f);
            } else {
                newLeft.set(0f, newDirection.z, -newDirection.y);
            }
        }

        newUp.set(newDirection).crossLocal(newLeft).normalizeLocal();

        this.rotation.fromAxes(newLeft, newUp, newDirection);
        this.rotation.normalizeLocal();
        vars.release();

        onFrameChange();
    }

    /**
     * <code>setFrustumPerspective</code> defines the frustum for the camera.  This
     * frustum is defined by a viewing angle, aspect ratio, and near/far planes
     *
     * @param fovY   Frame of view angle along the Y in degrees.
     * @param aspect Width:Height ratio
     * @param near   Near view plane distance
     * @param far    Far view plane distance
     */
    public void setFrustumPerspective(float fovY, float aspect, float near,
                                      float far) {
        if (Float.isNaN(aspect) || Float.isInfinite(aspect)) {
            // ignore.
            System.err.println("Warning: Invalid aspect given to setFrustumPerspective: " + aspect);
            return;
        }

        float h = FastMath.tan(fovY * FastMath.DEG_TO_RAD * .5f) * near;
        float w = h * aspect;
        frustumLeft = -w;
        frustumRight = w;
        frustumBottom = -h;
        frustumTop = h;
        frustumNear = near;
        frustumFar = far;

        // Camera is no longer parallel projection even if it was before
        parallelProjection = false;

        onFrustumChange();
    }

    public void update() {
    }

    public Vector3f getPosition() {
        return location;
    }

    /**
     * @return the width/resolution of the display.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height/resolution of the display.
     */
    public int getHeight() {
        return height;
    }

    /**
     * <code>setLocation</code> sets the position of the camera.
     *
     * @param location the position of the camera.
     */
    public void setPosition(Vector3f location) {
        this.location.set(location);
        onFrameChange();
    }

    /**
     * <code>setRotation</code> sets the orientation of this camera. This will
     * be equivalent to setting each of the axes:
     * <code><br>
     * cam.setLeft(rotation.getRotationColumn(0));<br>
     * cam.setUp(rotation.getRotationColumn(1));<br>
     * cam.setDirection(rotation.getRotationColumn(2));<br>
     * </code>
     *
     * @param rotation the rotation of this camera
     */
    public void setRotation(Quaternion rotation) {
        this.rotation.set(rotation);
        onFrameChange();
    }

    /**
     * <code>getDirection</code> retrieves the direction vector the camera is
     * facing.
     *
     * @return the direction the camera is facing.
     * @see Camera#getDirection()
     */
    public Vector3f getDirection(Vector3f store) {
        return rotation.getRotationColumn(2, store);
    }

    /**
     * <code>getLeft</code> retrieves the left axis of the camera.
     *
     * @return the left axis of the camera.
     * @see Camera#getLeft()
     */
    public Vector3f getLeft(Vector3f store) {
        return rotation.getRotationColumn(0, store);
    }

    /**
     * <code>getUp</code> retrieves the up axis of the camera.
     *
     * @return the up axis of the camera.
     * @see Camera#getUp()
     */
    public Vector3f getUp(Vector3f store) {
        return rotation.getRotationColumn(1, store);
    }

    /**
     * Called when the viewport has been changed.
     */
    public void onViewPortChange() {
        viewportChanged = true;
    }

    /**
     * <code>getPlaneState</code> returns the state of the frustum planes. So
     * checks can be made as to which frustum plane has been examined for
     * culling thus far.
     *
     * @return the current plane state int.
     */
    public int getPlaneState() {
        return planeState;
    }

    /**
     * <code>setPlaneState</code> sets the state to keep track of tested
     * planes for culling.
     *
     * @param planeState the updated state.
     */
    public void setPlaneState(int planeState) {
        this.planeState = planeState;
    }

    /**
     * <code>getViewPortLeft</code> gets the left boundary of the viewport
     *
     * @return the left boundary of the viewport
     */
    public float getViewPortLeft() {
        return viewPortLeft;
    }

    /**
     * <code>setViewPortLeft</code> sets the left boundary of the viewport
     *
     * @param left the left boundary of the viewport
     */
    public void setViewPortLeft(float left) {
        viewPortLeft = left;
        onViewPortChange();
    }

    /**
     * <code>getViewPortRight</code> gets the right boundary of the viewport
     *
     * @return the right boundary of the viewport
     */
    public float getViewPortRight() {
        return viewPortRight;
    }

    /**
     * <code>setViewPortRight</code> sets the right boundary of the viewport
     *
     * @param right the right boundary of the viewport
     */
    public void setViewPortRight(float right) {
        viewPortRight = right;
        onViewPortChange();
    }

    /**
     * <code>getViewPortTop</code> gets the top boundary of the viewport
     *
     * @return the top boundary of the viewport
     */
    public float getViewPortTop() {
        return viewPortTop;
    }

    /**
     * <code>setViewPortTop</code> sets the top boundary of the viewport
     *
     * @param top the top boundary of the viewport
     */
    public void setViewPortTop(float top) {
        viewPortTop = top;
        onViewPortChange();
    }

    /**
     * <code>getViewPortBottom</code> gets the bottom boundary of the viewport
     *
     * @return the bottom boundary of the viewport
     */
    public float getViewPortBottom() {
        return viewPortBottom;
    }

    /**
     * <code>setViewPortBottom</code> sets the bottom boundary of the viewport
     *
     * @param bottom the bottom boundary of the viewport
     */
    public void setViewPortBottom(float bottom) {
        viewPortBottom = bottom;
        onViewPortChange();
    }

    /**
     * <code>setViewPort</code> sets the boundaries of the viewport
     *
     * @param left   the left boundary of the viewport (default: 0)
     * @param right  the right boundary of the viewport (default: 1)
     * @param bottom the bottom boundary of the viewport (default: 0)
     * @param top    the top boundary of the viewport (default: 1)
     */
    public void setViewPort(float left, float right, float bottom, float top) {
        this.viewPortLeft = left;
        this.viewPortRight = right;
        this.viewPortBottom = bottom;
        this.viewPortTop = top;
        onViewPortChange();
    }

    /**
     * <code>onFrameChange</code> updates the view frame of the camera.
     */
    public void onFrameChange() {
        TempVars vars = TempVars.get();

        Vector3f left = getLeft(vars.vect1);
        Vector3f direction = getDirection(vars.vect2);
        Vector3f up = getUp(vars.vect3);

        float dirDotLocation = direction.dot(location);

        // left plane
        Vector3f leftPlaneNormal = worldPlane[LEFT_PLANE].getNormal();
        leftPlaneNormal.x = left.x * coeffLeft[0];
        leftPlaneNormal.y = left.y * coeffLeft[0];
        leftPlaneNormal.z = left.z * coeffLeft[0];
        leftPlaneNormal.addLocal(direction.x * coeffLeft[1], direction.y
                                                             * coeffLeft[1], direction.z * coeffLeft[1]);
        worldPlane[LEFT_PLANE].setConstant(location.dot(leftPlaneNormal));

        // right plane
        Vector3f rightPlaneNormal = worldPlane[RIGHT_PLANE].getNormal();
        rightPlaneNormal.x = left.x * coeffRight[0];
        rightPlaneNormal.y = left.y * coeffRight[0];
        rightPlaneNormal.z = left.z * coeffRight[0];
        rightPlaneNormal.addLocal(direction.x * coeffRight[1], direction.y
                                                               * coeffRight[1], direction.z * coeffRight[1]);
        worldPlane[RIGHT_PLANE].setConstant(location.dot(rightPlaneNormal));

        // bottom plane
        Vector3f bottomPlaneNormal = worldPlane[BOTTOM_PLANE].getNormal();
        bottomPlaneNormal.x = up.x * coeffBottom[0];
        bottomPlaneNormal.y = up.y * coeffBottom[0];
        bottomPlaneNormal.z = up.z * coeffBottom[0];
        bottomPlaneNormal.addLocal(direction.x * coeffBottom[1], direction.y
                                                                 * coeffBottom[1], direction.z * coeffBottom[1]);
        worldPlane[BOTTOM_PLANE].setConstant(location.dot(bottomPlaneNormal));

        // top plane
        Vector3f topPlaneNormal = worldPlane[TOP_PLANE].getNormal();
        topPlaneNormal.x = up.x * coeffTop[0];
        topPlaneNormal.y = up.y * coeffTop[0];
        topPlaneNormal.z = up.z * coeffTop[0];
        topPlaneNormal.addLocal(direction.x * coeffTop[1], direction.y
                                                           * coeffTop[1], direction.z * coeffTop[1]);
        worldPlane[TOP_PLANE].setConstant(location.dot(topPlaneNormal));

        // far plane
        worldPlane[FAR_PLANE].setNormal(left);
        worldPlane[FAR_PLANE].setNormal(-direction.x, -direction.y, -direction.z);
        worldPlane[FAR_PLANE].setConstant(-(dirDotLocation + frustumFar));

        // near plane
        worldPlane[NEAR_PLANE].setNormal(direction.x, direction.y, direction.z);
        worldPlane[NEAR_PLANE].setConstant(dirDotLocation + frustumNear);

        viewMatrix.fromFrame(location, direction, up, left);

        vars.release();

        updateViewProjection();
    }

    /**
     * <code>onFrustumChange</code> updates the frustum to reflect any changes
     * made to the planes. The new frustum values are kept in a temporary
     * location for use when calculating the new frame. The projection
     * matrix is updated to reflect the current values of the frustum.
     */
    private void onFrustumChange() {
        coeffLeft[0] = 1;
        coeffLeft[1] = 0;

        coeffRight[0] = -1;
        coeffRight[1] = 0;

        coeffBottom[0] = 1;
        coeffBottom[1] = 0;

        coeffTop[0] = -1;
        coeffTop[1] = 0;

        projectionMatrix.fromFrustum(frustumNear, frustumFar, frustumLeft, frustumRight, frustumTop, frustumBottom, parallelProjection);

        // The frame is effected by the frustum values
        // update it as well
        onFrameChange();
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Updates the view projection matrix.
     */
    private void updateViewProjection() {
        if (overrideProjection) {
            viewProjectionMatrix.set(projectionMatrixOverride).multLocal(viewMatrix);
        } else {
            viewProjectionMatrix.set(projectionMatrix).multLocal(viewMatrix);
        }
    }

    /**
     * @return True if the viewport (width, height, left, right, bottom, up)
     * has been changed. This is needed in the renderer so that the proper
     * viewport can be set-up.
     */
    public boolean isViewportChanged() {
        return viewportChanged;
    }

    /**
     * Clears the viewport changed flag once it has been updated inside
     * the renderer.
     */
    public void clearViewportChanged() {
        viewportChanged = false;
    }

    public Matrix4f getProjectionViewMatrix() {
        return projectionMatrix.mult(viewMatrix);
    }

    @Override
    public void onClick(MouseButton btn, double x, double y) {
    }

    @Override
    public void onRelease(MouseButton btn, double x, double y) {
    }

    @Override
    public void onScroll(double offset) {
    }

    @Override
    public void onClick(KeyboardKey key, int mods) {
    }

    @Override
    public void onRelease(KeyboardKey key, int mods) {
    }
}
