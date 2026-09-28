package l03;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Concrete checks for every class in l03; every number on the slides is asserted here. */
public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }

    static int bruteLowerBound(int[] a, int t) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] >= t) {
                return i;
            }
        }
        return a.length;
    }

    static int floorLog2PlusOne(int n) {
        return n == 0 ? 0 : 31 - Integer.numberOfLeadingZeros(n) + 1;
    }

    public static void main(String[] args) {
        // Running example: eight made-up student IDs.
        int[] ids = {1609, 1024, 1835, 1330, 1998, 1187, 1771, 1452};
        int[] sorted = ids.clone();
        Arrays.sort(sorted);
        check(Arrays.equals(sorted, new int[] {1024, 1187, 1330, 1452, 1609, 1771, 1835, 1998}), "sorted ids");

        // Linear search.
        check(Search.linearSearch(ids, 1609) == 0, "best case: first");
        check(Search.linearSearch(ids, 1771) == 6, "1771 at 6");
        check(Search.linearSearch(ids, 1500) == -1, "absent");
        check(Search.linearSearch(new int[0], 5) == -1, "empty linear");
        check(SearchTrace.linear(ids, 1771).equals(List.of(
                "i=0 arr[i]=1609 no", "i=1 arr[i]=1024 no", "i=2 arr[i]=1835 no",
                "i=3 arr[i]=1330 no", "i=4 arr[i]=1998 no", "i=5 arr[i]=1187 no",
                "i=6 arr[i]=1771 found")), "linear trace 1771");
        List<String> l1500 = SearchTrace.linear(ids, 1500);
        check(l1500.size() == 9 && l1500.get(8).equals("end not found"), "linear trace 1500: " + l1500);
        check(SearchTrace.linearComparisons(ids, 1609) == 1, "1 comparison");
        check(SearchTrace.linearComparisons(ids, 1771) == 7, "1771: 7 comparisons");
        check(SearchTrace.linearComparisons(ids, 1452) == 8, "last: 8 comparisons");
        check(SearchTrace.linearComparisons(ids, 1500) == 8, "absent: 8 comparisons");
        int total = 0;
        for (int id : ids) {
            total += SearchTrace.linearComparisons(ids, id);
        }
        check(total == 36, "average over positions (1+..+8)/8 = 4.5");
        for (int n = 1; n <= 200; n++) {           // sum of 1..n = n(n+1)/2, so average (n+1)/2
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = 7 * i + 1;
            }
            int sum = 0;
            for (int x : a) {
                sum += SearchTrace.linearComparisons(a, x);
            }
            check(2 * sum == n * (n + 1), "linear average formula at n=" + n);
        }

        // Binary search traces on the sorted IDs.
        check(SearchTrace.binary(sorted, 1771).equals(List.of(
                "low=0 high=7 mid=3 arr[mid]=1452 go right",
                "low=4 high=7 mid=5 arr[mid]=1771 found")), "trace 1771");
        check(SearchTrace.binary(sorted, 1500).equals(List.of(
                "low=0 high=7 mid=3 arr[mid]=1452 go right",
                "low=4 high=7 mid=5 arr[mid]=1771 go left",
                "low=4 high=4 mid=4 arr[mid]=1609 go left",
                "low=4 high=3 empty")), "trace 1500");
        check(SearchTrace.binary(sorted, 1998).equals(List.of(
                "low=0 high=7 mid=3 arr[mid]=1452 go right",
                "low=4 high=7 mid=5 arr[mid]=1771 go right",
                "low=6 high=7 mid=6 arr[mid]=1835 go right",
                "low=7 high=7 mid=7 arr[mid]=1998 found")), "trace 1998");
        check(SearchTrace.binary(sorted, 1452).equals(List.of(
                "low=0 high=7 mid=3 arr[mid]=1452 found")), "best case: middle first");
        check(Search.binarySearch(sorted, 1771) == 5 && Search.binarySearch(sorted, 1500) == -1, "binary");
        check(Search.binarySearch(sorted, 1998) == 7 && Search.binarySearch(sorted, 1452) == 3, "binary 2");
        check(Search.binarySearch(new int[0], 7) == -1 && Search.binarySearch(new int[] {7}, 7) == 0
                && Search.binarySearch(new int[] {7}, 8) == -1
                && Search.binarySearch(new int[] {7}, 6) == -1, "tiny binary");
        check(Search.binarySearchRecursive(new int[0], 7) == -1
                && Search.binarySearchRecursive(sorted, 1771) == 5, "tiny recursive");

        // Probes per present target for n = 8: 3 2 3 1 3 2 3 4, total 21.
        int[] expected = {3, 2, 3, 1, 3, 2, 3, 4};
        int probeSum = 0;
        for (int i = 0; i < sorted.length; i++) {
            int p = SearchTrace.binaryProbes(sorted, sorted[i]);
            check(p == expected[i], "probes for " + sorted[i]);
            probeSum += p;
        }
        check(probeSum == 21, "average 21/8 probes");
        check(SearchTrace.binaryProbes(sorted, 1500) == 3 && SearchTrace.binaryProbes(sorted, 2000) == 4,
                "absent probes");
        List<String> below = SearchTrace.binary(sorted, 1000);
        List<String> above = SearchTrace.binary(sorted, 2000);
        check(below.get(below.size() - 1).equals("low=0 high=-1 empty"), "1000 ends with high = -1");
        check(above.get(above.size() - 1).equals("low=8 high=7 empty"), "2000 ends with low = 8");

        // Unsorted input: binary search misses 1609 at index 0, silently.
        check(Search.binarySearch(ids, 1609) == -1, "unsorted input: misses 1609 at index 0");
        check(SearchTrace.binary(ids, 1609).equals(List.of(
                "low=0 high=7 mid=3 arr[mid]=1330 go right",
                "low=4 high=7 mid=5 arr[mid]=1187 go right",
                "low=6 high=7 mid=6 arr[mid]=1771 go left",
                "low=6 high=5 empty")), "unsorted trace");

        // Exhaustive agreement with linear search on small arrays.
        Random rng = new Random(3620);
        for (int n = 0; n <= 300; n++) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = 3 * i + rng.nextInt(3);   // strictly increasing
            }
            for (int t = -2; t <= 3 * n + 2; t++) {
                int lin = Search.linearSearch(a, t);
                check(Search.binarySearch(a, t) == lin, "iterative agrees");
                check(Search.binarySearchRecursive(a, t) == lin, "recursive agrees");
                check(Search.lowerBound(a, t) == bruteLowerBound(a, t), "lower bound");
                List<String> tr = SearchTrace.binary(a, t);
                int probes = SearchTrace.binaryProbes(a, t);
                check(tr.size() == (lin < 0 ? probes + 1 : probes), "trace length matches probe count");
                int ins = Arrays.binarySearch(a, t);
                check(lin >= 0 ? ins == lin : ins == -Search.lowerBound(a, t) - 1, "library encoding");
            }
        }

        // Probe bound floor(log2 n) + 1: never exceeded, and reached, for n = 1..2048.
        for (int n = 1; n <= 2048; n++) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = 2 * i + 1;                  // odd values; even targets are absent
            }
            int bound = floorLog2PlusOne(n);
            int worst = 0;
            long presentSum = 0;
            for (int t = 0; t <= 2 * n; t++) {
                int probes = SearchTrace.binaryProbes(a, t);
                check(probes <= bound, "probes " + probes + " > bound " + bound + " at n=" + n);
                worst = Math.max(worst, probes);
                if (t % 2 == 1) {
                    presentSum += probes;
                }
            }
            check(worst == bound, "bound is reached at n=" + n);
            // Average over present targets is at least floor(log2 n) / 2, so Theta(log n).
            check(2 * presentSum >= (long) n * (bound - 1), "average lower bound at n=" + n);
        }
        check(floorLog2PlusOne(1) == 1 && floorLog2PlusOne(8) == 4 && floorLog2PlusOne(100) == 7
                && floorLog2PlusOne(1000) == 10 && floorLog2PlusOne(1024) == 11
                && floorLog2PlusOne(1_000_000) == 20, "bound table");

        // Overflow of low + high.
        int low = 1_500_000_000;
        int high = 2_000_000_000;
        check(low + high == -794_967_296, "low + high overflows int");
        check((low + high) / 2 == -397_483_648, "naive midpoint is negative");
        int mid = low + (high - low) / 2;
        check(mid == 1_750_000_000, "safe midpoint");
        check((low + high) >>> 1 == 1_750_000_000, "unsigned shift also safe");

        // Duplicates and lower bound.
        int[] dup = {1024, 1330, 1330, 1330, 1609};
        check(Search.binarySearch(dup, 1330) == 2, "some match, index 2");
        check(SearchTrace.binary(dup, 1330).equals(List.of("low=0 high=4 mid=2 arr[mid]=1330 found")), "dup trace");
        check(Search.lowerBound(dup, 1330) == 1, "first match, index 1");
        check(SearchTrace.lowerBound(dup, 1330).equals(List.of(
                "low=0 high=5 mid=2 arr[mid]=1330 high=mid",
                "low=0 high=2 mid=1 arr[mid]=1330 high=mid",
                "low=0 high=1 mid=0 arr[mid]=1024 low=mid+1",
                "answer 1")), "lower bound trace");
        check(Search.lowerBound(dup, 1500) == 4 && Search.lowerBound(dup, 2000) == 5
                && Search.lowerBound(dup, 1000) == 0, "lower bound positions");
        check(Search.lowerBound(new int[0], 3) == 0, "lower bound empty");
        check(Search.linearSearch(dup, 1330) == 1, "linear finds first");
        check(Search.lowerBound(sorted, 1500) == 4, "1500 would go at index 4");

        // java.util.Arrays.binarySearch: -(insertion point) - 1 when absent.
        check(Arrays.binarySearch(sorted, 1771) == 5, "library found");
        check(Arrays.binarySearch(sorted, 1500) == -5, "absent: -(4)-1");
        check(Arrays.binarySearch(sorted, 1000) == -1, "absent: -(0)-1");
        check(Arrays.binarySearch(sorted, 2000) == -9, "absent: -(8)-1");
        check(Arrays.binarySearch(sorted, 1100) == -2, "quiz: insertion point 1");

        // Sorted insertion: the lower-bound probes and the shift order drawn on the slide.
        check(SearchTrace.lowerBound(sorted, 1500).equals(List.of(
                "low=0 high=8 mid=4 arr[mid]=1609 high=mid",
                "low=0 high=4 mid=2 arr[mid]=1330 low=mid+1",
                "low=3 high=4 mid=3 arr[mid]=1452 low=mid+1",
                "answer 4")), "insert: lower-bound probes 1609, 1330, 1452");
        int[] shown = new int[9];
        System.arraycopy(sorted, 0, shown, 0, 8);
        check(SearchTrace.insertShifts(shown, 8, 1500).equals(List.of(
                "arr[8] = 1998", "arr[7] = 1835", "arr[6] = 1771", "arr[5] = 1609",
                "arr[4] = 1500")), "insert: shift order");
        int[] arr = new int[9];
        System.arraycopy(sorted, 0, arr, 0, 8);
        int at = SortedArray.insert(arr, 8, 1500);
        check(at == 4 && 8 - at == 4, "1500 lands at index 4 after 4 shifts");
        check(Arrays.equals(arr, new int[] {1024, 1187, 1330, 1452, 1500, 1609, 1771, 1835, 1998}), "after insert");
        check(Arrays.equals(arr, shown), "instrumented insert matches SortedArray.insert");
        int[] front = new int[9];
        System.arraycopy(sorted, 0, front, 0, 8);
        check(SortedArray.insert(front, 8, 1000) == 0, "smallest value: 8 shifts, the worst case");
        int[] one = new int[1];
        check(SortedArray.insert(one, 0, 5) == 0 && one[0] == 5, "insert into empty");
        int[] tail = {1, 2, 0};
        check(SortedArray.insert(tail, 2, 3) == 2 && Arrays.equals(tail, new int[] {1, 2, 3}), "insert at end");
        int[] head = {1, 2, 0};
        check(SortedArray.insert(head, 2, 0) == 0 && Arrays.equals(head, new int[] {0, 1, 2}), "insert at front");
        int[] dupIns = {1, 3, 3, 0};
        check(SortedArray.insert(dupIns, 3, 3) == 1, "equal value goes before existing equals");
        boolean threw = false;
        try {
            SortedArray.insert(new int[] {1, 2}, 2, 3);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check(threw, "full array rejected");

        // A null array is not a valid input: arr.length throws.
        boolean npe = false;
        try {
            Search.binarySearch(null, 1);
        } catch (NullPointerException e) {
            npe = true;
        }
        check(npe, "null array throws NullPointerException");

        System.out.println("l03 OK");
    }
}
