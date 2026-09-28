package l21;

import java.util.ArrayList;
import java.util.List;

/** Choose numbers from a sorted array of positive integers that add up to a target. */
public final class SubsetSum {
    private SubsetSum() { }

    private static long nodes;                      // calls made by the most recent search

    /** Every subset (each number used at most once) with the given sum. Input sorted ascending. */
    public static List<List<Integer>> subsets(int[] sorted, int target, boolean prune) {
        List<List<Integer>> out = new ArrayList<>();
        nodes = 0;
        search(sorted, 0, target, new ArrayList<>(), out, prune, false);
        return out;
    }

    /** Every multiset (each number any number of times) with the given sum. Input sorted. */
    public static List<List<Integer>> combinations(int[] sorted, int target) {
        List<List<Integer>> out = new ArrayList<>();
        nodes = 0;
        search(sorted, 0, target, new ArrayList<>(), out, true, true);
        return out;
    }

    /** Nodes visited by the most recent call of subsets or combinations. */
    public static long lastNodes() {
        return nodes;
    }

    private static void search(int[] a, int start, int remaining, List<Integer> chosen,
                               List<List<Integer>> out, boolean prune, boolean reuse) {
        nodes++;
        if (remaining == 0) {
            out.add(new ArrayList<>(chosen));
            return;
        }
        for (int i = start; i < a.length; i++) {
            if (prune && a[i] > remaining) break;   // sorted: later a[i] are too big
            chosen.add(a[i]);                       // choose
            search(a, reuse ? i : i + 1, remaining - a[i], chosen, out, prune, reuse);
            chosen.remove(chosen.size() - 1);       // unchoose
        }
    }
}
