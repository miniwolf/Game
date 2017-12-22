package mini.utils.clone;

import java.util.List;

public class ListCloneFunction<T extends List<T>> implements CloneFunction<T> {
    @Override
    public T cloneObject(Cloner cloner, T object) {
        try {
            return cloner.javaClone(object);
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Clone not supported for type: " + object.getClass(),
                                               e);
        }
    }

    /**
     * Clones the elements of the list.
     */
    @Override
    public void cloneFields(Cloner cloner, T clone, T object) {
        for (int i = 0; i < clone.size(); i++) {
            clone.set(i, cloner.clone(clone.get(i)));
        }
    }
}
