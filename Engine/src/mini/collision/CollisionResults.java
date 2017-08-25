package mini.collision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <code>CollisionResults</code> is a collection returned as a result of a 
 * collision detection operation done by {@link Collidable}.
 */
public class CollisionResults implements Iterable<CollisionResult> {

    private ArrayList<CollisionResult> results = null;
    private boolean sorted = true;

    /**
     * Clears all collision results added to this list
     */
    public void clear(){
        if (results != null) {
            results.clear();
        }
    }

    /**
     * Iterator for iterating over the collision results.
     * 
     * @return the iterator
     */
    public Iterator<CollisionResult> iterator() {
        if (results == null) {
            List<CollisionResult> dumbCompiler = Collections.emptyList();            
            return dumbCompiler.iterator();
        }
        
        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.iterator();
    }

    public void addCollision(CollisionResult result){
        if (results == null) {
            results = new ArrayList<CollisionResult>();
        }
        results.add(result);
        sorted = false;
    }

    public int size(){
        if (results == null) {
            return 0;
        }
        return results.size();
    }

    public CollisionResult getClosestCollision(){
        if (results == null || size() == 0)
            return null;

        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(0);
    }

    public CollisionResult getFarthestCollision(){
        if (results == null || size() == 0)
            return null;

        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(size()-1);
    }

    public CollisionResult getCollision(int index){
        if (results == null) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 0");
        }
        
        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(index);
    }

    /**
     * Internal use only.
     * @param index
     * @return
     */
    public CollisionResult getCollisionDirect(int index){
        if (results == null) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 0");
        }
        return results.get(index);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("CollisionResults[");
        if (results != null) {
            for (CollisionResult result : results){
                sb.append(result).append(", ");
            }
            if (results.size() > 0)
                sb.setLength(sb.length()-2);
        }                

        sb.append("]");
        return sb.toString();
    }

}
