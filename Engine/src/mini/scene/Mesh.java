package mini.scene;

import mini.bounding.BoundingBox;
import mini.bounding.BoundingVolume;
import mini.collision.Collidable;
import mini.collision.CollisionResults;
import mini.collision.bih.BIHTree;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.Matrix4f;
import mini.math.Vector2f;
import mini.scene.mesh.IndexBuffer;
import mini.utils.BufferUtils;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>Mesh</code> is used to store rendering data.
 * <p>
 * All visible elements in a scene are represented by meshes.
 * Meshes may contain three types of geometric primitives:
 * <ul>
 * <li>Points - Every vertex represents a single point in space.</li>
 * <li>Lines - 2 vertices represent a line segment, with the width specified
 * via {@link Material#getAdditionalRenderState()} and {@link RenderState#setLineWidth(float)}.</li>
 * <li>Triangles - 3 vertices represent a solid triangle primitive. </li>
 * </ul>
 */
public class Mesh implements Cloneable, MiniCloneable {
    /**
     * The mode of the Mesh specifies both the type of primitive represented
     * by the mesh and how the data should be interpreted.
     */
    public enum Mode {
        /**
         * A primitive is a single point in space. The size of {@link Mode#Points points} are
         * determined via the vertex shader's <code>gl_PointSize</code> output.
         */
        Points(true),

        /**
         * A primitive is a line segment. Every two vertices specify
         * a single line. {@link Material#getAdditionalRenderState()} and {@link RenderState#setLineWidth(float)} can be used
         * to set the width of the lines.
         */
        Lines(true),

        /**
         * A primitive is a line segment. The first two vertices specify
         * a single line, while subsequent vertices are combined with the
         * previous vertex to make a line. {@link Material#getAdditionalRenderState()} and {@link RenderState#setLineWidth(float)} can
         * be used to set the width of the lines.
         */
        LineStrip(false),

        /**
         * Identical to {@link #LineStrip} except that at the end
         * the last vertex is connected with the first to form a line.
         * {@link Material#getAdditionalRenderState()} and {@link RenderState#setLineWidth(float)} can be used
         * to set the width of the lines.
         */
        LineLoop(false),

        /**
         * A primitive is a triangle. Each 3 vertices specify a single
         * triangle.
         */
        Triangles(true),

        /**
         * Similar to {@link #Triangles}, the first 3 vertices
         * specify a triangle, while subsequent vertices are combined with
         * the previous two to form a triangle.
         */
        TriangleStrip(false),

        /**
         * Similar to {@link #Triangles}, the first 3 vertices
         * specify a triangle, each 2 subsequent vertices are combined
         * with the very first vertex to make a triangle.
         */
        TriangleFan(false),

        /**
         * A combination of various triangle modes. It is best to avoid
         * using this mode as it may not be supported by all renderers.
         * The {@link Mesh#setModeStart(int[]) mode start points} and
         * {@link Mesh#setElementLengths(int[]) element lengths} must
         * be specified for this mode.
         */
        Hybrid(false),
        /**
         * Used for Tesselation only. Requires to set the number of vertices
         * for each patch (default is 3 for triangle tesselation)
         */
        Patch(true);
        private boolean listMode = false;

        Mode(boolean listMode) {
            this.listMode = listMode;
        }

        /**
         * Returns true if the specified mode is a list mode (meaning
         * ,it specifies the indices as a linear list and not some special
         * format).
         * Will return true for the types {@link #Points}, {@link #Lines} and
         * {@link #Triangles}.
         *
         * @return true if the mode is a list type mode
         */
        public boolean isListMode() {
            return listMode;
        }
    }

    /**
     * The bounding volume that contains the mesh entirely. By default a BoundingBox (AABB)
     */
    private BoundingVolume meshBound = new BoundingBox();

    private CollisionData collisionTree = null;

    private List<VertexBuffer> buffersList = new ArrayList<>();
    private Map<Integer, VertexBuffer> buffers = new HashMap<>();
    private VertexBuffer[] lodLevels;
    private float pointSize = 1;
    private float lineWidth = 1;

    private transient int vertexArrayID = -1;

    private int vertCount = -1;
    private int elementCount = -1;
    private int instanceCount = -1;
    private int patchVertexCount = 3; //only used for tesselation
    private int maxNumWeights = -1; // only if using skeletal animation

    private int[] elementLengths;
    private int[] modeStart;

    private Mode mode = Mode.Triangles;

    /**
     * Creates a new mesh with no {@link VertexBuffer vertex buffers}.
     */
    public Mesh() {
    }

    /**
     * Create a shallow clone of this Mesh. The {@link VertexBuffer vertex
     * buffers} are shared between this and the clone mesh, the rest
     * of the data is cloned.
     *
     * @return A shallow clone of the mesh
     */
    @Override
    public Mesh clone() {
        try {
            Mesh clone = (Mesh) super.clone();
            clone.buffers = new HashMap<>(buffers);
            clone.buffersList = new ArrayList<>(buffersList);
            clone.vertexArrayID = -1;
            if (elementLengths != null) {
                clone.elementLengths = elementLengths.clone();
            }
            if (modeStart != null) {
                clone.modeStart = modeStart.clone();
            }
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public Mesh miniClone() {
        try {
            Mesh clone = (Mesh) super.clone();
            clone.vertexArrayID = -1;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        // Probably could clone this now but it will get regenerated anyway
        this.collisionTree = null;

        this.meshBound = cloner.clone(meshBound);
        this.buffersList = cloner.clone(buffersList);
        this.buffers = cloner.clone(buffers);
        this.lodLevels = cloner.clone(lodLevels);
        this.elementLengths = cloner.clone(elementLengths);
        this.modeStart = cloner.clone(modeStart);
    }

    /**
     * Set the LOD (level of detail) index buffers on this mesh.
     *
     * @param lodLevels The LOD levels to set
     */
    public void setLodLevels(VertexBuffer[] lodLevels) {
        this.lodLevels = lodLevels;
    }

    /**
     * @return The number of LOD levels set on this mesh, including the main
     * index buffer, returns zero if there are no lod levels.
     */
    public int getNumLodLevels() {
        return lodLevels != null ? lodLevels.length : 0;
    }

    /**
     * Returns the lod level at the given index.
     *
     * @param lod The lod level index, this does not include
     *            the main index buffer.
     * @return The LOD index buffer at the index
     * @throws IndexOutOfBoundsException If the index is outside of the
     *                                   range [0, {@link #getNumLodLevels()}].
     * @see #setLodLevels(mini.scene.VertexBuffer[])
     */
    public VertexBuffer getLodLevel(int lod) {
        return lodLevels[lod];
    }

    /**
     * Get the element lengths for {@link Mode#Hybrid} mesh mode.
     *
     * @return element lengths
     */
    public int[] getElementLengths() {
        return elementLengths;
    }

    /**
     * Set the element lengths for {@link Mode#Hybrid} mesh mode.
     *
     * @param elementLengths The element lengths to set
     */
    public void setElementLengths(int[] elementLengths) {
        this.elementLengths = elementLengths;
    }

    /**
     * Set the mode start indices for {@link Mode#Hybrid} mesh mode.
     *
     * @return mode start indices
     */
    public int[] getModeStart() {
        return modeStart;
    }

    /**
     * Get the mode start indices for {@link Mode#Hybrid} mesh mode.
     */
    public void setModeStart(int[] modeStart) {
        this.modeStart = modeStart;
    }

    /**
     * Returns the mesh mode
     *
     * @return the mesh mode
     * @see #setMode(mini.scene.Mesh.Mode)
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Change the Mesh's mode. By default the mode is {@link Mode#Triangles}.
     *
     * @param mode The new mode to set
     * @see Mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        updateCounts();
    }

    /**
     * Returns the maximum number of weights per vertex on this mesh.
     *
     * @return maximum number of weights per vertex
     * @see #setMaxNumWeights(int)
     */
    public int getMaxNumWeights() {
        return maxNumWeights;
    }

    /**
     * Set the maximum number of weights per vertex on this mesh.
     * Only relevant if this mesh has bone index/weight buffers.
     * This value should be between 0 and 4.
     *
     * @param maxNumWeights
     */
    public void setMaxNumWeights(int maxNumWeights) {
        this.maxNumWeights = maxNumWeights;
    }

    /**
     * Indicates to the GPU that this mesh will not be modified (a hint).
     * Sets the usage mode to {@link VertexBuffer.Usage#Static}
     * for all {@link VertexBuffer vertex buffers} on this Mesh.
     */
    public void setStatic() {
        for (VertexBuffer vb : buffersList) {
            vb.setUsage(VertexBuffer.Usage.Static);
        }
    }

    /**
     * Indicates to the GPU that this mesh will be modified occasionally (a hint).
     * Sets the usage mode to {@link VertexBuffer.Usage#Dynamic}
     * for all {@link VertexBuffer vertex buffers} on this Mesh.
     */
    public void setDynamic() {
        for (VertexBuffer vb : buffersList) {
            vb.setUsage(VertexBuffer.Usage.Dynamic);
        }
    }

    /**
     * Indicates to the GPU that this mesh will be modified every frame (a hint).
     * Sets the usage mode to {@link VertexBuffer.Usage#Stream}
     * for all {@link VertexBuffer vertex buffers} on this Mesh.
     */
    public void setStreamed() {
        for (VertexBuffer vb : buffersList) {
            vb.setUsage(VertexBuffer.Usage.Stream);
        }
    }

    private int computeNumElements(int bufSize) {
        switch (mode) {
            case Triangles:
                return bufSize / 3;
            case TriangleFan:
            case TriangleStrip:
                return bufSize - 2;
            case Points:
                return bufSize;
            case Lines:
                return bufSize / 2;
            case LineLoop:
                return bufSize;
            case LineStrip:
                return bufSize - 1;
            case Patch:
                return bufSize / patchVertexCount;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int computeInstanceCount() {
        // Whatever the max of the base instance counts
        int max = 0;
        for (VertexBuffer vb : buffersList) {
            if (vb.getBaseInstanceCount() > max) {
                max = vb.getBaseInstanceCount();
            }
        }
        return max;
    }

    /**
     * Update the {@link #getVertexCount() vertex} and
     * {@link #getTriangleCount() triangle} counts for this mesh
     * based on the current data. This method should be called
     * after the {@link Buffer#capacity() capacities} of the mesh's
     * {@link VertexBuffer vertex buffers} has been altered.
     */
    public void updateCounts() {
        if (getBuffer(VertexBuffer.Type.InterleavedData) != null) {
            throw new IllegalStateException("Should update counts before interleave");
        }

        VertexBuffer pb = getBuffer(VertexBuffer.Type.Position);
        VertexBuffer ib = getBuffer(VertexBuffer.Type.Index);
        if (pb != null) {
            vertCount = pb.getData().limit() / pb.getNumComponents();
        }
        if (ib != null) {
            elementCount = computeNumElements(ib.getData().limit());
        } else {
            elementCount = computeNumElements(vertCount);
        }
        instanceCount = computeInstanceCount();
    }

    /**
     * Returns the triangle count for the given LOD level.
     *
     * @param lod The lod level to look up
     * @return The triangle count for that LOD level
     */
    public int getTriangleCount(int lod) {
        if (lodLevels != null) {
            if (lod < 0) {
                throw new IllegalArgumentException("LOD level cannot be < 0");
            }

            if (lod >= lodLevels.length) {
                throw new IllegalArgumentException("LOD level " + lod + " does not exist!");
            }

            return computeNumElements(lodLevels[lod].getData().limit());
        } else if (lod == 0) {
            return elementCount;
        } else {
            throw new IllegalArgumentException("There are no LOD levels on the mesh!");
        }
    }

    /**
     * Returns how many triangles or elements are on this Mesh.
     * This value is only updated when {@link #updateCounts() } is called.
     * If the mesh mode is not a triangle mode, then this returns the
     * number of elements/primitives, e.g. how many lines or how many points,
     * instead of how many triangles.
     *
     * @return how many triangles/elements are on this Mesh.
     */
    public int getTriangleCount() {
        return elementCount;
    }

    /**
     * Returns the number of vertices on this mesh.
     * The value is computed based on the position buffer, which
     * must be set on all meshes.
     *
     * @return Number of vertices on the mesh
     */
    public int getVertexCount() {
        return vertCount;
    }

    /**
     * Returns the number of instances this mesh contains.  The instance
     * count is based on any VertexBuffers with instancing set.
     */
    public int getInstanceCount() {
        return instanceCount;
    }

    /**
     * Returns the mesh's VAO ID. Internal use only.
     */
    public int getId() {
        return vertexArrayID;
    }

    /**
     * Sets the mesh's VAO ID. Internal use only.
     */
    public void setId(int id) {
        if (vertexArrayID != -1) {
            throw new IllegalStateException("ID has already been set.");
        }

        vertexArrayID = id;
    }

    private void createCollisionData() {
        BIHTree tree = new BIHTree(this);
        tree.construct();
        collisionTree = tree;
    }

    public int collideWith(Collidable other, Matrix4f worldMatrix, BoundingVolume worldBound,
                           CollisionResults results) {
        if (getVertexCount() == 0) {
            return 0;
        }

        if (collisionTree == null) {
            createCollisionData();
        }

        return collisionTree.collideWith(other, worldMatrix, worldBound, results);
    }

    /**
     * Sets the {@link VertexBuffer} on the mesh.
     * This will update the vertex/triangle counts if needed.
     *
     * @param vb The buffer to set
     * @throws IllegalArgumentException If the buffer type is already set
     */
    public void setBuffer(VertexBuffer vb) {
        if (buffers.containsKey(vb.getBufferType().ordinal())) {
            throw new IllegalArgumentException("Buffer type already set: " + vb.getBufferType());
        }

        buffers.put(vb.getBufferType().ordinal(), vb);
        buffersList.add(vb);
        updateCounts();
    }

    /**
     * Unsets the {@link VertexBuffer} set on this mesh
     * with the given type. Does nothing if the vertex buffer type is not set
     * initially.
     *
     * @param type The buffer type to remove
     */
    public void clearBuffer(VertexBuffer.Type type) {
        VertexBuffer vb = buffers.remove(type.ordinal());
        if (vb != null) {
            buffersList.remove(vb);
            updateCounts();
        }
    }

    /**
     * Creates a {@link VertexBuffer} for the mesh or modifies
     * the existing one per the parameters given.
     *
     * @param type       The type of the buffer
     * @param components Number of components
     * @param format     Data format
     * @param buf        The buffer data
     * @throws UnsupportedOperationException If the buffer already set is
     *                                       incompatible with the parameters given.
     */
    public void setBuffer(VertexBuffer.Type type, int components, VertexBuffer.Format format,
                          Buffer buf) {
        VertexBuffer vb = buffers.get(type.ordinal());
        if (vb == null) {
            vb = new VertexBuffer(type);
            vb.setupData(VertexBuffer.Usage.Dynamic, components, format, buf);
            setBuffer(vb);
        } else {
            if (vb.getNumComponents() != components || vb.getFormat() != format) {
                throw new UnsupportedOperationException("The buffer already set "
                                                        + "is incompatible with the given parameters");
            }
            vb.updateData(buf);
            updateCounts();
        }
    }

    /**
     * Set a floating point {@link VertexBuffer} on the mesh.
     *
     * @param type       The type of {@link VertexBuffer},
     *                   e.g. {@link VertexBuffer.Type#Position}, {@link VertexBuffer.Type#Normal}, etc.
     * @param components Number of components on the vertex buffer, should
     *                   be between 1 and 4.
     * @param buf        The floating point data to contain
     */
    public void setBuffer(VertexBuffer.Type type, int components, FloatBuffer buf) {
        setBuffer(type, components, VertexBuffer.Format.Float, buf);
    }

    public void setBuffer(VertexBuffer.Type type, int components, float[] buf) {
        setBuffer(type, components, BufferUtils.createFloatBuffer(buf));
    }

    public void setBuffer(VertexBuffer.Type type, int components, IntBuffer buf) {
        setBuffer(type, components, VertexBuffer.Format.UnsignedInt, buf);
    }

    public void setBuffer(VertexBuffer.Type type, int components, int[] buf) {
        setBuffer(type, components, BufferUtils.createIntBuffer(buf));
    }

    public void setBuffer(VertexBuffer.Type type, int components, ShortBuffer buf) {
        setBuffer(type, components, VertexBuffer.Format.UnsignedShort, buf);
    }

    public void setBuffer(VertexBuffer.Type type, int components, byte[] buf) {
        setBuffer(type, components, BufferUtils.createByteBuffer(buf));
    }

    public void setBuffer(VertexBuffer.Type type, int components, ByteBuffer buf) {
        setBuffer(type, components, VertexBuffer.Format.UnsignedByte, buf);
    }

    public void setBuffer(VertexBuffer.Type type, int components, short[] buf) {
        setBuffer(type, components, BufferUtils.createShortBuffer(buf));
    }

    /**
     * Get the {@link VertexBuffer} stored on this mesh with the given
     * type.
     *
     * @param type The type of VertexBuffer
     * @return the VertexBuffer data, or null if not set
     */
    public VertexBuffer getBuffer(VertexBuffer.Type type) {
        return buffers.get(type.ordinal());
    }

    /**
     * Get the {@link VertexBuffer} data stored on this mesh in float
     * format.
     *
     * @param type The type of VertexBuffer
     * @return the VertexBuffer data, or null if not set
     */
    public FloatBuffer getFloatBuffer(VertexBuffer.Type type) {
        VertexBuffer vb = getBuffer(type);
        if (vb == null) {
            return null;
        }

        return (FloatBuffer) vb.getData();
    }

    /**
     * Get the {@link VertexBuffer} data stored on this mesh in short
     * format.
     *
     * @param type The type of VertexBuffer
     * @return the VertexBuffer data, or null if not set
     */
    public ShortBuffer getShortBuffer(VertexBuffer.Type type) {
        VertexBuffer vb = getBuffer(type);
        if (vb == null) {
            return null;
        }

        return (ShortBuffer) vb.getData();
    }

    /**
     * Get the index buffer for this mesh.
     * Will return <code>null</code> if no index buffer is set.
     *
     * @return The index buffer of this mesh.
     * @see VertexBuffer.Type#Index
     */
    public IndexBuffer getIndexBuffer() {
        VertexBuffer vb = getBuffer(VertexBuffer.Type.Index);
        if (vb == null) {
            return null;
        }

        return IndexBuffer.wrapIndexBuffer(vb.getData());
    }

    public void scaleTextureCoordinates(Vector2f scaleFactor) {
        VertexBuffer texBuffer = getBuffer(VertexBuffer.Type.TexCoord);
        if (texBuffer == null) {
            throw new IllegalStateException("The mesh has no texture coordinates");
        }

        if (texBuffer.getFormat() != VertexBuffer.Format.Float) {
            throw new UnsupportedOperationException(
                    "Only float texture coordinate format is supported");
        }

        if (texBuffer.getNumComponents() != 2) {
            throw new UnsupportedOperationException("Only 2D texture coordinates are supported.");
        }

        FloatBuffer data = (FloatBuffer) texBuffer.getData();
        data.clear();
        for (int i = 0; i < data.limit() * 0.5f; i++) {
            float x = data.get();
            float y = data.get();
            data.position(data.position() - 2);
            x *= scaleFactor.getX();
            y *= scaleFactor.getY();
            data.put(x).put(y);
        }
        data.clear();
        texBuffer.updateData(data);
    }

    /**
     * Updates the bounding volume of this mesh.
     * The method does nothing if the mesh has no {@link VertexBuffer.Type#Position} buffer.
     * It is expected that the position buffer is a float buffer with 3 components.
     */
    public void updateBounds() {
        VertexBuffer positionBuffer = getBuffer(VertexBuffer.Type.Position);
        if (meshBound != null && positionBuffer != null) {
            meshBound.computeFromPoints((FloatBuffer) positionBuffer.getData());
        }
    }

    /**
     * @return the {@link BoundingVolume} of this Mesh. By default the bounding volume is a
     * {@link BoundingBox}
     */
    public BoundingVolume getBound() {
        return meshBound;
    }

    /**
     * Returns a map of all {@link VertexBuffer vertex buffers} on this Mesh.
     * The integer key for the map is the {@link Enum#ordinal() ordinal}
     * of the vertex buffer's {@link VertexBuffer.Type}.
     * Note that the returned map is a reference to the map used internally,
     * modifying it will cause undefined results.
     *
     * @return map of vertex buffers on this mesh.
     */
    public Map<Integer, VertexBuffer> getBuffers() {
        return buffers;
    }

    /**
     * Returns a list of all {@link VertexBuffer vertex buffers} on this Mesh.
     * Using a list instead an IntMap via the {@link #getBuffers() } method is
     * better for iteration as there's no need to create an iterator instance.
     * Note that the returned list is a reference to the list used internally,
     * modifying it will cause undefined results.
     *
     * @return list of vertex buffers on this mesh.
     */
    public List<VertexBuffer> getBufferList() {
        return buffersList;
    }

    /**
     * Sets the count of vertices used for each tessellation patch
     *
     * @param patchVertexCount
     */
    public void setPatchVertexCount(int patchVertexCount) {
        this.patchVertexCount = patchVertexCount;
    }

    /**
     * Gets the amout of vertices used for each patch;
     *
     * @return
     */
    public int getPatchVertexCount() {
        return patchVertexCount;
    }
}
