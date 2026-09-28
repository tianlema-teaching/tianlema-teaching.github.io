package l19;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Choose as many pairwise compatible intervals as possible. */
public final class IntervalScheduling {
    private IntervalScheduling() { }

    public static List<Interval> earliestFinish(List<Interval> intervals) {
        return earliestFinish(intervals, new ArrayList<>());
    }

    /** The optimal greedy rule: always take the compatible interval that ends first. */
    public static List<Interval> earliestFinish(List<Interval> intervals, List<String> log) {
        List<Interval> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparingInt(Interval::finish));
        List<Interval> chosen = new ArrayList<>();
        int free = Integer.MIN_VALUE;        // time when the room is free again
        for (Interval x : sorted) {
            if (x.start() >= free) {
                chosen.add(x);
                free = x.finish();
                log.add("take " + x);
            } else {
                log.add("skip " + x);
            }
        }
        return chosen;
    }

    /** Any other rule: scan in the given order, keep what fits with all chosen. */
    public static List<Interval> greedyBy(List<Interval> intervals, Comparator<Interval> order) {
        List<Interval> sorted = new ArrayList<>(intervals);
        sorted.sort(order);
        List<Interval> chosen = new ArrayList<>();
        for (Interval x : sorted) {
            boolean fits = true;
            for (Interval c : chosen) fits &= x.compatibleWith(c);
            if (fits) chosen.add(x);
        }
        return chosen;
    }

    public static final Comparator<Interval> SHORTEST_FIRST = Comparator.comparingInt(Interval::length);
    public static final Comparator<Interval> EARLIEST_START = Comparator.comparingInt(Interval::start);
}
