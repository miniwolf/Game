package mini.light;

import mini.scene.Spatial;

import java.util.Arrays;
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
public class LightList implements Iterable<Light> {
    private Light[] list, tlist;
    private int listSize;
    private float[] distToOwner;
    private Spatial owner;

    private static final int DEFAULT_SIZE = 1;

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
     * Updates a "world-space" light list, using the spatial's local-space
     * light list and its parent's world-space light list.
     *
     * @param local
     * @param parent
     */
    public void update(LightList local, LightList parent){
        // clear the list as it will be reconstructed
        // using the arguments
        clear();

        while (list.length <= local.listSize){
            doubleSize();
        }

        // add the lights from the local list
        System.arraycopy(local.list, 0, list, 0, local.listSize);
        for (int i = 0; i < local.listSize; i++){
//            list[i] = local.list[i];
            distToOwner[i] = Float.NEGATIVE_INFINITY;
        }

        // if the spatial has a parent node, add the lights
        // from the parent list as well
        if (parent != null){
            int sz = local.listSize + parent.listSize;
            while (list.length <= sz)
                doubleSize();

            for (int i = 0; i < parent.listSize; i++){
                int p = i + local.listSize;
                list[p] = parent.list[i];
                distToOwner[p] = Float.NEGATIVE_INFINITY;
            }

            listSize = local.listSize + parent.listSize;
        }else{
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
     * @return The size of the list.
     */
    public int size() {
        return listSize;
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
     * Remove the light at the given index.
     *
     * @param index to be removed
     */
    private void remove(int index) {
        if (index >= listSize || index < 0) {
            throw new IndexOutOfBoundsException();
        }

        listSize--;
        if (index == listSize) {
            list[listSize] = null;
            return;
        }

        System.arraycopy(list, index + 1, list, index, listSize - index);
        list[listSize] = null;
    }

    /**
     * Resets list size to 0.
     */
    public void clear() {
        if (listSize == 0)
            return;

        for (int i = 0; i < listSize; i++)
            list[i] = null;

        if (tlist != null)
            Arrays.fill(tlist, null);

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
}
