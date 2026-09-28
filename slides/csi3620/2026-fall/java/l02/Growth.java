package l02;

import java.math.BigInteger;

/** Exact values of the common growth functions, for the comparison table. */
public final class Growth {
    private Growth() { }

    public static int log2(int n) {
        return 31 - Integer.numberOfLeadingZeros(n);   // floor(log2 n), n >= 1
    }

    public static BigInteger twoTo(int n) {
        return BigInteger.ONE.shiftLeft(n);
    }

    public static BigInteger factorial(int n) {
        BigInteger r = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            r = r.multiply(BigInteger.valueOf(i));
        }
        return r;
    }
}
