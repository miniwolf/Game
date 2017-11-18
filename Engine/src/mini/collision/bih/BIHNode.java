package mini.collision.bih;

import mini.collision.CollisionResult;
import mini.collision.CollisionResults;
import mini.math.Matrix4f;
import mini.math.Ray;
import mini.math.Triangle;
import mini.math.Vector3f;
import mini.utils.TempVars;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Bounding Interval Hierachy.
 * <p>
 * Based on:
 * <p>
 * Instant Ray Tracing: The Bounding Interval Hierachy
 * By Carsten Wächter and Alexander Keller
 */
public final class BIHNode {

    private int leftIndex, rightIndex;
    private BIHNode left;
    private BIHNode right;
    private float leftPlane;
    private float rightPlane;
    private int axis;

    public BIHNode(int l, int r) {
        leftIndex = l;
        rightIndex = r;
        axis = 3; // indicates leaf
    }

    public BIHNode(int axis) {
        this.axis = axis;
    }

    public BIHNode() {
    }

    public BIHNode getLeftChild() {
        return left;
    }

    public void setLeftChild(BIHNode left) {
        this.left = left;
    }

    public float getLeftPlane() {
        return leftPlane;
    }

    public void setLeftPlane(float leftPlane) {
        this.leftPlane = leftPlane;
    }

    public BIHNode getRightChild() {
        return right;
    }

    public void setRightChild(BIHNode right) {
        this.right = right;
    }

    public float getRightPlane() {
        return rightPlane;
    }

    public void setRightPlane(float rightPlane) {
        this.rightPlane = rightPlane;
    }

    public final int intersectWhere(Ray ray, Matrix4f worldMatrix, BIHTree tree, float sceneMin,
                                    float sceneMax, CollisionResults results) {

        TempVars vars = TempVars.get();
        List<BIHStackData> stack = vars.bihStack;
        stack.clear();

        Vector3f o = vars.vect1.set(ray.origin);
        Vector3f d = vars.vect2.set(ray.direction);

        Matrix4f inv = vars.tempMat4.set(worldMatrix).invertLocal();
        inv.mult(ray.origin, ray.origin);
        inv.multNormal(ray.direction, ray.direction);

        float[] origins = {ray.origin.x, ray.origin.y, ray.origin.z};
        float[] invDirections = {1f / ray.direction.x, 1f / ray.direction.y, 1f / ray.direction.z};

        ray.direction.normalizeLocal();

        Vector3f v1 = vars.vect3,
                v2 = vars.vect4,
                v3 = vars.vect5;
        int collisions = 0;

        stack.add(new BIHStackData(this, sceneMin, sceneMax));

        stackloop:
        while (stack.size() > 0) {
            BIHStackData data = stack.remove(stack.size() - 1);
            BIHNode node = data.node;
            float tMin = data.min, tMax = data.max;

            if (tMax < tMin) {
                continue;
            }

            while (node.axis != 3) { // While node is not a leaf
                int axis = node.axis;

                // find the origin and direction value for the given axis
                float origin = origins[axis];
                float invDirection = invDirections[axis];

                float tNearSplit, tFarSplit;
                BIHNode nearNode, farNode;

                tNearSplit = (node.leftPlane - origin) * invDirection;
                tFarSplit = (node.rightPlane - origin) * invDirection;
                nearNode = node.left;
                farNode = node.right;

                if (invDirection < 0) {
                    float tmpSplit = tNearSplit;
                    tNearSplit = tFarSplit;
                    tFarSplit = tmpSplit;

                    BIHNode tmpNode = nearNode;
                    nearNode = farNode;
                    farNode = tmpNode;
                }

                if (tMin > tNearSplit && tMax < tFarSplit) {
                    continue stackloop;
                }

                if (tMin > tNearSplit) {
                    tMin = max(tMin, tFarSplit);
                    node = farNode;
                } else if (tMax < tFarSplit) {
                    tMax = min(tMax, tNearSplit);
                    node = nearNode;
                } else {
                    stack.add(new BIHStackData(farNode, max(tMin, tFarSplit), tMax));
                    tMax = min(tMax, tNearSplit);
                    node = nearNode;
                }
            }

            // a leaf
            for (int i = node.leftIndex; i <= node.rightIndex; i++) {
                tree.getTriangle(i, v1, v2, v3);

                float t = ray.intersects(v1, v2, v3);
                if (!Float.isInfinite(t)) {
                    worldMatrix.mult(v1, v1);
                    worldMatrix.mult(v2, v2);
                    worldMatrix.mult(v3, v3);
                    t = new Ray(o, d).intersects(v1, v2, v3);

                    Vector3f contactNormal = Triangle.computeTriangleNormal(v1, v2, v3, null);
                    Vector3f contactPoint = new Vector3f(d).multLocal(t).addLocal(o);
                    float worldSpaceDist = o.distance(contactPoint);

                    CollisionResult cr = new CollisionResult(contactPoint, worldSpaceDist);
                    cr.setContactNormal(contactNormal);
                    cr.setTriangleIndex(tree.getTriangleIndex(i));
                    results.addCollision(cr);
                    collisions++;
                }
            }
        }
        vars.release();
        ray.origin = o;
        ray.direction = d;

        return collisions;
    }

    public static final class BIHStackData {

        private final BIHNode node;
        private final float min, max;

        BIHStackData(BIHNode node, float min, float max) {
            this.node = node;
            this.min = min;
            this.max = max;
        }
    }
}
