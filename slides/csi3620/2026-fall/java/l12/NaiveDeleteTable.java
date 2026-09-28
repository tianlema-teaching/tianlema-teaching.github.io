package l12;

/**
 * A deliberately wrong linear-probing set of nonnegative ints: remove() empties the slot.
 * It exists only to show why open addressing needs tombstones.
 */
public final class NaiveDeleteTable {
    private static final int EMPTY = -1;
    private final int[] slots;

    public NaiveDeleteTable(int m) {
        slots = new int[m];
        java.util.Arrays.fill(slots, EMPTY);
    }

    public void add(int k) {                  // assumes a free slot exists
        int i = k % slots.length;
        while (slots[i] != EMPTY && slots[i] != k) {
            i = (i + 1) % slots.length;
        }
        slots[i] = k;
    }

    public boolean contains(int k) {
        int i = k % slots.length;
        for (int j = 0; j < slots.length && slots[i] != EMPTY; j++) {
            if (slots[i] == k) {
                return true;
            }
            i = (i + 1) % slots.length;
        }
        return false;
    }

    public void removeWrong(int k) {
        int i = k % slots.length;
        for (int j = 0; j < slots.length && slots[i] != EMPTY; j++) {
            if (slots[i] == k) {
                slots[i] = EMPTY;             // BUG: breaks probe chains through i
                return;
            }
            i = (i + 1) % slots.length;
        }
    }
}
