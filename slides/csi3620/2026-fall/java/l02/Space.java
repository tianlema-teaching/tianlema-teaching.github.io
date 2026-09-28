package l02;

/** Same answer, different extra memory. */
public final class Space {
    private Space() { }

    public static int[] reversedCopy(int[] a) {
        int[] out = new int[a.length];         // Theta(n) auxiliary
        for (int i = 0; i < a.length; i++) {
            out[a.length - 1 - i] = a[i];
        }
        return out;
    }

    public static void reverseInPlace(int[] a) {
        for (int i = 0, j = a.length - 1; i < j; i++, j--) {
            int tmp = a[i];                    // Theta(1) auxiliary
            a[i] = a[j];
            a[j] = tmp;
        }
    }
}
