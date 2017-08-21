package mini.utils;

import java.util.Comparator;

public class SortUtil {

    /**
     * Merge sort
     */
    public static void msort(Object[] src, Object[] dest, Comparator comp) {
        msort(src, dest, 0, src.length - 1, comp);
    }

    /**
     * Merge sort
     *
     * @param src  Source array
     * @param dest Destination array
     * @param low  Index of beginning element
     * @param high Index of end element
     * @param comp Comparator
     */
    public static void msort(Object[] src, Object[] dest, int low, int high,
                             Comparator comp) {
        if (low < high) {
            int center = (low + high) / 2;
            msort(src, dest, low, center, comp);
            msort(src, dest, center + 1, high, comp);
            merge(src, dest, low, center + 1, high, comp);
        }
    }

    private static void merge(Object[] src, Object[] dest,
                              int low, int middle, int high, Comparator comp) {
        int leftEnd = middle - 1;
        int pos = low;
        int numElements = high - low + 1;

        while (low <= leftEnd && middle <= high) {
            if (comp.compare(src[low], src[middle]) <= 0) {
                dest[pos++] = src[low++];
            } else {
                dest[pos++] = src[middle++];
            }
        }

        while (low <= leftEnd) {
            dest[pos++] = src[low++];
        }

        while (middle <= high) {
            dest[pos++] = src[middle++];
        }

        for (int i = 0; i < numElements; i++, high--) {
            src[high] = dest[high];
        }
    }
}
