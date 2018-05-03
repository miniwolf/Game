package mini.animation;

import mini.math.Vector3f;

/**
 * Serialize and compress Vector3f[] by indexing same values
 */
public class CompactVector3fArray extends CompactArray<Vector3f> {
    /**
     * Creates a compact vector array
     */
    public CompactVector3fArray() {
    }

    /**
     * creates a compact vector array
     *
     * @param dataArray the data array
     * @param index     the indices
     */
    public CompactVector3fArray(float[] dataArray, int[] index) {
        super(dataArray, index);
    }

    @Override
    protected final int getTupleSize() {
        return 3;
    }

    @Override
    protected final Class<Vector3f> getElementClass() {
        return Vector3f.class;
    }

    @Override
    protected void serialize(int i, Vector3f store) {
        int j = i * getTupleSize();
        array[j] = store.getX();
        array[j + 1] = store.getY();
        array[j + 2] = store.getZ();
    }

    @Override
    protected Vector3f deserialize(int i, Vector3f store) {
        int j = i * getTupleSize();
        store.set(array[j], array[j + 1], array[j + 2]);
        return store;
    }
}
