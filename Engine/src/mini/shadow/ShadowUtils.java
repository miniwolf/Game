package mini.shadow;

import mini.bounding.BoundingBox;
import mini.bounding.BoundingVolume;
import mini.math.Matrix4f;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.renderer.ViewPort;
import mini.renderer.queue.GeometryList;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.utils.TempVars;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ShadowUtils {
    /**
     * Updates the points array to contain the frustum corners of the given camera. The nearOverride
     * and farOverride variables can be used to override the camera's near/far values with own
     * values.
     *
     * @param viewCam
     * @param nearOverride
     * @param farOverride
     * @param scale
     * @param points
     */
    public static void updateFrustumPoints(Camera viewCam, float nearOverride, float farOverride,
                                           float scale, Vector3f[] points) {

        float frustumRight = viewCam.getFrustumRight();
        float frustumTop = viewCam.getFrustumTop();

        float depthHeightRatio = frustumTop / viewCam.getFrustumNear();
        float ratio = frustumRight / frustumTop;

        float nearHeight;
        float nearWidth;
        float farHeight;
        float farWidth;
        if (viewCam.isParallelProjection()) {
            nearHeight = frustumTop;
            nearWidth = nearHeight * ratio;
            farHeight = frustumTop;
            farWidth = farHeight * ratio;
        } else {
            nearHeight = depthHeightRatio * nearOverride;
            nearWidth = nearHeight * ratio;
            farHeight = depthHeightRatio * farOverride;
            farWidth = farHeight * ratio;
        }

        Vector3f position = viewCam.getLocation();
        Vector3f direction = viewCam.getDirection();
        Vector3f temp = new Vector3f();
        temp.set(direction).multLocal(farOverride).addLocal(position);
        Vector3f farCenter = temp.clone();
        temp.set(direction).multLocal(nearOverride).addLocal(position);
        Vector3f nearCenter = temp.clone();
        Vector3f up = viewCam.getUp();
        Vector3f right = direction.cross(up).normalizeLocal();

        Vector3f nearUp = temp.set(up).multLocal(nearHeight).clone();
        Vector3f nearRight = temp.set(right).multLocal(nearWidth).clone();
        Vector3f farUp = temp.set(up).multLocal(farHeight).clone();
        Vector3f farRight = temp.set(right).multLocal(farWidth).clone();

        points[0].set(nearCenter).subtractLocal(nearUp).subtractLocal(nearRight);
        points[1].set(nearCenter).addLocal(nearUp).subtractLocal(nearRight);
        points[2].set(nearCenter).addLocal(nearUp).addLocal(nearRight);
        points[3].set(nearCenter).subtractLocal(nearUp).addLocal(nearRight);

        points[4].set(farCenter).subtractLocal(farUp).subtractLocal(farRight);
        points[5].set(farCenter).addLocal(farUp).subtractLocal(farRight);
        points[6].set(farCenter).addLocal(farUp).addLocal(farRight);
        points[7].set(farCenter).subtractLocal(farUp).addLocal(farRight);

        if (scale != 1.0f) {
            throw new NotImplementedException();
        }
    }

    /**
     * Populate the outputGeometryList with the rootScene children geometries that are in the
     * frustum of the given camera.
     *
     * @param rootScene          the rootNode of the scene to traverse
     * @param camera             the camera to check geometries against
     * @param mode               ShadowMode to apply to the geometries
     * @param outputGeometryList the list of all geometries that in the camera frustum
     */
    public static void getGeometriesInCamFrustum(Spatial rootScene, Camera camera,
                                                 RenderQueue.ShadowMode mode,
                                                 GeometryList outputGeometryList) {
        if (rootScene == null || !(rootScene instanceof Node)) {
            return;
        }

        int planeState = camera.getPlaneState();
        addGeometriesInCamFrustumFromNode(camera, (Node) rootScene, mode, outputGeometryList);
        camera.setPlaneState(planeState);
    }

    private static void addGeometriesInCamFrustumFromNode(Camera camera, Node scene,
                                                          RenderQueue.ShadowMode mode,
                                                          GeometryList outputGeometryList) {
        if (scene.getCullHint() == Spatial.CullHint.Always) {
            return;
        }

        camera.setPlaneState(0);
        if (camera.contains(scene.getWorldBound()) == Camera.FrustumIntersect.Outside) {
            return;
        }

        for (Spatial child : scene.getChildren()) {
            if (child instanceof Node) {
                addGeometriesInCamFrustumFromNode(camera, (Node) child, mode, outputGeometryList);
            } else if (child instanceof Geometry
                       && child.getCullHint() != Spatial.CullHint.Always) {
                camera.setPlaneState(0);
                if (checkShadowNode(child.getShadowMode(), mode) && !((Geometry) child).isGrouped()
                    && camera.contains(child.getWorldBound()) != Camera.FrustumIntersect.Outside) {
                    outputGeometryList.add((Geometry) child);
                }
            }
        }
    }

    /**
     * Distinguish between Occluders and Receivers
     *
     * @param shadowMode the <code>ShadowMode</code> tested
     * @param desired    the desired <code>ShadowMode</code>
     * @return Whether tested matches the desired shadowMode
     */
    private static boolean checkShadowNode(RenderQueue.ShadowMode shadowMode,
                                           RenderQueue.ShadowMode desired) {
        if (shadowMode == RenderQueue.ShadowMode.Off) {
            return false;
        }

        switch (desired) {
            case Cast:
                return shadowMode == RenderQueue.ShadowMode.Cast
                       || shadowMode == RenderQueue.ShadowMode.CastAndReceive;
            case Receive:
                return shadowMode == RenderQueue.ShadowMode.Receive
                       || shadowMode == RenderQueue.ShadowMode.CastAndReceive;
            case CastAndReceive:
                return true;
            default:
                return false;
        }
    }

    public static void updateShadowCamera(ViewPort viewPort, GeometryList receivers,
                                          Camera shadowCam, Vector3f[] points,
                                          GeometryList splitOccluders, int shadowMapSize) {
        boolean ortho = shadowCam.isParallelProjection();

        shadowCam.setProjectionMatrix(null);

        if (ortho) {
            shadowCam.setFrustum(-shadowCam.getFrustumFar(), shadowCam.getFrustumFar(), -1, 1, 1,
                                 -1);
        }

        Matrix4f viewProjectionMatrix = shadowCam.getViewProjectionMatrix();
        BoundingBox splitBBox = computeBoundForPoints(points, viewProjectionMatrix);

        TempVars vars = TempVars.get();
        BoundingBox receiverBBox = new BoundingBox();
        int receiverCount = 0;

        for (Geometry receiver : receivers) {
            BoundingVolume bv = receiver.getWorldBound();
            BoundingVolume recvBox = bv.transform(viewProjectionMatrix, vars.bbox);

            if (splitBBox.intersects(recvBox) && !Float.isNaN(recvBox.getCenter().x)
                && !Float.isInfinite(recvBox.getCenter().x)) {
                receiverBBox.mergeLocal(recvBox);
                receiverCount++;
            }
        }

        BoundingBox casterBBox = new BoundingBox();
        OccludersExtractor occludersExtractor = new OccludersExtractor(viewProjectionMatrix,
                                                                       splitBBox, casterBBox,
                                                                       splitOccluders, vars);
        for (Spatial scene : viewPort.getScenes()) {
            occludersExtractor.addOccluders(scene);
        }

        int casterCount = occludersExtractor.getCasterCount();

        // Avoid shadow bleeding when the ground is set to only receive shadows
        if (casterCount != receiverCount) {
            casterBBox.setXExtent(casterBBox.getXExtent() + 2.0f);
            casterBBox.setYExtent(casterBBox.getYExtent() + 2.0f);
            casterBBox.setZExtent(casterBBox.getZExtent() + 2.0f);
        }

        Vector3f casterMin = casterBBox.getMin(vars.vect1);
        Vector3f casterMax = casterBBox.getMax(vars.vect2);

        Vector3f receiverMin = receiverBBox.getMin(vars.vect3);
        Vector3f receiverMax = receiverBBox.getMax(vars.vect4);

        Vector3f splitMin = splitBBox.getMin(vars.vect5);
        Vector3f splitMax = splitBBox.getMax(vars.vect6);

        Vector3f cropMin = vars.vect7;
        Vector3f cropMax = vars.vect8;

        cropMin.x = max(max(casterMin.x, receiverMin.x), splitMin.x);
        cropMax.x = min(min(casterMax.x, receiverMax.x), splitMax.x);

        cropMin.y = max(max(casterMin.y, receiverMin.y), splitMin.y);
        cropMax.y = min(min(casterMax.y, receiverMax.y), splitMax.y);

        cropMin.z = min(casterMin.z, splitMin.z);
        cropMax.z = min(receiverMax.z, splitMax.z);

        // Create the crop matrix
        float scaleX, scaleY, scaleZ;
        float offsetX, offsetY, offsetZ;

        // TODO: Shadow map stabilization from Practical Cascaded Shadow Maps scale stabilization
        scaleX = (2.0f) / (cropMax.x - cropMin.x);
        scaleY = (2.0f) / (cropMax.y - cropMin.y);

        // TODO: Shadow map stabilization from Practical Cascaded Shadow Maps offset stabilization
        offsetX = -.5f * (cropMax.x + cropMin.x) * scaleX;
        offsetY = -.5f * (cropMax.y + cropMin.y) * scaleY;

        scaleZ = 1.0f / (cropMax.z - cropMin.z);
        offsetZ = -cropMin.z * scaleZ;

        Matrix4f cropMatrix = vars.tempMat4;
        cropMatrix.set(scaleX, 0f, 0f, offsetX,
                       0f, scaleY, 0f, offsetY,
                       0f, 0f, scaleZ, offsetZ,
                       0f, 0f, 0f, 1f);

        Matrix4f projMatrix = shadowCam.getProjectionMatrix();
        Matrix4f result = new Matrix4f();
        result.set(cropMatrix);
        result.multLocal(projMatrix);
        vars.release();

        shadowCam.setProjectionMatrix(result);
    }

    /**
     * @return Bounds from the array of points
     */
    private static BoundingBox computeBoundForPoints(Vector3f[] points,
                                                     Matrix4f viewProjectionMatrix) {
        Vector3f min = new Vector3f(Vector3f.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Vector3f.NEGATIVE_INFINITY);
        TempVars vars = TempVars.get();
        Vector3f temp = vars.vect1;
        for (Vector3f point : points) {
            float w = viewProjectionMatrix.multProj(point, temp);

            temp.x /= w;
            temp.y /= w;
            temp.z /= w;

            min.minLocal(temp);
            max.maxLocal(temp);
        }

        vars.release();

        Vector3f center = min.add(max).multLocal(0.5f);
        Vector3f extent = max.subtract(min).multLocal(0.5f);

        // Offset on the extent has been added to avoid banding artifacts when the frustum are aligned
        return new BoundingBox(center, extent.x + 2.0f, extent.y + 2.0f, extent.z + 2.5f);
    }

    /**
     * Updates a points array with the frustum corners of the provided camera.
     *
     * @param viewCam camera to retrieve frustum corners from
     * @param points  Vector3 array to store the vector positions, must be initialized with 8
     *                positions
     */
    public static void updateFrustumPoints(Camera viewCam, Vector3f[] points) {
        int w = viewCam.getWidth();
        int h = viewCam.getHeight();

        points[0].set(viewCam.getWorldCoordinates(new Vector2f(0, 0), 0));
        points[1].set(viewCam.getWorldCoordinates(new Vector2f(0, h), 0));
        points[2].set(viewCam.getWorldCoordinates(new Vector2f(w, h), 0));
        points[3].set(viewCam.getWorldCoordinates(new Vector2f(w, 0), 0));

        points[4].set(viewCam.getWorldCoordinates(new Vector2f(0, 0), 1));
        points[5].set(viewCam.getWorldCoordinates(new Vector2f(0, h), 1));
        points[6].set(viewCam.getWorldCoordinates(new Vector2f(w, h), 1));
        points[7].set(viewCam.getWorldCoordinates(new Vector2f(w, 0), 1));
    }
}
