package l07;

import java.util.List;

/** A recursive method that logs when each call starts and returns. */
public final class CallStackDemo {

    private CallStackDemo() {
    }

    public static int sumTo(int n, List<String> log) {
        log.add("call sumTo(" + n + ")");
        int result = (n == 0) ? 0 : n + sumTo(n - 1, log);
        log.add("return " + result + " from sumTo(" + n + ")");
        return result;
    }
}
