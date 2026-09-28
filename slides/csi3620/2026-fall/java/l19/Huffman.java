package l19;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/** Huffman coding: repeatedly merge the two least frequent trees. */
public final class Huffman {
    private Huffman() { }

    /** A tree node; leaves carry a symbol. seq breaks ties by creation order. */
    record Node(char symbol, long freq, Node left, Node right, int seq) {
        boolean isLeaf() { return left == null; }
    }

    public static Map<Character, String> codes(Map<Character, Long> freq) {
        return codes(freq, new ArrayList<>());
    }

    public static Map<Character, String> codes(Map<Character, Long> freq, List<String> log) {
        if (freq.isEmpty()) return new TreeMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) ->
                a.freq() != b.freq() ? Long.compare(a.freq(), b.freq())
                                     : Integer.compare(a.seq(), b.seq()));
        int seq = 0;
        for (var e : new TreeMap<>(freq).entrySet()) {
            pq.add(new Node(e.getKey(), e.getValue(), null, null, seq++));
        }
        while (pq.size() > 1) {
            Node a = pq.poll(), b = pq.poll();          // two smallest
            Node merged = new Node('\0', a.freq() + b.freq(), a, b, seq++);
            log.add("merge " + label(a) + " + " + label(b)
                    + " = " + merged.freq());
            pq.add(merged);
        }
        Map<Character, String> out = new TreeMap<>();
        assign(pq.poll(), "", out);
        return out;
    }

    private static void assign(Node t, String prefix, Map<Character, String> out) {
        if (t.isLeaf()) {
            out.put(t.symbol(), prefix.isEmpty() ? "0" : prefix);  // one symbol: 1 bit
            return;
        }
        assign(t.left(), prefix + "0", out);           // left edge is 0
        assign(t.right(), prefix + "1", out);          // right edge is 1
    }

    static String label(Node t) {
        if (t.isLeaf()) return t.symbol() + ":" + t.freq();
        return "(" + leaves(t) + "):" + t.freq();
    }

    private static String leaves(Node t) {
        return t.isLeaf() ? String.valueOf(t.symbol()) : leaves(t.left()) + leaves(t.right());
    }

    /** Total encoded length: sum of frequency times code length. */
    public static long totalBits(Map<Character, Long> freq, Map<Character, String> code) {
        long bits = 0;
        for (var e : freq.entrySet()) bits += e.getValue() * code.get(e.getKey()).length();
        return bits;
    }
}
