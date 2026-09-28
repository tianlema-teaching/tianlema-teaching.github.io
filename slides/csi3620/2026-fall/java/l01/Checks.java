package l01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }

    static void checkSet(IntSet set, String name) {
        check(set.size() == 0, name + " starts empty");
        for (int x : new int[] {27, 8, 19, 3}) {
            check(set.add(x), name + " adds " + x);
        }
        check(!set.add(8), name + " rejects duplicate 8");
        check(set.size() == 4, name + " size 4");
        for (int x : new int[] {3, 8, 19, 27}) {
            check(set.contains(x), name + " contains " + x);
        }
        for (int x : new int[] {0, 14, 28, -5}) {
            check(!set.contains(x), name + " lacks " + x);
        }
        for (int x = 100; x < 150; x++) {
            check(set.add(x), name + " grows past capacity");
        }
        check(set.size() == 54 && set.contains(149) && !set.contains(150), name + " after growth");
    }

    public static void main(String[] args) {
        checkSet(new UnsortedIntSet(), "unsorted");
        checkSet(new SortedIntSet(), "sorted");

        // Running example: add 27, 8, 19, 3 then 14.
        UnsortedIntSet u = new UnsortedIntSet();
        SortedIntSet s = new SortedIntSet();
        for (int x : new int[] {27, 8, 19, 3}) {
            u.add(x);
            s.add(x);
        }
        check(Arrays.equals(u.toArray(), new int[] {27, 8, 19, 3}), "unsorted keeps arrival order");
        check(Arrays.equals(s.toArray(), new int[] {3, 8, 19, 27}), "sorted keeps order");
        check(u.data.length == 4 && s.data.length == 4, "both arrays full at capacity 4");
        u.add(14);
        s.add(14);
        check(u.data.length == 8 && s.data.length == 8, "add 14 doubles both arrays to 8 slots");
        check(Arrays.equals(u.toArray(), new int[] {27, 8, 19, 3, 14}), "unsorted appends 14");
        check(Arrays.equals(s.toArray(), new int[] {3, 8, 14, 19, 27}), "sorted inserts 14 at index 2");

        // Efficiency game: find 27 in 1..32.
        int[] game = EfficiencyGame.oneTo(32);
        check(game[0] == 1 && game[31] == 32, "1..32");
        check(EfficiencyGame.linearProbes(game, 27) == 27, "linear checks 27 values");
        List<Integer> probes = EfficiencyGame.binaryProbes(game, 27);
        check(probes.equals(List.of(16, 24, 28, 26, 27)), "binary probes 16 24 28 26 27");
        int worst = 0;
        for (int t = 0; t <= 33; t++) {
            worst = Math.max(worst, EfficiencyGame.binaryProbes(game, t).size());
            if (t >= 1 && t <= 32) {
                check(EfficiencyGame.binaryProbes(game, t).get(EfficiencyGame.binaryProbes(game, t).size() - 1) == t, "finds " + t);
                check(EfficiencyGame.linearProbes(game, t) == t, "linear " + t);
            }
        }
        check(worst == 6, "at most 6 probes for 32 values, got " + worst);
        check(EfficiencyGame.binaryProbes(game, 32).size() == 6, "32 needs 6 probes");
        for (int t = 1; t <= 31; t++) {
            check(EfficiencyGame.binaryProbes(game, t).size() <= 5, "only 32 needs 6 of the present values");
        }
        check(EfficiencyGame.binaryProbes(game, 33).size() == 6
                && EfficiencyGame.binaryProbes(game, 1000).size() == 6, "absent values above 32 need 6");
        check(EfficiencyGame.binaryProbes(game, 0).size() == 5, "absent 0 needs 5");
        check(EfficiencyGame.linearProbes(game, 99) == 32, "absent: linear checks all 32");
        // Doubling adds one probe: 1..64 worst is 7; 1..1024 worst is 11.
        for (int[] pair : new int[][] {{64, 7}, {1024, 11}}) {
            int[] big = EfficiencyGame.oneTo(pair[0]);
            int w = 0;
            for (int t = 0; t <= pair[0] + 1; t++) {
                w = Math.max(w, EfficiencyGame.binaryProbes(big, t).size());
            }
            check(w == pair[1], "worst probes for " + pair[0]);
        }

        // Java review.
        check(JavaReview.maxOf(List.of(27, 8, 19, 3)) == 27, "max int");
        check(JavaReview.maxOf(List.of("pear", "apple", "fig")).equals("pear"), "max string");
        boolean threw = false;
        try {
            JavaReview.maxOf(List.<Integer>of());
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check(threw, "max of empty throws");
        check(JavaReview.sumTo(0) == 0 && JavaReview.sumTo(4) == 10 && JavaReview.sumTo(100) == 5050, "sumTo");
        check(JavaReview.sumTo(-1) == 0 && JavaReview.sumTo(Integer.MIN_VALUE) == 0, "sumTo of n <= 0 is 0");
        List<String> log = new ArrayList<>();
        check(JavaReview.sumToTraced(3, 1, log) == 6, "traced sumTo(3)");
        check(log.equals(List.of(
                "call sumTo(3), frames 1", "call sumTo(2), frames 2",
                "call sumTo(1), frames 3", "call sumTo(0), frames 4",
                "sumTo(0) returns 0", "sumTo(1) returns 1",
                "sumTo(2) returns 3", "sumTo(3) returns 6")), "sumTo(3) trace: " + log);

        System.out.println("l01 OK");
    }
}
