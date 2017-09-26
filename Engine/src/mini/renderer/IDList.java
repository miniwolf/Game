package mini.renderer;

import java.util.Arrays;

/**
 * A specialized data-structure used to optimize state changes of "slot"
 * based state.
 */
public class IDList {
    public int[] newList = new int[16];
    public int[] oldList = new int[16];
    public int newLen = 0;
    public int oldLen = 0;

    /**
     * Reset all states to zero
     */
    public void reset(){
        newLen = 0;
        oldLen = 0;
        Arrays.fill(newList, 0);
        Arrays.fill(oldList, 0);
    }

    /**
     * Adds an index to the new list.
     * If the index was not in the old list, false is returned,
     * if the index was in the old list, it is removed from the old
     * list and true is returned.
     *
     * @param idx The index to move
     * @return True if it existed in old list and was removed
     * from there, false otherwise.
     */
    public boolean moveToNew(int idx){
        if (newLen == 0 || newList[newLen - 1] != idx) {
            // add item to newList first
            newList[newLen++] = idx;
        }

        // find idx in oldList, if removed successfully, return true.
        for (int i = 0; i < oldLen; i++){
            if (oldList[i] == idx){
                // found index in slot i
                // delete index from old list
                oldLen --;
                System.arraycopy(oldList, i + 1, oldList, i, oldLen - i);
                return true;
            }
        }
        return false;
    }

    /**
     * Copies the new list to the old list, and clears the new list.
     */
    public void copyNewToOld(){
        System.arraycopy(newList, 0, oldList, 0, newLen);
        oldLen = newLen;
        newLen = 0;
    }

    /**
     * Prints the contents of the lists
     */
    public void print(){
        if (newLen > 0){
            System.out.print("New List: ");
            for (int i = 0; i < newLen; i++){
                if (i == newLen -1)
                    System.out.println(newList[i]);
                else
                    System.out.print(newList[i]+", ");
            }
        }
        if (oldLen > 0){
            System.out.print("Old List: ");
            for (int i = 0; i < oldLen; i++){
                if (i == oldLen -1)
                    System.out.println(oldList[i]);
                else
                    System.out.print(oldList[i]+", ");
            }
        }
    }

}