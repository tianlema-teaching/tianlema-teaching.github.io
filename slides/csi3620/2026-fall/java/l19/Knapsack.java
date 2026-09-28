package l19;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Greedy by value per unit weight: optimal when items can be split. */
public final class Knapsack {
    private Knapsack() { }

    static List<Item> byRatio(List<Item> items) {
        List<Item> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparingDouble(Item::ratio).reversed());
        return sorted;
    }

    /** Fractional knapsack: take the best ratio first, split the last item. */
    public static double fractional(List<Item> items, int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("negative capacity " + capacity);
        double total = 0;
        int room = capacity;
        for (Item it : byRatio(items)) {
            if (room == 0) break;
            int take = Math.min(it.weight(), room);
            total += (double) it.value() * take / it.weight();
            room -= take;
        }
        return total;
    }

    /** The same rule for 0/1 knapsack: whole items only. Not always optimal. */
    public static int greedyZeroOne(List<Item> items, int capacity) {
        int total = 0;
        int room = capacity;
        for (Item it : byRatio(items)) {
            if (it.weight() <= room) {
                total += it.value();
                room -= it.weight();
            }
        }
        return total;
    }
}
