package mini.utils;

import mini.bounding.BoundingBox;
import mini.collision.CollisionResults;
import mini.collision.bih.BIHNode;
import mini.math.ColorRGBA;
import mini.math.Matrix3f;
import mini.math.Matrix4f;
import mini.math.Plane;
import mini.math.Quaternion;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.scene.Spatial;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Temporary variables assigned to each thread. Engine classes may access
 * these temp variables with TempVars.get(), all retrieved TempVars
 * instances must be returned via TempVars.release().
 * This returns an available instance of the TempVar class ensuring this
 * particular instance is never used elsewhere in the mean time.
 */
public class TempVars {

    /**
     * Allow X instances of TempVars in a single thread.
     */
    private static final int STACK_SIZE = 5;

    /**
     * <code>TempVarsStack</code> contains a stack of TempVars.
     * Every time TempVars.get() is called, a new entry is added to the stack,
     * and the index incremented.
     * When TempVars.release() is called, the entry is checked against
     * the current instance and  then the index is decremented.
     */
    private static class TempVarsStack {
        int index = 0;
        TempVars[] tempVars = new TempVars[STACK_SIZE];
    }

    /**
     * ThreadLocal to store a TempVarsStack for each thread.
     * This ensures each thread has a single TempVarsStack that is
     * used only in method calls in that thread.
     */
    private static final ThreadLocal<TempVarsStack> varsLocal = ThreadLocal
            .withInitial(TempVarsStack::new);

    /**
     * This instance of TempVars has been retrieved but not released yet.
     */
    private boolean isUsed = false;

    /**
     * Used for serialization.
     */
    private TempVars() {
    }

    /**
     * Acquire an instance of the TempVar class.
     * You have to release the instance after use by calling the release() method.
     * If more than STACK_SIZE (currently 5) instances are requested in a single thread then an
     * ArrayIndexOutOfBoundsException will be thrown.
     *
     * @return A TempVar instance
     */
    public static TempVars get() {
        TempVarsStack stack = varsLocal.get();

        TempVars instance = stack.tempVars[stack.index];

        if (instance == null) {
            // Create new
            instance = new TempVars();

            // Put it in there
            stack.tempVars[stack.index] = instance;
        }

        stack.index++;

        instance.isUsed = true;

        return instance;
    }

    /**
     * Releases this instance of TempVars.
     * Once released, the contents of the TempVars are undefined.
     * The TempVars must be released in the opposite order that they are retrieved,
     * e.g. Acquiring vars1, then acquiring vars2, vars2 MUST be released
     * first otherwise an exception will be thrown.
     */
    public void release() {
        if (!isUsed) {
            throw new IllegalStateException("This instance of TempVars was already released!");
        }

        isUsed = false;

        TempVarsStack stack = varsLocal.get();

        // Return it to the stack
        stack.index--;

        // Check if it is actually there
        if (stack.tempVars[stack.index] != this) {
            throw new IllegalStateException(
                    "An instance of TempVars has not been released in a called method!");
        }
    }

    /**
     * For interfacing with OpenGL in Renderer.
     */
    public final IntBuffer intBuffer1 = BufferUtils.createIntBuffer(1);
    public final IntBuffer intBuffer16 = BufferUtils.createIntBuffer(16);
    public final FloatBuffer floatBuffer16 = BufferUtils.createFloatBuffer(16);
    /**
     * BoundingVolumes (for shadows etc.)
     */
    public final BoundingBox bbox = new BoundingBox();
    /**
     * Skinning buffers
     */
    public final float[] skinPositions = new float[512 * 3];
    public final float[] skinNormals = new float[512 * 3];
    //tangent buffer as 4 components by elements
    public final float[] skinTangents = new float[512 * 4];
//    /**
//     * Fetching triangle from mesh
//     */
//    public final Triangle triangle = new Triangle();
    /**
     * Color
     */
    public final ColorRGBA color = new ColorRGBA();
    /**
     * General vectors.
     */
    public final Vector3f vect1 = new Vector3f();
    public final Vector3f vect2 = new Vector3f();
    public final Vector3f vect3 = new Vector3f();
    public final Vector3f vect4 = new Vector3f();
    public final Vector3f vect5 = new Vector3f();
    public final Vector3f vect6 = new Vector3f();
    public final Vector3f vect7 = new Vector3f();
    //seems the maximum number of vector used is 7 in mini.bounding.java
    public final Vector3f vect8 = new Vector3f();
    public final Vector3f vect9 = new Vector3f();
    public final Vector3f vect10 = new Vector3f();
    public final Vector4f vect4f1 = new Vector4f();
    public final Vector4f vect4f2 = new Vector4f();
    public final Vector3f[] tri = {new Vector3f(),
                                   new Vector3f(),
                                   new Vector3f()};
    /**
     * 2D vector
     */
    public final Vector2f vect2d = new Vector2f();
    public final Vector2f vect2d2 = new Vector2f();
    /**
     * General matrices.
     */
    public final Matrix3f tempMat3 = new Matrix3f();
    public final Matrix4f tempMat4 = new Matrix4f();
    public final Matrix4f tempMat42 = new Matrix4f();
    /**
     * General quaternions.
     */
    public final Quaternion quat1 = new Quaternion();
    public final Quaternion quat2 = new Quaternion();
    /**
     * Plane
     */
    public final Plane plane = new Plane();
    /**
     * BoundingBox ray collision
     */
    public final float[] fWdU = new float[3];
    public final float[] fAWdU = new float[3];
    public final float[] fDdU = new float[3];
    public final float[] fADdU = new float[3];
    public final float[] fAWxDdU = new float[3];
    /**
     * Maximum tree depth .. 32 levels??
     */
    public final Spatial[] spatialStack = new Spatial[32];
    public final float[] matrixWrite = new float[16];
    /**
     * BIHTree
     */
    public final CollisionResults collisionResults = new CollisionResults();
    public final float[] bihSwapTmp = new float[9];
    public final List<BIHNode.BIHStackData> bihStack = new ArrayList<>();
}