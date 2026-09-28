package l01;

import java.util.ArrayList;
import java.util.List;

/** Counts how many values each strategy looks at before it finds the target. */
public final class EfficiencyGame {
    private EfficiencyGame() { }

    public static int[] oneTo(int n) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = i + 1;
        }
        return values;
    }

    public static int linearProbes(int[] sorted, int target) {
        int probes = 0;
        for (int value : sorted) {
            probes++;
            if (value == target) {
                return probes;
            }
        }
        return probes;
    }

    /** The values binary search looks at, in order. */
    public static List<Integer> binaryProbes(int[] sorted, int target) {
        List<Integer> probed = new ArrayList<>();
        int low = 0, high = sorted.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            probed.add(sorted[mid]);
            if (sorted[mid] == target) {
                break;
            } else if (sorted[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return probed;
    }
}
