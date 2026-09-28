package l19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Greedy change making: always take the largest coin that fits. */
public final class CoinChange {
    private CoinChange() { }

    public static List<Integer> greedy(int[] coins, int amount) {
        for (int c : coins) {
            if (c <= 0) throw new IllegalArgumentException("coin values must be positive: " + c);
        }
        int[] sorted = coins.clone();
        Arrays.sort(sorted);
        List<Integer> used = new ArrayList<>();
        int left = amount;
        for (int i = sorted.length - 1; i >= 0; i--) {
            while (sorted[i] <= left) {
                used.add(sorted[i]);
                left -= sorted[i];
            }
        }
        if (left != 0) throw new IllegalArgumentException("greedy cannot make " + amount);
        return used;
    }
}
