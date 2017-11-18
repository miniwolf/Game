package mini.collision.bih;

import mini.bounding.BoundingBox;
import mini.bounding.BoundingVolume;
import mini.collision.Collidable;
import mini.collision.CollisionResults;
import mini.math.FastMath;
import mini.math.Matrix4f;
import mini.math.Ray;
import mini.math.Vector3f;
import mini.scene.CollisionData;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.scene.mesh.IndexBuffer;
import mini.scene.mesh.VirtualIndexBuffer;
import mini.scene.mesh.WrappedIndexBuffer;
import mini.utils.TempVars;

import java.nio.FloatBuffer;

import static java.lang.Math.max;

/**
 * Bounding Interval Hierachy.
 * <p>
 * Based on:
 * <p>
 * Instant Ray Tracing: The Bounding Interval Hierachy
 * By Carsten Wächter and Alexander Keller
 */
public class BIHTree implements CollisionData {
    private static final int MAX_TRIS_PER_NODE = 21;
    private static final int MAX_TREE_DEPTH = 100;
    private final Mesh mesh;
    private final int maxTrisPerNode;
    private final int numTris;
    private BIHNode root;
    private int[] triIndices;
    private float[] bihSwapArray;
    private float[] pointData;
    private int triangleIndex;

    public BIHTree(Mesh mesh) {
        this(mesh, MAX_TRIS_PER_NODE);
    }

    public BIHTree(Mesh mesh, int maxTrisPerNode) {
        this.mesh = mesh;
        this.maxTrisPerNode = maxTrisPerNode;

        if (maxTrisPerNode < 1) {
            throw new IllegalArgumentException("maxTrisPerNode cannot be less than 1");
        }
        if (mesh == null) {
            throw new IllegalArgumentException("Mesh cannot be null");
        }

        bihSwapArray = new float[9];

        VertexBuffer vertexBuffer = mesh.getBuffer(VertexBuffer.Type.Position);
        if (vertexBuffer == null) {
            throw new IllegalArgumentException("A mesh should at least contain a Position buffer");
        }
        IndexBuffer ib = mesh.getIndexBuffer();
        FloatBuffer vb = (FloatBuffer) vertexBuffer.getData();

        if (ib == null) {
            ib = new VirtualIndexBuffer(mesh.getVertexCount(), mesh.getMode());
        } else if (mesh.getMode() != Mesh.Mode.Triangles) {
            ib = new WrappedIndexBuffer(mesh);
        }

        numTris = ib.size() / 3;
        initTrisList(vb, ib);
    }

    private void initTrisList(FloatBuffer vb, IndexBuffer ib) {
        pointData = new float[numTris * 3 * 3];
        int p = 0;
        for (int i = 0; i < numTris; i += 3) {
            int vert = ib.get(i) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);

            vert = ib.get(i + 1) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);

            vert = ib.get(i + 2) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);
        }

        triIndices = new int[numTris];
        for (int i = 0; i < numTris; i++) {
            triIndices[i] = i;
        }
    }

    @Override
    public int collideWith(Collidable other, Matrix4f worldMatrix, BoundingVolume worldBound,
                           CollisionResults results) {
        if (other instanceof Ray) {
            Ray ray = (Ray) other;
            return collideWithRay(ray, worldMatrix, worldBound, results);
        } else {
            throw new UnsupportedOperationException("Collidable: " + other);
        }
    }

    private int collideWithRay(Ray ray, Matrix4f worldMatrix, BoundingVolume worldBound,
                               CollisionResults results) {
        TempVars vars = TempVars.get();

        CollisionResults collisionResults = vars.collisionResults;
        collisionResults.clear();
        worldBound.collideWith(ray, collisionResults);
        if (collisionResults.size() <= 0) {
            vars.release();
            return 0;
        }

        float tMin = collisionResults.getClosestCollision().getDistance();
        float tMax = collisionResults.getFarthestCollision().getDistance();

        if (tMax <= 0) {
            tMax = Float.POSITIVE_INFINITY;
        } else if (tMin == tMax) {
            tMin = 0;
        }

        if (tMin <= 0) {
            tMin = 0;
        }

        if (ray.getLimit() < Float.POSITIVE_INFINITY) {
            tMax = Math.min(tMax, ray.limit);
            if (tMin > tMax) {
                vars.release();
                return 0;
            }
        }

        vars.release();
        return root.intersectWhere(ray, worldMatrix, this, tMin, tMax, results);

    }

    public void construct() {
        BoundingBox sceneBox = createBox(0, numTris - 1);
        root = createNode(0, numTris - 1, sceneBox, 0);
    }

    private BIHNode createNode(int l, int r, BoundingBox nodeBbox, int depth) {
        if ((r - l) < maxTrisPerNode || depth > MAX_TREE_DEPTH) {
            return new BIHNode(l, r);
        }

        BoundingBox currentBox = createBox(l, r);

        Vector3f exteriorExt = nodeBbox.getExtent(null);
        Vector3f interiorExt = currentBox.getExtent(null);
        exteriorExt.subtractLocal(interiorExt);

        int axis = 0;
        if (exteriorExt.x > interiorExt.y) {
            if (exteriorExt.x > exteriorExt.z) {
                axis = 0;
            } else {
                axis = 2;
            }
        } else {
            if (exteriorExt.y > exteriorExt.z) {
                axis = 1;
            } else {
                axis = 2;
            }
        }
        if (exteriorExt.equals(Vector3f.ZERO)) {
            axis = 0;
        }

        float split = currentBox.getCenter().get(axis);
        int pivot = sortTriangles(l, r, split, axis);
        if (pivot == l || pivot == r) {
            pivot = (r + l) / 2;
        }

        // If one of the partitions is empty, continue with recursion: same level but different bbox
        if (pivot < l) {
            // only right side
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);
            return createNode(l, r, rbbox, depth + 1);
        } else if (pivot > r) {
            // only left
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);
            return createNode(l, r, lbbox, depth + 1);
        } else {
            // Build the node
            BIHNode node = new BIHNode(axis);

            // Left child
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);

            // The left node right border is the plane most right
            node.setLeftPlane(getMinMax(createBox(l, max(l, pivot - 1)), false, axis));
            node.setLeftChild(createNode(l, max(l, pivot - 1), lbbox, depth + 1)); // Recursive call

            // Right child
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);

            // The right node left border is the plane most left
            node.setRightPlane(getMinMax(createBox(pivot, r), true, axis));
            node.setRightChild(createNode(pivot, r, rbbox, depth + 1));

            return node;
        }
    }

    private float getMinMax(BoundingBox boundingBox, boolean doMin, int axis) {
        if (doMin) {
            return boundingBox.getMin(null).get(axis);
        } else {
            return boundingBox.getMax(null).get(axis);
        }
    }

    private void setMinMax(BoundingBox boundingBox, boolean doMin, int axis, float value) {
        Vector3f min = boundingBox.getMin(null);
        Vector3f max = boundingBox.getMax(null);

        if (doMin) {
            min.set(axis, value);
        } else {
            max.set(axis, value);
        }

        boundingBox.setMinMax(min, max);
    }

    private int sortTriangles(int l, int r, float split, int axis) {
        int pivot = l;
        int j = r;

        TempVars vars = TempVars.get();

        Vector3f v1 = vars.vect1,
                v2 = vars.vect2,
                v3 = vars.vect3;

        while (pivot <= j) {
            getTriangle(pivot, v1, v2, v3);
            v1.addLocal(v2).addLocal(v3).multLocal(FastMath.ONE_THIRD);
            if (v1.get(axis) > split) {
                swapTriangles(pivot, j);
                --j;
            } else {
                ++pivot;
            }
        }

        vars.release();
        pivot = (pivot == l && j < pivot) ? j : pivot;
        return pivot;
    }

    private void swapTriangles(int index1, int index2) {
        int p1 = index1 * 9;
        int p2 = index2 * 9;

        // temp store p1
        System.arraycopy(pointData, p1, bihSwapArray, 0, 9);

        // copy p2 to p1
        System.arraycopy(pointData, p2, pointData, p1, 9);

        // copy temp to p2
        System.arraycopy(bihSwapArray, 0, pointData, p2, 9);

        // swap indices
        int tmp2 = triIndices[index1];
        triIndices[index1] = triIndices[index2];
        triIndices[index2] = tmp2;
    }

    private BoundingBox createBox(int l, int r) {
        TempVars vars = TempVars.get();

        Vector3f min = vars.vect1.set(new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                                                   Float.POSITIVE_INFINITY));
        Vector3f max = vars.vect2.set(new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
                                                   Float.NEGATIVE_INFINITY));

        Vector3f v1 = vars.vect3,
                v2 = vars.vect4,
                v3 = vars.vect5;

        for (int i = 0; i < r; i++) {
            getTriangle(i, v1, v2, v3);
            BoundingBox.checkMinMax(min, max, v1);
            BoundingBox.checkMinMax(min, max, v2);
            BoundingBox.checkMinMax(min, max, v3);
        }

        BoundingBox bbox = new BoundingBox(min, max);
        vars.release();
        return bbox;
    }

    public void getTriangle(int index, Vector3f v1, Vector3f v2, Vector3f v3) {
        int pointIndex = index * 3;

        v1.x = pointData[pointIndex++];
        v1.y = pointData[pointIndex++];
        v1.z = pointData[pointIndex++];

        v2.x = pointData[pointIndex++];
        v2.y = pointData[pointIndex++];
        v2.z = pointData[pointIndex++];

        v3.x = pointData[pointIndex++];
        v3.y = pointData[pointIndex++];
        v3.z = pointData[pointIndex];
    }

    public int getTriangleIndex(int triIndex) {
        return triIndices[triIndex];
    }
}
