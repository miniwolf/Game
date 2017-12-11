package mini.utils;

/**
 * Describes a native object. An encapsulation of a certain object
 * on the native side of the graphics or audio library.
 * <p>
 * This class is used to track when OpenGL and OpenAL native objects are
 * collected by the garbage collector, and then invoke the proper destructor
 * on the OpenGL library to delete it from memory.
 */
public abstract class NativeObject implements Cloneable {

    public static final int INVALID_ID = -1;

    protected static final int OBJTYPE_VERTEXBUFFER = 1,
            OBJTYPE_TEXTURE = 2,
            OBJTYPE_FRAMEBUFFER = 3,
            OBJTYPE_SHADER = 4,
            OBJTYPE_SHADERSOURCE = 5,
            OBJTYPE_AUDIOBUFFER = 6,
            OBJTYPE_AUDIOSTREAM = 7,
            OBJTYPE_FILTER = 8;

    /**
     * The object manager to which this NativeObject is registered to.
     */
    protected NativeObjectManager objectManager = null;

    /**
     * The ID of the object, usually depends on its type.
     * Typically returned from calls like glGenTextures, glGenBuffers, etc.
     */
    protected int id = INVALID_ID;

    /**
     * A reference to a "handle". By hard referencing a certain object, it's
     * possible to find when a certain GLObject is no longer used, and to delete
     * its instance from the graphics library.
     */
    protected Object handleRef = null;

    /**
     * True if the data represented by this GLObject has been changed
     * and needs to be updated before used.
     */
    protected boolean updateNeeded = true;

    /**
     * Creates a new GLObject with the given type. Should be
     * called by the subclasses.
     *
     * @param type The type that the subclass represents.
     */
    public NativeObject() {
        this.handleRef = new Object();
    }

    /**
     * Protected constructor that doesn't allocate handle ref.
     * This is used in subclasses for the createDestructableClone().
     */
    protected NativeObject(int id) {
        this.id = id;
    }

    void setNativeObjectManager(NativeObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    /**
     * Sets the ID of the NativeObject. This method is used in Renderer and must
     * not be called by the user.
     *
     * @param id The ID to set
     */
    public void setId(int id) {
        if (this.id != INVALID_ID) {
            throw new IllegalStateException("ID has already been set for this GL object.");
        }
        this.id = id;
    }

    /**
     * @return The ID of the object. Should not be used by user code in most
     * cases.
     */
    public int getId() {
        return id;
    }

    /**
     * Internal use only. Indicates that the object has changed
     * and its state needs to be updated.
     */
    public void setUpdateNeeded() {
        updateNeeded = true;
    }

    /**
     * Internal use only. Indicates that the state changes were applied.
     */
    public void clearUpdateNeeded() {
        updateNeeded = false;
    }

    /**
     * Internal use only. Check if {@link #setUpdateNeeded()} was called before.
     */
    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    @Override
    public String toString() {
        return "Native" + getClass().getSimpleName() + " " + id;
    }

    /**
     * This should create a deep clone. For a shallow clone, use
     * createDestructableClone().
     */
    @Override
    protected NativeObject clone() {
        try {
            NativeObject obj = (NativeObject) super.clone();
            obj.handleRef = new Object();
            obj.id = INVALID_ID;
            obj.updateNeeded = true;
            return obj;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     * Deletes any associated native {@link Buffer buffers}.
     * This is necessary because it is unlikely that native buffers
     * will be garbage collected naturally (due to how GC works), therefore
     * the collection must be handled manually.
     * <p>
     * Only implementations that manage native buffers need to override
     * this method. Note that the behavior that occurs when a
     * deleted native buffer is used is not defined, therefore this
     * method is protected
     */
    protected void deleteNativeBuffers() {
    }

    /**
     * Package-private version of {@link #deleteNativeBuffers() }, to be used
     * from the {@link NativeObjectManager}.
     */
    void deleteNativeBuffersInternal() {
        deleteNativeBuffers();
    }

    /**
     * Called when the GL context is restarted to reset all IDs. Prevents
     * "white textures" on display restart.
     */
    public abstract void resetObject();

    /**
     * Deletes the GL object from the GPU when it is no longer used. Called
     * automatically by the GL object manager.
     *
     * @param rendererObject The renderer to be used to delete the object
     */
    public abstract void deleteObject(Object rendererObject);

    /**
     * Creates a shallow clone of this GL Object. The deleteObject method
     * should be functional for this object.
     */
    public abstract NativeObject createDestructableClone();

    /**
     * Returns a unique ID for this NativeObject. No other NativeObject shall
     * have the same ID.
     *
     * @return unique ID for this NativeObject.
     */
    public abstract long getUniqueId();
}
