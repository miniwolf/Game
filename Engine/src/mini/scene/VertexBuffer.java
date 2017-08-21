package mini.scene;

import mini.renderEngine.opengl.GLRenderer;
import mini.utils.BufferUtils;
import mini.utils.NativeObject;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Created by miniwolf on 15-04-2017.
 */
public class VertexBuffer extends NativeObject {
    private boolean updateNeeded;
    private boolean normalized = false;
    private int id = -1;
    private int stride = 0;
    private int offset = 0;

    /**
     * Type of buffer. Specifies the actual attribute it defines.
     */
    public enum Type {
        /**
         * Position of the vertex (3 floats)
         */
        Position,

        /**
         * The size of the point when using point buffers (float).
         */
        Size,

        /**
         * Normal vector, normalized (3 floats).
         */
        Normal,

        /**
         * Texture coordinate (2 float)
         */
        TexCoord,

        /**
         * Color and Alpha (4 floats)
         */
        Color,

        /**
         * Tangent vector, normalized (4 floats) (x,y,z,w). The w component is
         * called the binormal parity, is not normalized, and is either 1f or
         * -1f. It's used to compute the direction on the binormal vector on the
         * GPU at render time.
         */
        Tangent,

        /**
         * Binormal vector, normalized (3 floats, optional)
         */
        Binormal,

        /**
         * Specifies the source data for various vertex buffers
         * when interleaving is used. By default the format is
         * byte.
         */
        InterleavedData,

        /**
         * Specifies the index buffer, must contain integer data
         * (ubyte, ushort, or uint).
         */
        Index,

        /**
         * Texture coordinate #2
         */
        TexCoord2,

        /**
         * Texture coordinate #3
         */
        TexCoord3,

        /**
         * Texture coordinate #4
         */
        TexCoord4,

        /**
         * Texture coordinate #5
         */
        TexCoord5,

        /**
         * Texture coordinate #6
         */
        TexCoord6,

        /**
         * Texture coordinate #7
         */
        TexCoord7,

        /**
         * Texture coordinate #8
         */
        TexCoord8,

        /**
         * Information about this instance.
         * <p>
         * Format should be {@link Format#Float} and number of components should be 16.
         */
        InstanceData
    }

    /**
     * The usage of the VertexBuffer, specifies how often the buffer is used. This can determine if
     * a vertex buffer is placed in VRAM or held in video memory, but no guarantees are made- it's
     * only a hint.
     */
    public enum Usage {

        /**
         * Mesh data is sent once and very rarely updated.
         */
        Static,

        /**
         * Mesh data is updated occasionally (once per frame or less).
         */
        Dynamic,

        /**
         * Mesh data is updated every frame.
         */
        Stream,

        /**
         * Mesh data is <em>not</em> sent to GPU at all. It is only
         * used by the CPU.
         */
        CpuOnly
    }

    /**
     * Specifies format of the data stored in the buffer.
     * This should directly correspond to the buffer's class, for example, an
     * {@link Format#UnsignedShort} formatted buffer should use the class {@link ShortBuffer}
     * (e.g. the closest resembling type).
     * For the {@link Format#Half} type, {@link ByteBuffer}s should be used.
     */
    public enum Format {
        /**
         * Half precision floating point.
         * 2 bytes, signed.
         */
        Half(2),

        /**
         * Single precision floating point.
         * 4 bytes, signed
         */
        Float(4),

        /**
         * Double precision floating point.
         * 8 bytes, signed. May not
         * be supported by all GPUs.
         */
        Double(8),

        /**
         * 1 byte integer, signed.
         */
        Byte(1),

        /**
         * 1 byte integer, unsigned.
         */
        UnsignedByte(1),

        /**
         * 2 byte integer, signed.
         */
        Short(2),

        /**
         * 2 byte integer, unsigned.
         */
        UnsignedShort(2),

        /**
         * 4 byte integer, signed.
         */
        Int(4),

        /**
         * 4 byte integer, unsigned.
         */
        UnsignedInt(4);

        private int componentSize = 0;

        Format(int componentSize) {
            this.componentSize = componentSize;
        }

        /**
         * Returns the size in bytes of this data type.
         *
         * @return Size in bytes of this data type.
         */
        public int getComponentSize() {
            return componentSize;
        }
    }

    /**
     * derived from components format.getComponentSize()
     */
    private transient int componentsLength = 0;
    private transient boolean dataSizeChanged = false;

    private int instanceSpan = 0;
    private int components = 0;
    private int lastLimit = 0;
    private Type bufType;
    private Buffer data = null;
    private Usage usage;
    private Format format;

    /**
     * Creates an empty, uninitialized buffer.
     * Must call setupData() to initialize.
     */
    public VertexBuffer(Type type) {
        super();
        this.bufType = type;
    }

    /**
     * Serialization only. Do not use.
     */
    public VertexBuffer() {
        super();
    }

    protected VertexBuffer(int id) {
        super(id);
    }

    /**
     * Called to initialize the data in the <code>VertexBuffer</code>. Must only
     * be called once.
     *
     * @param usage      The usage for the data, or how often will the data be updated per frame.
     *                   See the {@link Usage} enum.
     * @param components The number of components per element.
     * @param format     The {@link Format format}, or data-type of a single component.
     * @param data       A native buffer, the format of which matches the {@link Format} argument.
     */
    public void setupData(Usage usage, int components, Format format, Buffer data) {
        if (usage == null || format == null || data == null) {
            throw new IllegalArgumentException("None of the arguments can be null");
        }

        if (data.isReadOnly()) {
            throw new IllegalArgumentException("VertexBuffer data cannot be read-only.");
        }

        if (bufType != Type.InstanceData) {
            if (components < 1 || components > 4) {
                throw new IllegalArgumentException("components must be between 1 and 4");
            }
        }

        this.data = data;
        this.components = components;
        this.usage = usage;
        this.format = format;
        this.componentsLength = components * format.getComponentSize();
        this.lastLimit = data.limit();
        setUpdateNeeded();
    }

    /**
     * Called to update the data in the buffer with new data. Can only
     * be called after {@link VertexBuffer#setupData(VertexBuffer.Usage, int, VertexBuffer.Format, java.nio.Buffer) }
     * has been called. Note that it is fine to call this method on the
     * data already set, e.g. vb.updateData(vb.getData()), this will just
     * set the proper update flag indicating the data should be sent to the GPU
     * again.
     * <p>
     * It is allowed to specify a buffer with different capacity than the
     * originally set buffer, HOWEVER, if you do so, you must
     * call Mesh.updateCounts() otherwise bizarre errors can occur.
     *
     * @param data The data buffer to set
     */
    public void updateData(Buffer data) {
        // Check if the data buffer is read-only which is a sign
        // of a bug on the part of the caller
        if (data != null && data.isReadOnly()) {
            throw new IllegalArgumentException("VertexBuffer data cannot be read-only.");
        }

        // will force renderer to call glBufferData again
        if (data != null && (this.data.getClass() != data.getClass()
                || data.limit() != lastLimit)) {
            dataSizeChanged = true;
            lastLimit = data.limit();
        }

        this.data = data;
        setUpdateNeeded();
    }

    public void clearUpdateNeeded() {
        updateNeeded = false;
        dataSizeChanged = false;
    }

    /**
     * @return The usage of this buffer. See {@link Usage} for more
     * information.
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * @param usage The usage of this buffer. See {@link Usage} for more
     *              information.
     */
    public void setUsage(Usage usage) {
//        if (id != -1)
//            throw new UnsupportedOperationException("Data has already been sent. Cannot set usage.");

        this.usage = usage;
        this.setUpdateNeeded();
    }

    /**
     * @return The type of information that this buffer has.
     */
    public Type getBufferType() {
        return bufType;
    }

    /**
     * @return The {@link Format format}, or data type of the data.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Returns the raw internal data buffer used by this VertexBuffer.
     * This buffer is not safe to call from multiple threads since buffers
     * have their own internal position state that cannot be shared.
     * Call getData().duplicate(), getData().asReadOnlyBuffer(), or
     * the more convenient getDataReadOnly() if the buffer may be accessed
     * from multiple threads.
     *
     * @return A native buffer, in the specified {@link Format format}.
     */
    public Buffer getData() {
        return data;
    }

    /**
     * @return The number of components of the given {@link Format format} per
     * element.
     */
    public int getNumComponents() {
        return components;
    }

    /**
     * @return The total number of data elements in the data buffer.
     */
    public int getNumElements() {
        int elements = data.limit() / components;
        if (format == Format.Half)
            elements /= 2;
        return elements;
    }

    /**
     * @param normalized Set to true if integer components should be converted
     *                   from their maximal range into the range 0.0 - 1.0 when converted to
     *                   a floating-point value for the shader.
     *                   E.g. if the {@link Format} is {@link Format#UnsignedInt}, then
     *                   the components will be converted to the range 0.0 - 1.0 by dividing
     *                   every integer by 2^32.
     */
    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    /**
     * @return True if integer components should be converted to the range 0-1.
     * @see VertexBuffer#setNormalized(boolean)
     */
    public boolean isNormalized() {
        return normalized;
    }

    /**
     * Sets the instanceSpan to 1 or 0 depending on
     * the value of instanced and the existing value of
     * instanceSpan.
     */
    public void setInstanced(boolean instanced) {
        if (instanced && instanceSpan == 0) {
            instanceSpan = 1;
        } else if (!instanced) {
            instanceSpan = 0;
        }
    }

    /**
     * Returns true if instanceSpan is more than 0 indicating
     * that this vertex buffer contains per-instance data.
     */
    public boolean isInstanced() {
        return instanceSpan > 0;
    }

    /**
     * Sets how this vertex buffer matches with rendered instances
     * where 0 means no instancing at all, ie: all elements are
     * per vertex.  If set to 1 then each element goes with one
     * instance.  If set to 2 then each element goes with two
     * instances and so on.
     */
    public void setInstanceSpan(int i) {
        this.instanceSpan = i;
    }

    public int getInstanceSpan() {
        return instanceSpan;
    }

    /**
     * @return The stride (in bytes) for the data.
     * @see #setStride(int)
     */
    public int getStride() {
        return stride;
    }

    /**
     * Set the stride (in bytes) for the data.
     * <p>
     * If the data is packed in the buffer, then stride is 0, if there's other
     * data that is between the current component and the next component in the
     * buffer, then this specifies the size in bytes of that additional data.
     *
     * @param stride the stride (in bytes) for the data
     */
    public void setStride(int stride) {
        this.stride = stride;
    }

    /**
     * @return The offset after which the data is sent to the GPU.
     * @see #setOffset(int)
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset Specify the offset (in bytes) from the start of the buffer
     *               after which the data is sent to the GPU.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Creates a {@link Buffer} that satisfies the given type and size requirements
     * of the parameters. The buffer will be of the type specified by
     * {@link Format format} and would be able to contain the given number
     * of elements with the given number of components in each element.
     */
    public static Buffer createBuffer(Format format, int components, int numElements) {
        if (components < 1 || components > 4)
            throw new IllegalArgumentException("Num components must be between 1 and 4");

        int total = numElements * components;

        switch (format) {
            case Byte:
            case UnsignedByte:
                return BufferUtils.createByteBuffer(total);
            case Half:
                return BufferUtils.createByteBuffer(total * 2);
            case Short:
            case UnsignedShort:
                return BufferUtils.createShortBuffer(total);
            case Int:
            case UnsignedInt:
                return BufferUtils.createIntBuffer(total);
            case Float:
                return BufferUtils.createFloatBuffer(total);
            case Double:
                return BufferUtils.createDoubleBuffer(total);
            default:
                throw new UnsupportedOperationException("Unrecoginized buffer format: " + format);
        }
    }

    /**
     * Returns the number of 'instances' in this VertexBuffer.  This
     * is dependent on the current instanceSpan.  When instanceSpan
     * is 0 then 'instances' is 1. Otherwise, instances is elements *
     * instanceSpan.  It is possible to render a mesh with more instances
     * but the instance data begins to repeat.
     */
    public int getBaseInstanceCount() {
        return 1;
        // TODO: Support instanced rendering
    }

    @Override
    public void resetObject() {
        this.id = -1;
        setUpdateNeeded();
    }

    @Override
    public long getUniqueId() {
        return ((long) OBJTYPE_VERTEXBUFFER << 32) | ((long) id);
    }

    @Override
    public NativeObject createDestructableClone() {
        return new VertexBuffer(id);
    }

    @Override
    public void deleteObject(Object rendererObject) {
        ((GLRenderer) rendererObject).deleteBuffer(this);
    }
}
