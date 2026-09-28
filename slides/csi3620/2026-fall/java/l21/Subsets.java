package l21;

import java.util.ArrayList;
import java.util.List;

/** Generate every subset and every permutation by choose, explore, unchoose. */
public final class Subsets {
    private Subsets() { }

    /** All 2^n subsets of items, each as a list in the original order. */
    public static <T> List<List<T>> subsets(List<T> items) {
        List<List<T>> out = new ArrayList<>();
        subsets(items, 0, new ArrayList<>(), out);
        return out;
    }

    private static <T> void subsets(List<T> items, int i, List<T> chosen,
            List<List<T>> out) {
        if (i == items.size()) {             // all decided: record a copy
            out.add(new ArrayList<>(chosen));
            return;
        }
        chosen.add(items.get(i));            // choose: include items[i]
        subsets(items, i + 1, chosen, out);  // explore
        chosen.remove(chosen.size() - 1);    // unchoose
        subsets(items, i + 1, chosen, out);  // explore without items[i]
    }

    /** All n! orderings of items. */
    public static <T> List<List<T>> permutations(List<T> items) {
        List<List<T>> out = new ArrayList<>();
        permute(items, new boolean[items.size()], new ArrayList<>(), out);
        return out;
    }

    private static <T> void permute(List<T> items, boolean[] used, List<T> order,
                                    List<List<T>> out) {
        if (order.size() == items.size()) {
            out.add(new ArrayList<>(order));
            return;
        }
        for (int k = 0; k < items.size(); k++) {
            if (used[k]) continue;                  // each item appears once
            used[k] = true;                         // choose
            order.add(items.get(k));
            permute(items, used, order, out);       // explore
            order.remove(order.size() - 1);         // unchoose
            used[k] = false;
        }
    }
}
