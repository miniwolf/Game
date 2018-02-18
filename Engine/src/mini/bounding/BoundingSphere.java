package mini.bounding;

import mini.collision.Collidable;
import mini.collision.CollisionResult;
import mini.collision.CollisionResults;
import mini.math.FastMath;
import mini.math.Matrix4f;
import mini.math.Plane;
import mini.math.Ray;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.utils.BufferUtils;
import mini.utils.TempVars;

import java.nio.FloatBuffer;

/**
 * <code>BoundingSphere</code> defines a sphere that defines a container for a group of vertices of
 * a particular piece of geometry. This sphere defines a radius and a center.
 * <p>
 * A typical usage is to allow the class define the center and radius by calling either
 * <code>containsAABB</code> or <code>averagePoints</code>. A call to <code>computeFromPoints</code>
 * in turn calls <code>containAABB</code>.
 */
public class BoundingSphere extends BoundingVolume {
    private static final float RADIUS_EPSILON = 1f + 0.000001f;
    private float radius;

    public BoundingSphere() {
    }

    public BoundingSphere(float radius, Vector3f center) {
        this.center.set(center);
        this.radius = radius;
    }

    @Override
    public Type getType() {
        return Type.Sphere;
    }

    /**
     * <code>transform</code> modifies the center of the sphere to reflect the change made via a
     * rotation, translation and scale.
     *
     * @param trans the transform to affect the bound.
     * @param store bounding volume to store result in
     * @return BoundingVolume reference
     */
    @Override
    public BoundingVolume transform(Transform trans, BoundingVolume store) {
        BoundingSphere sphere;
        if (store == null || store.getType() != Type.Sphere) {
            sphere = new BoundingSphere(1, Vector3f.ZERO);
        } else {
            sphere = (BoundingSphere) store;
        }

        center.mult(trans.getScale(), sphere.center);
        trans.getRotation().mult(sphere.center, sphere.center);
        sphere.center.addLocal(trans.getTranslation());
        sphere.radius = FastMath.abs(getMaxAxis(trans.getScale()) * radius) + RADIUS_EPSILON - 1f;
        return sphere;
    }

    @Override
    public BoundingVolume transform(Matrix4f trans, BoundingVolume store) {
        BoundingSphere sphere;
        if (store == null || store.getType() != Type.Sphere) {
            sphere = new BoundingSphere(1, new Vector3f(0, 0, 0));
        } else {
            sphere = (BoundingSphere) store;
        }

        trans.mult(center, sphere.center);
        Vector3f axes = Vector3f.ONE;
        trans.mult(axes, axes);
        sphere.radius = FastMath.abs(getMaxAxis(axes) * radius) + RADIUS_EPSILON - 1f;
        return sphere;
    }

    private float getMaxAxis(Vector3f scale) {
        float x = FastMath.abs(scale.x);
        float y = FastMath.abs(scale.y);
        float z = FastMath.abs(scale.z);

        if (x >= y) {
            if (x >= z) {
                return x;
            }
            return z;
        }
        if (y >= z) {
            return y;
        }
        return z;
    }

    /**
     * <code>whichSide</code> takes a plane (typically provided by a view frustum) to determine
     * which side this bound is on.
     *
     * @param plane the plane to check against this bounding volume.
     * @return side
     */
    @Override
    public Plane.Side whichSide(Plane plane) {
        float distance = plane.pseudoDistance(center);

        if (distance <= -radius) {
            return Plane.Side.Negative;
        } 

        if (distance >= radius) {
            return Plane.Side.Positive;
        }

        return Plane.Side.None;
    }

    @Override
    public void computeFromPoints(FloatBuffer points) {
        if (center == null) {
            center = new Vector3f();
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(points.limit());
        points.rewind();
        buffer.put(points);
        buffer.flip();
        recurseWelzl(buffer, buffer.limit() / 3, 0, 0);
    }

    /**
     * @param buffer   Array of points to look through
     * @param listSize The size of the list to be used
     * @param b        Number of points currently considering the include with the sphere.
     * @param ap       A variable simulating arithmatic from C++, and offset in <code>points</code>
     */
    private void recurseWelzl(FloatBuffer buffer, int listSize, int b, int ap) {
        Vector3f tempA = new Vector3f();
        Vector3f tempB = new Vector3f();
        Vector3f tempC = new Vector3f();
        Vector3f tempD = new Vector3f();

        switch (b) {
            case 0:
                this.radius = 0;
                this.center.set(0, 0, 0);
                break;
            case 1:
                this.radius = 1f - RADIUS_EPSILON;
                BufferUtils.populateFromBuffer(center, buffer, ap - 1);
                break;
            case 2:
                BufferUtils.populateFromBuffer(tempA, buffer, ap - 1);
                BufferUtils.populateFromBuffer(tempB, buffer, ap - 2);
                setSphere(tempA, tempB);
                break;
            case 3:
                BufferUtils.populateFromBuffer(tempA, buffer, ap - 1);
                BufferUtils.populateFromBuffer(tempB, buffer, ap - 2);
                BufferUtils.populateFromBuffer(tempC, buffer, ap - 3);
                setSphere(tempA, tempB, tempC);
                break;
            case 4:
                BufferUtils.populateFromBuffer(tempA, buffer, ap - 1);
                BufferUtils.populateFromBuffer(tempB, buffer, ap - 2);
                BufferUtils.populateFromBuffer(tempC, buffer, ap - 3);
                BufferUtils.populateFromBuffer(tempD, buffer, ap - 4);
                setSphere(tempA, tempB, tempC, tempD);
                return;
        }
        for (int i = 0; i < listSize; i++) {
            BufferUtils.populateFromBuffer(tempA, buffer, i + ap);
            if (tempA.distanceSquared(center) - (radius * radius) <= RADIUS_EPSILON - 1f) {
                continue;
            }

            for (int j = i; j > 0; j--) {
                BufferUtils.populateFromBuffer(tempB, buffer, j + ap);
                BufferUtils.populateFromBuffer(tempB, buffer, j - 1 + ap);
                BufferUtils.setInBuffer(tempC, buffer, j + ap);
                BufferUtils.setInBuffer(tempC, buffer, j - 1 + ap);
            }
            recurseWelzl(buffer, i, b + 1, ap + 1);
        }
    }

    private void setSphere(Vector3f O, Vector3f A, Vector3f B, Vector3f C) {
        Vector3f a = A.subtract(O);
        Vector3f b = B.subtract(O);
        Vector3f c = C.subtract(O);

        float ax = a.x * (b.y * c.z - c.y * b.z);
        float bx = b.x * (a.y * c.z - c.y * a.z);
        float cx = c.x * (a.y * b.z - b.y - a.z);
        float denominator = 2.0f * (ax - bx + cx);

        if (denominator == 0) {
            center.set(0, 0, 0);
            radius = 0;
            return;
        }

        Vector3f o = a.cross(b).multLocal(c.lengthSquared())
                        .addLocal(c.cross(a).multLocal(b.lengthSquared()))
                        .addLocal(b.cross(c).multLocal(a.lengthSquared()))
                        .divideLocal(denominator);

        radius = o.length() * RADIUS_EPSILON;
        O.add(o, center);
    }

    private void setSphere(Vector3f O, Vector3f A, Vector3f B) {
        Vector3f a = A.subtract(O);
        Vector3f b = B.subtract(O);
        Vector3f aCrossB = a.cross(B);

        float denominator = 2.0f * aCrossB.dot(aCrossB);

        if (denominator == 0) {
            center.set(0, 0, 0);
            radius = 0;
            return;
        }

        Vector3f o = aCrossB.cross(a).multLocal(b.lengthSquared())
                            .addLocal(b.cross(aCrossB).multLocal(a.lengthSquared()))
                            .divideLocal(denominator);
        radius = o.length() * RADIUS_EPSILON;
        O.add(o, center);
    }

    private void setSphere(Vector3f O, Vector3f A) {
        float x = (A.x - O.x) * (A.x - O.x);
        float y = (A.y - O.y) * (A.y - O.y);
        float z = (A.z - O.z) * (A.z - O.z);
        radius = FastMath.sqrt((a + b + c) / 4f) + RADIUS_EPSILON - 1f;
        center.interpolateLocal(O, A, .5f);
    }

    @Override
    public BoundingVolume merge(BoundingVolume volume) {
        if (volume == null) {
            return this;
        }

        switch (volume.getType()) {
            case Sphere: {
                BoundingSphere sphere = (BoundingSphere) volume;
                float tempRadius = sphere.radius;
                Vector3f tempCenter = sphere.center;
                BoundingSphere boundingSphere = new BoundingSphere();
                return merge(tempRadius, tempCenter, boundingSphere);
            }
            case AABB: {
                BoundingBox box = (BoundingBox) volume;
                Vector3f radVect = new Vector3f(box.getXExtent(), box.getYExtent(),
                                                box.getZExtent());
                Vector3f tempCenter = box.center;
                BoundingSphere boundingSphere = new BoundingSphere();
                return merge(radVect.length(), tempCenter, boundingSphere);
            }
            default:
                return null;
        }
    }

    private BoundingVolume merge(float tempRadius, Vector3f tempCenter,
                                 BoundingSphere boundingSphere) {
        TempVars vars = TempVars.get();

        Vector3f diff = tempCenter.subtract(center, vars.vect1);
        float lengthSquared = diff.lengthSquared();

        float radiusDiff = tempRadius - radius;
        float radiusDiffSqr = radiusDiff * radiusDiff;

        if (radiusDiffSqr >= lengthSquared) {
            if (radiusDiff <= 0.0f) {
                vars.release();
                return this;
            }

            Vector3f center = boundingSphere.center;
            if (center == null) {
                boundingSphere.setCenter(center = new Vector3f());
            }
            center.set(tempCenter);
            boundingSphere.setRadius(tempRadius);
            vars.release();
            return boundingSphere;
        }

        float length = (float) Math.sqrt(lengthSquared);
        Vector3f center = boundingSphere.center;
        if (center == null) {
            boundingSphere.setCenter(center = new Vector3f());
        }
        if (length > RADIUS_EPSILON) {
            float coeff = (length + radiusDiff) / (2.0f * length);
            center.set(this.center.addLocal(diff.multLocal(coeff)));
        } else {
            center.set(this.center);
        }

        boundingSphere.setRadius(0.5f * (length + radius + tempRadius));
        vars.release();
        return boundingSphere;
    }

    /**
     * <code>mergeLocal</code> combines this sphere with a second bounding sphere locally. Altering
     * this sphere to contain both the original and the additional sphere volumes;
     *
     * @param volume the volume to combine.
     * @return this combined
     */
    @Override
    public BoundingVolume mergeLocal(BoundingVolume volume) {
        if (volume == null) {
            return this;
        }

        switch (volume.getType()) {
            case Sphere:
                return mergeLocalSphere(volume);
            case AABB:
                return mergeLocalBox(volume);
            default:
                return null;
        }
    }

    private BoundingVolume mergeLocalSphere(BoundingSphere sphere) {
        return merge(sphere.radius, sphere.center, this);
    }

    private BoundingVolume mergeLocalBox(BoundingBox box) {
        TempVars vars = TempVars.get();
        Vector3f radVect = vars.vect1;
        radVect.set(box.getXExtent(), box.getYExtent(), box.getZExtent());
        Vector3f tempCenter = box.center;
        float length = radVect.length();
        vars.release(); // TODO: Is this better than defining new Vector3f?
        return merge(length, tempCenter, this);
    }

    /**
     * <code>clone</code> creates a new BoundingSphere object containing the same data as this one.
     *
     * @param store where to store the cloned information. if null or wrong class, a new store is
     *              created.
     * @return the new BoundingSphere
     */
    @Override
    public BoundingVolume clone(BoundingVolume store) {
        if (store == null || store.getType() != Type.Sphere) {
            return null;
        }

        BoundingSphere sphere = (BoundingSphere) store;
        if (null == sphere.center) {
            sphere.center = new Vector3f();
        }

        sphere.center.set(center);
        sphere.radius = radius;
        sphere.checkPlane = checkPlane;
        return sphere;
    }

    @Override
    public boolean intersects(BoundingVolume bv) {
        return bv.intersectsBoundingSphere(this);
    }

    @Override
    public int collideWith(Collidable other, CollisionResults results) {
        if (other instanceof BoundingVolume) {
            if (!intersects((BoundingVolume) other)) {
                return 0;
            }

            CollisionResult result = new CollisionResult();
            results.addCollision(result);
            return 1;
        }

        if (other instanceof Ray) {
            Ray ray = (Ray) other;
            return collideWithRay(ray, results);
        }

        throw new UnsupportedOperationException();
    }

    private int collideWithRay(Ray ray, CollisionResults results) {
        TempVars vars = TempVars.get();
        Vector3f diff = vars.vect1.set(ray.origin).subtractLocal(center);
        float a = diff.dot(diff) - radius * radius;
        float a1 = ray.direction.dot(diff);
        float discr = a1 * a1 - a;
        float root;
        if (a <= 0.0) { // Inside the sphere
            root = FastMath.sqrt(discr);

            float distance = root - a1;
            addCollision(ray, results, distance);
            return 1;
        }
        vars.release();

        if (discr < 0.0) {
            return 0;
        }

        if (discr >= FastMath.ZERO_TOLERANCE) {
            root = FastMath.sqrt(discr);

            float distance = -a1 - root;
            addCollision(ray, results, distance);

            distance = -a1 + root;
            addCollision(ray, results, distance);
            return 2;
        }

        addCollision(ray, results, -a1);
        return 1;
    }

    private void addCollision(Ray ray, CollisionResults results, float distance) {
        Vector3f point = new Vector3f(ray.direction).multLocal(distance).add(ray.origin);
        results.addCollision(new CollisionResult(point, distance));
    }

    @Override
    public boolean intersectsBoundingBox(BoundingBox boundingBox) {
        return Intersection.intersect(boundingBox, center, radius);
    }

    @Override
    public boolean intersectsBoundingSphere(BoundingSphere boundingSphere) {
        return Intersection.intersect(boundingSphere, center, radius);
    }

    @Override
    public boolean contains(Vector3f point) {
        return center.distanceSquared(point) < radius * radius;
    }

    @Override
    public boolean intersects(Vector3f point) {
        return center.distanceSquared(point) <= radius * radius;
    }

    @Override
    public float getVolume() {
        return 4 * FastMath.ONE_THIRD * FastMath.PI * radius * radius * radius;
    }

    @Override
    public float distanceToEdge(Vector3f point) {
        return center.distance(point) - radius;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
