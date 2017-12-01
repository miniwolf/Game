package mini.light;

import mini.scene.Spatial;
import mini.utils.SortUtil;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * <code>LightList</code> is used internally by {@link Spatial}s to manage
 * lights that are attached to them.
 *
 * @author miniwolf
 */
public class LightList implements Iterable<Light>, Cloneable, MiniCloneable {
    private Light[] list, tlist;
    private int listSize;
    private float[] distToOwner;
    private Spatial owner;

    private static final int DEFAULT_SIZE = 1;

    private static final Comparator<Light> c = new Comparator<Light>() {
        /**
         * This assumes lastDistance have been computed in a previous step.
         */
        public int compare(Light l1, Light l2) {
            return Float.compare(l1.lastDistance, l2.lastDistance);
        }
    };

    /**
     * Creates a <code>LightList</code> for the given {@link Spatial}.
     *
     * @param owner The spatial owner
     */
    public LightList(Spatial owner) {
        listSize = 0;
        list = new Light[DEFAULT_SIZE];
        distToOwner = new float[DEFAULT_SIZE];
        Arrays.fill(distToOwner, Float.NEGATIVE_INFINITY);
        this.owner = owner;
    }

    /**
     * Sorts the elements in the list according to their Comparator.
     * There are two reasons why lights should be resorted.
     * First, if the lights have moved, that means their distance to
     * the spatial changed.
     * Second, if the spatial itself moved, it means the distance from it to
     * the individual lights might have changed.
     *
     *
     * @param transformChanged Whether the spatial's transform has changed
     */
    public void sort(boolean transformChanged) {
        if (listSize > 1) {
            // resize or populate our temporary array as necessary
            if (tlist == null || tlist.length != list.length) {
                tlist = list.clone();
            } else {
                System.arraycopy(list, 0, tlist, 0, list.length);
            }

            if (transformChanged){
                // check distance of each light
                for (int i = 0; i < listSize; i++){
                    list[i].computeLastDistance(owner);
                }
            }

            // now merge sort tlist into list
            SortUtil.msort(tlist, list, 0, listSize - 1, c);
        }
    }

    /**
     * Updates a "world-space" light list, using the spatial's local-space
     * light list and its parent's world-space light list.
     *
     * @param local
     * @param parent
     */
    public void update(LightList local, LightList parent) {
        // clear the list as it will be reconstructed
        // using the arguments
        clear();

        while (list.length <= local.listSize) {
            doubleSize();
        }

        // add the lights from the local list
        System.arraycopy(local.list, 0, list, 0, local.listSize);
        for (int i = 0; i < local.listSize; i++) {
//            list[i] = local.list[i];
            distToOwner[i] = Float.NEGATIVE_INFINITY;
        }

        // if the spatial has a parent node, add the lights
        // from the parent list as well
        if (parent != null) {
            int sz = local.listSize + parent.listSize;
            while (list.length <= sz) {
                doubleSize();
            }

            for (int i = 0; i < parent.listSize; i++) {
                int p = i + local.listSize;
                list[p] = parent.list[i];
                distToOwner[p] = Float.NEGATIVE_INFINITY;
            }

            listSize = local.listSize + parent.listSize;
        } else {
            listSize = local.listSize;
        }
    }

    /**
     * Returns an iterator that can be used to iterate over this LightList.
     *
     * @return an iterator that can be used to iterate over this LightList.
     */
    public Iterator<Light> iterator() {
        return new Iterator<Light>() {

            int index = 0;

            public boolean hasNext() {
                return index < size();
            }

            public Light next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                return list[index++];
            }

            public void remove() {
                LightList.this.remove(--index);
            }
        };
    }

    /**
     * Remove the light at the given index.
     *
     * @param index
     */
    public void remove(int index){
        if (index >= listSize || index < 0)
            throw new IndexOutOfBoundsException();

        listSize --;
        if (index == listSize){
            list[listSize] = null;
            return;
        }

        System.arraycopy(list, index + 1, list, index, listSize - index);
        list[listSize] = null;
    }

    /**
     * Removes the given light from the LightList.
     *
     * @param l the light to remove
     */
    public void remove(Light l){
        for (int i = 0; i < listSize; i++){
            if (list[i] == l){
                remove(i);
                return;
            }
        }
    }

    /**
     * @return The size of the list.
     */
    public int size() {
        return listSize;
    }

    /**
     * @return the light at the given index.
     * @throws IndexOutOfBoundsException If the given index is outside bounds.
     */
    public Light get(int num) {
        if (num >= listSize || num < 0) {
            throw new IndexOutOfBoundsException();
        }

        return list[num];
    }

    private void doubleSize() {
        Light[] temp = new Light[list.length * 2];
        float[] temp2 = new float[list.length * 2];
        System.arraycopy(list, 0, temp, 0, list.length);
        System.arraycopy(distToOwner, 0, temp2, 0, list.length);
        list = temp;
        distToOwner = temp2;
    }

    /**
     * Adds a light to the list. List size is doubled if there is no room.
     *
     * @param l The light to add.
     */
    public void add(Light l) {
        if (listSize == list.length) {
            doubleSize();
        }
        list[listSize] = l;
        distToOwner[listSize++] = Float.NEGATIVE_INFINITY;
    }

    /**
     * Resets list size to 0.
     */
    public void clear() {
        if (listSize == 0) {
            return;
        }

        for (int i = 0; i < listSize; i++) {
            list[i] = null;
        }

        if (tlist != null) {
            Arrays.fill(tlist, null);
        }

        listSize = 0;
    }

    @Override
    public void forEach(Consumer<? super Light> action) {
        Objects.requireNonNull(action);
        for (Light light : list) {
            action.accept(light);
        }
    }

    @Override
    public Spliterator<Light> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }

    @Override
    public Object miniClone() {
        try {
            LightList clone = (LightList) super.clone();
            clone.tlist = null; // List used for sorting only
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        this.owner = cloner.clone(owner);
        this.list = cloner.clone(list);
        this.distToOwner = cloner.clone(distToOwner);
    }
}
