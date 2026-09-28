package l12;

/** Small hash functions used in the lecture. */
public final class Hashing {

    private Hashing() {
    }

    /** Division method: h(k) = k mod m, for a nonnegative key. */
    public static int division(int k, int m) {
        return k % m;
    }

    /** Multiplication method: h(k) = floor(m * frac(k * A)), with A = (sqrt(5) - 1) / 2. */
    public static int multiplication(int k, int m) {
        final double a = (Math.sqrt(5) - 1) / 2;
        double product = k * a;
        double frac = product - Math.floor(product);   // fractional part, in [0, 1)
        return (int) (m * frac);
    }

    /** Same value as String.hashCode(): s[0]*31^(n-1) + ... + s[n-1], with int overflow. */
    public static int stringHash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);         // Horner's rule
        }
        return h;
    }

    /** Compress any int, including a negative one, to a slot in 0..m-1. */
    public static int indexFor(int hashCode, int m) {
        return Math.floorMod(hashCode, m);
    }

    /** For m a power of two, masking keeps the low bits and is never negative. */
    public static int indexForPowerOfTwo(int hashCode, int m) {
        return hashCode & (m - 1);
    }
}
