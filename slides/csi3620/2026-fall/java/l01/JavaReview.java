package l01;

import java.util.List;

/** The Java features this course leans on: generics and recursion. */
public final class JavaReview {
    private JavaReview() { }

    /** Largest item; works for any type whose values can be compared. */
    public static <T extends Comparable<T>> T maxOf(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("no items");
        }
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) > 0) {
                best = item;
            }
        }
        return best;
    }

    /** 1 + 2 + ... + n, written recursively; 0 for n <= 0 (an empty sum). */
    public static int sumTo(int n) {
        if (n <= 0) {
            return 0;                  // base case: no recursion
        }
        return n + sumTo(n - 1);       // recursive case: smaller input
    }

    /**
     * An instrumented copy of sumTo for the trace slide: it logs each call with the
     * number of frames on the stack (this call included) and each return value.
     */
    public static int sumToTraced(int n, int frames, List<String> log) {
        log.add("call sumTo(" + n + "), frames " + frames);
        int result;
        if (n <= 0) {
            result = 0;
        } else {
            result = n + sumToTraced(n - 1, frames + 1, log);
        }
        log.add("sumTo(" + n + ") returns " + result);
        return result;
    }
}
