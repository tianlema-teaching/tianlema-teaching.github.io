package l12;

/** Prints the states of the running example that the slides draw. */
public final class Trace {

    static final int[] KEYS = {12, 44, 13, 88, 23};

    private Trace() {
    }

    public static void main(String[] args) {
        System.out.println("division method, m = 7");
        for (int k : KEYS) {
            System.out.println(k + " -> " + Hashing.division(k, 7));
        }
        System.out.println("multiplication method, m = 8");
        for (int k : KEYS) {
            System.out.println(k + " -> " + Hashing.multiplication(k, 8));
        }

        System.out.println("== chaining, m = 7");
        ChainingHashMap<Integer, String> chain = new ChainingHashMap<>(7, 0.75);
        for (int k : KEYS) {
            chain.put(k, "v" + k);
            System.out.println("after put " + k + " (size " + chain.size() + ")");
            System.out.print(chain.bucketString());
        }
        chain.put(51, "v51");
        System.out.println("after put 51: capacity " + chain.capacity());
        System.out.print(chain.bucketString());

        System.out.println("== linear probing, m = 7");
        LinearProbingHashMap<Integer, String> probe = new LinearProbingHashMap<>(7, 0.75);
        for (int k : KEYS) {
            probe.put(k, "v" + k);
            System.out.println("after put " + k + ": " + probe.slotString()
                    + " probes " + probe.probeSequence(k));
        }
        System.out.println("search 51 probes " + probe.probeSequence(51));
        probe.remove(44);
        System.out.println("after remove 44: " + probe.slotString());
        System.out.println("search 23 probes " + probe.probeSequence(23)
                + " found " + probe.containsKey(23));
        probe.put(51, "v51");
        System.out.println("after put 51: " + probe.slotString());

        System.out.println("== naive delete");
        NaiveDeleteTable naive = new NaiveDeleteTable(7);
        for (int k : KEYS) {
            naive.add(k);
        }
        naive.removeWrong(44);
        System.out.println("contains 23 after naive remove 44: " + naive.contains(23));

        System.out.println("== strings");
        for (String s : new String[] {"a", "ab", "cat", "hash", "polygenelubricants"}) {
            System.out.println(s + " " + s.hashCode() + " floorMod16 " + Math.floorMod(s.hashCode(), 16)
                    + " % 16 " + (s.hashCode() % 16) + " mask " + (s.hashCode() & 15));
        }
        System.out.println("abs(MIN) " + Math.abs(Integer.MIN_VALUE));
    }
}
