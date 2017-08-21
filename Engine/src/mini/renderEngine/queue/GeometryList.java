package mini.renderEngine.queue;

import mini.renderEngine.Camera;
import mini.scene.Geometry;
import mini.utils.ListSort;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is a special purpose list of {@link Geometry} objects for render
 * queuing.
 *
 * @author Jack Lindamood
 * @author Three Rings - better sorting alg.
 * @author Kirill Vainer
 */
public class GeometryList implements Iterable<Geometry> {
    private static final int DEFAULT_SIZE = 32;

    private Geometry[] geometries;
    private ListSort listSort;
    private int size;
    private GeometryComparator comparator;

    /**
     * Initializes the GeometryList to use the given {@link GeometryComparator}
     * to use for comparing geometries.
     *
     * @param comparator The comparator to use.
     */
    public GeometryList(GeometryComparator comparator) {
        size = 0;
        geometries = new Geometry[DEFAULT_SIZE];
        this.comparator = comparator;
        listSort = new ListSort<Geometry>();
    }

    public void setComparator(GeometryComparator comparator) {
        this.comparator = comparator;
    }

    /**
     * Returns the GeometryComparator that this Geometry list uses
     * for sorting.
     */
    public GeometryComparator getComparator() {
        return comparator;
    }

    /**
     * Set the camera that will be set on the geometry comparators
     * via {@link GeometryComparator#setCamera(Camera)}.
     *
     * @param cam Camera to use for sorting.
     */
    public void setCamera(Camera cam) {
        this.comparator.setCamera(cam);
    }

    /**
     * Returns the number of elements in this GeometryList.
     *
     * @return Number of elements in the list
     */
    public int size() {
        return size;
    }

    /**
     * Sets the element at the given index.
     *
     * @param index The index to set
     * @param value The value
     */
    public void set(int index, Geometry value) {
        geometries[index] = value;
    }

    /**
     * Returns the element at the given index.
     *
     * @param index The index to lookup
     * @return Geometry at the index
     */
    public Geometry get(int index) {
        return geometries[index];
    }

    /**
     * Adds a geometry to the list.
     * List size is doubled if there is no room.
     *
     * @param g The geometry to add.
     */
    public void add(Geometry g) {
        if (size == geometries.length) {
            Geometry[] temp = new Geometry[size * 2];
            System.arraycopy(geometries, 0, temp, 0, size);
            geometries = temp; // original list replaced by double-size list
        }
        geometries[size++] = g;
    }

    /**
     * Resets list size to 0.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            geometries[i] = null;
        }

        size = 0;
    }

    /**
     * Sorts the elements in the list according to their Comparator.
     */
    public void sort() {
        if (size > 1) {
            // sort the spatial list using the comparator
            if (listSort.getLength() != size) {
                listSort.allocateStack(size);
            }
            listSort.sort(geometries, comparator);
        }
    }

    public Iterator<Geometry> iterator() {
        return new Iterator<Geometry>() {

            int index = 0;

            public boolean hasNext() {
                return index < size();
            }

            public Geometry next() {
                if (index >= size()) {
                    throw new NoSuchElementException(
                            "Geometry list has only " + size() + " elements");
                }
                return get(index++);
            }

            public void remove() {
                throw new UnsupportedOperationException(
                        "Geometry list doesn't support iterator removal");
            }
        };
    }
}
