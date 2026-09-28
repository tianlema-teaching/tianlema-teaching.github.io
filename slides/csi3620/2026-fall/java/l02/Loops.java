package l02;

/** Loop shapes; each method returns how many times its innermost body runs. */
public final class Loops {
    private Loops() { }

    public static long single(int n) {
        long count = 0;
        for (int i = 0; i < n; i++) {
            count++;
        }
        return count;
    }

    public static long nested(int n) {
        long count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                count++;
            }
        }
        return count;
    }

    public static long dependent(int n) {
        long count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                count++;
            }
        }
        return count;
    }

    public static long halving(int n) {
        long count = 0;
        for (int i = n; i >= 1; i /= 2) {
            count++;
        }
        return count;
    }

    public static long nestedHalving(int n) {
        long count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = n; j >= 1; j /= 2) {
                count++;
            }
        }
        return count;
    }
}
