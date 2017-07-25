package mini.scene;

import mini.utils.BufferUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mesh {
    /**
     * The mode of the Mesh specifies both the type of primitive represented
     * by the mesh and how the data should be interpreted.
     */
    public enum Mode {
        /**
         * A primitive is a triangle. Each 3 vertices specify a single
         * triangle.
         */
        Triangles(true);

        private boolean listMode = false;

        Mode(boolean listMode) {
            this.listMode = listMode;
        }

        /**
         * Returns true if the specified mode is a list mode (meaning, it specifies the indices
         * as a linear list and not some special format).
         * Will return true for the types {@link #Points}, {@link #Lines} and
         * {@link #Triangles}.
         *
         * @return true if the mode is a list type mode
         */
        public boolean isListMode() {
            return listMode;
        }
    }

    private transient int vertexArrayID = -1;
    private int vertCount = -1;
    private int elementCount = -1;
    private int instanceCount = -1;
    private Mode mode = Mode.Triangles;

    private List<VertexBuffer> buffersList = new ArrayList<>();
    private Map<Integer, VertexBuffer> buffers = new HashMap<>();

    /**
     * Returns how many triangles or elements are on this Mesh.
     * This value is only updated when {@link #updateCounts() } is called.
     * If the mesh mode is not a triangle mode, then this returns the
     * number of elements/primitives, e.g. how many lines or how many points,
     * instead of how many triangles.
     *
     * @return how many triangles/elements are on this Mesh.
     */
    public int getTriangleCount(){
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
     * @param type       The type of {@link VertexBuffer}, e.g. {@link VertexBuffer.Type#Position},
     *                   {@link VertexBuffer.Type#Normal}, etc.
     * @param components Number of components on the vertex buffer, should be between 1 and 4.
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
     * Update the {@link #getVertexCount() vertex} and {@link #getTriangleCount() triangle}
     * counts for this mesh based on the current data. This method should be called after the
     * {@link Buffer#capacity() capacities} of the mesh's {@link VertexBuffer vertex buffers}
     * has been altered.
     */
    public void updateCounts() {
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
        //instanceCount = computeInstanceCount();
    }

    /**
     * Get the {@link VertexBuffer} stored on this mesh with the given type.
     *
     * @param type The type of VertexBuffer
     * @return the VertexBuffer data, or null if not set
     */
    public VertexBuffer getBuffer(VertexBuffer.Type type) {
        return buffers.get(type.ordinal());
    }

    private int computeNumElements(int bufSize) {
        switch (mode) {
            case Triangles:
                return bufSize / 3;
            default:
                throw new UnsupportedOperationException();
        }
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
        if (vertexArrayID != -1)
            throw new IllegalStateException("ID has already been set.");

        vertexArrayID = id;
    }

    /**
     * Change the Mesh's mode. By default the mode is {@link Mode#Triangles}.
     *
     * @param mode The new mode to set
     *
     * @see Mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        updateCounts();
    }

    /**
     * Returns the mesh mode
     *
     * @return the mesh mode
     *
     * @see #setMode(Mesh.Mode)
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Indicates to the GPU that this mesh will not be modified (a hint).
     * Sets the usage mode to {@link VertexBuffer.Usage#Static}
     * for all {@link VertexBuffer vertex buffers} on this Mesh.
     */
    public void setStatic() {
        for (VertexBuffer vb : buffersList){
            vb.setUsage(VertexBuffer.Usage.Static);
        }
    }

    /**
     * Returns a list of all {@link VertexBuffer vertex buffers} on this Mesh.
     * Using a list instead of an IntMap is better for iteration as there's no need to create
     * an iterator instance. Note that the returned list is a reference to the list used
     * internally, modifying it will cause undefined results.
     *
     * @return list of vertex buffers on this mesh.
     */
    public List<VertexBuffer> getBufferList(){
        return buffersList;
    }

    /**
     * Needed for instanced rendering
     */
    private int computeInstanceCount() {
        // Whatever the max of the base instance counts
        return buffersList.stream()
                          .map(VertexBuffer::getBaseInstanceCount)
                          .max((a, b) -> a > b ? a : b)
                          .orElse(1);
    }
}
