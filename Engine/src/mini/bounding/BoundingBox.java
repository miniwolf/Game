package mini.bounding;

import mini.collision.Collidable;
import mini.collision.CollisionResult;
import mini.collision.CollisionResults;
import mini.math.FastMath;
import mini.math.Matrix3f;
import mini.math.Matrix4f;
import mini.math.Plane;
import mini.math.Ray;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.utils.TempVars;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.FloatBuffer;

/**
 * <code>BoundingBox</code> describes a bounding volume as an axis-aligned bounding box.
 * <br>
 * Instances may be initialized by invoking the <code>containAABB</code> method.
 */
public class BoundingBox extends BoundingVolume {
    /**
     * the extents of the box (>=0, may be +Infinity)
     */
    private float xExtent;
    private float yExtent;
    private float zExtent;

    public BoundingBox() {
    }

    public BoundingBox(Vector3f center, float x, float y, float z) {
        this.center.set(center);
        xExtent = x;
        yExtent = y;
        zExtent = z;
    }

    public BoundingBox(Vector3f min, Vector3f max) {
        setMinMax(min, max);
    }

    /**
     * Instantiate a <code>BoundingBox</code> equivilant to an existing box.
     *
     * @param source the existing box (not null, not altered)
     */
    public BoundingBox(BoundingBox source) {
        this.center.set(source.center);
        this.xExtent = source.xExtent;
        this.yExtent = source.yExtent;
        this.zExtent = source.zExtent;
    }

    public static void checkMinMax(Vector3f min, Vector3f max, Vector3f point) {
        if (point.x < min.x) {
            min.x = point.x;
        }
        if (point.x > max.x) {
            max.x = point.x;
        }
        if (point.y < min.y) {
            min.y = point.y;
        }
        if (point.y > max.y) {
            max.y = point.y;
        }
        if (point.z < min.z) {
            min.z = point.z;
        }
        if (point.z > max.z) {
            max.z = point.z;
        }
    }

    public void setMinMax(Vector3f min, Vector3f max) {
        this.center.set(max).addLocal(min).multLocal(0.5f);

        xExtent = FastMath.abs(max.x - center.x);
        yExtent = FastMath.abs(max.y - center.y);
        zExtent = FastMath.abs(max.z - center.z);
    }

    @Override
    public Type getType() {
        return Type.AABB;
    }

    @Override
    /**
     * <code>transform</code> modifies the center of the box to reflect the change made via a
     * rotation, translation and scale
     */
    public BoundingVolume transform(Transform trans, BoundingVolume store) {
        BoundingBox box;
        if (store == null || store.getType() != Type.AABB) {
            box = new BoundingBox();
        } else {
            box = (BoundingBox) store;
        }

        center.mult(trans.getScale(), box.center);
        trans.getRotation().mult(box.center, box.center);
        box.center.addLocal(trans.getTranslation());

        TempVars vars = TempVars.get();

        Matrix3f transformationMatrix = vars.tempMat3;
        transformationMatrix.set(trans.getRotation());
        // Make the rotation matrix all positive to get the maximum x/y/z extent
        transformationMatrix.absoluteLocal();

        Vector3f scale = trans.getScale();
        vars.vect1.set(xExtent * FastMath.abs(scale.x), yExtent * FastMath.abs(scale.y),
                       zExtent * FastMath.abs(scale.z));
        transformationMatrix.mult(vars.vect1, vars.vect2);
        // Assign the biggest rotations after scales.
        box.xExtent = FastMath.abs(vars.vect2.getX());
        box.yExtent = FastMath.abs(vars.vect2.getY());
        box.zExtent = FastMath.abs(vars.vect2.getZ());

        vars.release();

        return box;
    }

    @Override
    public BoundingVolume transform(Matrix4f trans, BoundingVolume store) {
        BoundingBox box;
        if (store == null || store.getType() != Type.AABB) {
            box = new BoundingBox();
        } else {
            box = (BoundingBox) store;
        }
        TempVars vars = TempVars.get();

        float w = trans.multProj(center, box.center);
        box.center.divideLocal(w);

        Matrix3f transformationMatrix = vars.tempMat3;
        trans.toRotationMatrix(transformationMatrix);

        // Make the rotation matrix all positive to get the maximum x/y/z extent
        transformationMatrix.absoluteLocal();

        vars.vect1.set(xExtent, yExtent, zExtent);
        transformationMatrix.mult(vars.vect1, vars.vect1);

        // Assign the biggest rotations after scales.
        box.xExtent = FastMath.abs(vars.vect1.getX());
        box.yExtent = FastMath.abs(vars.vect1.getY());
        box.zExtent = FastMath.abs(vars.vect1.getZ());

        vars.release();

        return box;
    }

    @Override
    /**
     * <code>whichSide</code> takes a plane (typically provided bya view frustum) to determine which
     * side this bound is on.
     *
     * @param plane the plane to check against.
     */
    public Plane.Side whichSide(Plane plane) {
        float radius = FastMath.abs(xExtent * plane.getNormal().getX())
                       + FastMath.abs(yExtent * plane.getNormal().getY())
                       + FastMath.abs(zExtent * plane.getNormal().getZ());

        float distance = plane.pseudoDistance(center);

        if (distance < -radius) {
            return Plane.Side.Negative;
        } else if (distance > radius) {
            return Plane.Side.Positive;
        } else {
            return Plane.Side.None;
        }
    }

    @Override
    /**
     * creates a minimum-volume axis-aligned bounding box of the points, then selects the smallest
     * enclosing sphere of the box with the sphere centered at the boxes center.
     */
    public void computeFromPoints(FloatBuffer points) {
        if (points == null) {
            return;
        }

        points.rewind();
        if (points.remaining() < 3) { // We need at least a 3 float vector
            return;
        }

        TempVars vars = TempVars.get();

        float[] tmpArray = vars.skinPositions;

        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ
                = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ
                = Float.NEGATIVE_INFINITY;

        int iterations = (int) FastMath.ceil(points.limit() / ((float) tmpArray.length));
        // TODO: Parallel iterations through the array could get all 6 values. Using streaming on the array
        // Array should be copied from the float buffer
        for (int i = 0; i < iterations; i++) {
            int bufferLength = Math.min(tmpArray.length, points.remaining());
            points.get(tmpArray, 0, bufferLength);

            for (int j = 0; j < bufferLength; j += 3) {
                vars.vect1.x = tmpArray[j];
                vars.vect1.y = tmpArray[j + 1];
                vars.vect1.z = tmpArray[j + 2];

                if (vars.vect1.x < minX) {
                    minX = vars.vect1.x;
                }
                if (vars.vect1.x > maxX) {
                    maxX = vars.vect1.x;
                }

                if (vars.vect1.y < minY) {
                    minY = vars.vect1.y;
                }
                if (vars.vect1.y > maxY) {
                    maxY = vars.vect1.y;
                }

                if (vars.vect1.z < minZ) {
                    minZ = vars.vect1.z;
                }
                if (vars.vect1.z > maxZ) {
                    maxZ = vars.vect1.z;
                }
            }
        }

        vars.release();

        center.set(minX + maxX, minY + maxY, minZ + maxZ);
        center.multLocal(0.5f);

        xExtent = maxX - center.x;
        yExtent = maxY - center.y;
        zExtent = maxZ - center.z;
    }

    @Override
    public BoundingVolume merge(BoundingVolume volume) {
        throw new NotImplementedException();
    }

    @Override
    /**
     * <code>mergeLocal</code> combines this bounding box locally with a second bounding volume. The
     * result contains both the original box and the second volume.
     *
     * @param volume the bounding volume to combine this box (or null) (not altered)
     * @return this box (with its components modified) or null if the second volume is of some type
     * other than AABB or Sphere
     */
    public BoundingVolume mergeLocal(BoundingVolume volume) {
        if (volume == null) {
            return this;
        }

        switch (volume.getType()) {
            case AABB:
                BoundingBox box = (BoundingBox) volume;
                return mergeLocal(box.center, box.xExtent, box.yExtent, box.zExtent);
            case Sphere:
                throw new NotImplementedException();
            default:
                return null;
        }
    }

    /**
     * <code>mergeLocal</code> combines this bounding box locally with a second bounding box
     * described by its center and extents.
     *
     * @param center  the center of the second box (not null, not altered)
     * @param xExtent the X-extent of the second box
     * @param yExtent the Y-extent of the second box
     * @param zExtent the Z-extent of the second box
     * @return the resulting merged box
     */
    private BoundingVolume mergeLocal(Vector3f center, float xExtent, float yExtent,
                                      float zExtent) {
        if (this.xExtent == Float.POSITIVE_INFINITY || xExtent == Float.POSITIVE_INFINITY) {
            this.center.x = 0;
            this.xExtent = Float.POSITIVE_INFINITY;
        } else {
            float low = this.center.x - this.xExtent;
            if (low > center.x - xExtent) {
                low = center.x - xExtent;
            }
            float high = this.center.x + this.xExtent;
            if (high < center.x + xExtent) {
                high = center.x + xExtent;
            }
            this.center.x = (low + high) / 2;
            this.xExtent = high - this.center.x;
        }

        if (this.yExtent == Float.POSITIVE_INFINITY || yExtent == Float.POSITIVE_INFINITY) {
            this.center.y = 0;
            this.yExtent = Float.POSITIVE_INFINITY;
        } else {
            float low = this.center.y - this.yExtent;
            if (low > center.y - yExtent) {
                low = center.y - yExtent;
            }
            float high = this.center.y + this.yExtent;
            if (high < center.y + yExtent) {
                high = center.y + yExtent;
            }
            this.center.y = (low + high) / 2;
            this.yExtent = high - this.center.y;
        }

        if (this.zExtent == Float.POSITIVE_INFINITY || zExtent == Float.POSITIVE_INFINITY) {
            this.center.z = 0;
            this.zExtent = Float.POSITIVE_INFINITY;
        } else {
            float low = this.center.z - this.zExtent;
            if (low > center.z - zExtent) {
                low = center.z - zExtent;
            }
            float high = this.center.z + this.zExtent;
            if (high < center.z + zExtent) {
                high = center.z + zExtent;
            }
            this.center.z = (low + high) / 2;
            this.zExtent = high - this.center.z;
        }

        return this;
    }

    @Override
    public float distanceToEdge(Vector3f point) {
        throw new NotImplementedException();
    }

    @Override
    /**
     * determines if this Bounding Box intersects with another given bounding volume.
     *
     * @return If this bounding box intersects with the bounding volume
     */
    public boolean intersects(BoundingVolume bv) {
        return bv.intersectsBoundingBox(this);
    }

    @Override
    public boolean intersectsBoundingSphere(BoundingSphere boundingSphere) {
        throw new UnsupportedOperationException();
    }

    @Override
    /**
     * determines if this bounding box interesects a given bounding box.
     *
     * @return Whether the boxes intersects in any way.
     * @see BoundingVolume#intersectsBoundingBox(BoundingBox)
     */
    public boolean intersectsBoundingBox(BoundingBox boundingBox) {
        assert Vector3f.isValidVector(center) && Vector3f.isValidVector(boundingBox.center);

        if (center.x + xExtent < boundingBox.center.x - boundingBox.xExtent
            || center.x - xExtent > boundingBox.center.x + boundingBox.xExtent) {
            return false;
        }
        if (center.y + yExtent < boundingBox.center.y - boundingBox.yExtent
            || center.y - yExtent > boundingBox.center.y + boundingBox.yExtent) {
            return false;
        }
        return !(center.z + zExtent < boundingBox.center.z - boundingBox.zExtent)
               && !(center.z - zExtent > boundingBox.center.z + boundingBox.zExtent);
    }

    @Override
    public boolean contains(Vector3f point) {
        throw new NotImplementedException();
    }

    @Override
    public boolean intersects(Vector3f point) {
        throw new NotImplementedException();
    }

    @Override
    public float getVolume() {
        throw new NotImplementedException();
    }

    @Override
    public int collideWith(Collidable other, CollisionResults results) {
        if (other instanceof Ray) {
            Ray ray = (Ray) other;
            return collideWithRay(ray, results);
        } else {
            throw new UnsupportedOperationException("With: " + other.getClass().getSimpleName());
        }
    }

    private int collideWithRay(Ray ray, CollisionResults results) {
        TempVars vars = TempVars.get();

        Vector3f diff = vars.vect1.set(ray.origin).subtractLocal(center);
        Vector3f direction = vars.vect2.set(ray.direction);

        float[] t = vars.fWdU; // use one the tempvars arrays
        t[0] = 0;
        t[1] = Float.POSITIVE_INFINITY;

        float saveT0 = t[0], saveT1 = t[1];
        boolean notEntirelyClipped = clip(+direction.x, -diff.x - xExtent, t)
                                     && clip(-direction.x, +diff.x - xExtent, t)
                                     && clip(+direction.y, -diff.y - yExtent, t)
                                     && clip(-direction.y, +diff.y - yExtent, t)
                                     && clip(+direction.z, -diff.z - zExtent, t)
                                     && clip(-direction.z, +diff.z - zExtent, t);

        if (notEntirelyClipped && (t[0] != saveT0 || t[1] != saveT1)) {
            if (t[1] > t[0]) {
                float[] distances = t;
                Vector3f point0 = new Vector3f(ray.direction).multLocal(distances[0])
                                                             .addLocal(ray.origin);
                Vector3f point1 = new Vector3f(ray.direction).multLocal(distances[1])
                                                             .addLocal(ray.origin);

                CollisionResult result = new CollisionResult(point0, distances[0]);
                results.addCollision(result);
                result = new CollisionResult(point1, distances[1]);
                results.addCollision(result);
                vars.release();
                return 2;
            }

            Vector3f point = new Vector3f(ray.direction).multLocal(t[0]).addLocal(ray.origin);
            CollisionResult result = new CollisionResult(point, t[0]);
            results.addCollision(result);
            vars.release();
            return 1;

        }

        vars.release();
        return 0;
    }

    /**
     * <code>clip</code> determines if a line segment intersects with the current test plane.
     *
     * @param denominator the denominator of the line segment
     * @param numerator   the numerator of the line segment
     * @param t           test values of the plane
     * @return Whether the segment intersects the plane
     */
    private boolean clip(float denominator, float numerator, float t[]) {
        if (denominator > 0.0f) {
            float newT = numerator / denominator;
            if (newT > t[1]) {
                return false;
            }

            if (newT > t[0]) {
                t[0] = newT;
            }
            return true;
        } else if (denominator < 0.0f) {
            float newT = numerator / denominator;
            if (newT < t[0]) {
                return false;
            }

            if (newT < t[1]) {
                t[1] = newT;
            }
            return true;
        } else {
            return numerator <= 0.0f;
        }
    }

    /**
     * Query extent
     *
     * @param store where extent gets stored - null to return a new vector
     * @return store / new vector
     */
    public Vector3f getExtent(Vector3f store) {
        if (store == null) {
            store = new Vector3f();
        }
        store.set(xExtent, yExtent, zExtent);
        return store;
    }

    public Vector3f getMin(Vector3f store) {
        if (store == null) {
            store = new Vector3f();
        }
        store.set(center).subtractLocal(xExtent, yExtent, zExtent);
        return store;
    }

    public Vector3f getMax(Vector3f store) {
        if (store == null) {
            store = new Vector3f();
        }
        store.set(center).addLocal(xExtent, yExtent, zExtent);
        return store;
    }

    @Override
    /**
     * <code>clone</code> creates a new BoundingBox object containing the same data as this one.
     *
     * @param store where to store the cloned information. If this is null or the wrong class, a new
     *              store is created.
     * @return the new BoundingBox
     */
    public BoundingVolume clone(BoundingVolume store) {
        if (store != null && store.getType() == Type.AABB) {
            BoundingBox rVal = (BoundingBox) store;
            rVal.center.set(center);
            rVal.xExtent = xExtent;
            rVal.yExtent = yExtent;
            rVal.zExtent = zExtent;
            rVal.checkPlane = checkPlane;
            return rVal;
        }

        return new BoundingBox(center.clone(), xExtent, yExtent, zExtent);
    }

    public float getZExtent() {
        return zExtent;
    }

    public void setZExtent(float zExtent) {
        if (zExtent < 0) {
            throw new IllegalArgumentException("zExtent has to be <= 0");
        }
        this.zExtent = zExtent;
    }

    public float getXExtent() {
        return xExtent;
    }

    public void setXExtent(float xExtent) {
        if (xExtent < 0) {
            throw new IllegalArgumentException("zExtent has to be <= 0");
        }
        this.xExtent = xExtent;
    }

    public float getYExtent() {
        return yExtent;
    }

    public void setYExtent(float yExtent) {
        if (yExtent < 0) {
            throw new IllegalArgumentException("zExtent has to be <= 0");
        }
        this.yExtent = yExtent;
    }
}
